package me.xxastaspastaxx.dimensions.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

import me.xxastaspastaxx.dimensions.Main;
import me.xxastaspastaxx.dimensions.events.DestroyCause;
import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.portal.PortalFrame;
import me.xxastaspastaxx.dimensions.portal.listeners.PortalListeners;
import me.xxastaspastaxx.dimensions.utils.Dimensions;
import me.xxastaspastaxx.dimensions.utils.DimensionsSettings;
import me.xxastaspastaxx.dimensions.utils.Messages;

public class PortalFiles implements Listener {

	private PortalClass portalClass;
	private PortalListeners portalListeners;
	
	private PortalLocations portalLocations;
	private PlayerHistories playerHistories;
	private PlayerData playerData;
	private long lastSave = 0;
	
  	public static ArrayList<CustomPortal> createdPortals = new ArrayList<CustomPortal>();
  	public static ArrayList<Material> lighters = new ArrayList<Material>();
  	public static ArrayList<Material> frameMaterials = new ArrayList<Material>();
  	public static ArrayList<Material> blocks = new ArrayList<Material>();
	
  	private Main plugin;
	
	public PortalFiles(Main pl) {
		
		plugin = pl;
	  	
		//Create general settings config file
		File settings = new File("plugins/Dimensions/Settings.yml");
		YamlConfiguration portalSettings = YamlConfiguration.loadConfiguration(settings);
		

		portalSettings.addDefault("Debug.Level", 0);
		portalSettings.addDefault("GenerateNewWorlds", false);
		portalSettings.addDefault("MaxRadius", 10);
		portalSettings.addDefault("DefaultWorld", "world");
		portalSettings.addDefault("EnableParticles", false);
		portalSettings.addDefault("EnableMobsTeleportation", false);
		portalSettings.addDefault("EnableNonLivingEntitiesTeleportation", false);
		portalSettings.addDefault("TeleportDelay", 4);
		portalSettings.addDefault("SearchRadius", 128);
		portalSettings.addDefault("SafeSpotSearchRadius", 16);
		portalSettings.addDefault("ConsumeItems", true);
		portalSettings.addDefault("NetherPortalEffect", true);
		portalSettings.addDefault("ShowPortalContent", true);
		if (portalSettings.getStringList("PathRules").isEmpty()) portalSettings.addDefault("PathRules", new ArrayList<String>());
		
		portalSettings.options().copyDefaults(true);
  	  	
  	  	try {
  	  		portalSettings.save(settings);
  	  	} catch (IOException e) {
			e.printStackTrace();
		}

	  	portalClass = new PortalClass(pl);

	  	DimensionsSettings.reloadSettings();
	  	

		//Create and register all portals
		reloadPortals();

	  	portalClass.debug("Loaded "+ createdPortals.size() +" portals",1);
		
		portalListeners = new PortalListeners(pl, portalClass);
		
		
		
		//TODO
		File fileVersions = new File("plugins/Dimensions/versions.yml");
		YamlConfiguration fileVesionsConfig = YamlConfiguration.loadConfiguration(fileVersions);
		
		portalLocations = new PortalLocations(fileVesionsConfig.getDouble("PortalLocations", 1.0));
		portalClass.setPortalLocations(portalLocations, portalListeners);
		
		playerHistories = new PlayerHistories(portalClass, fileVesionsConfig.getDouble("PlayerHistories", 1.0));
	    portalClass.setPlayerHistories(playerHistories);
		
	    playerData = new PlayerData(fileVesionsConfig.getDouble("PlayerData", 1.0));
	    portalClass.setPlayerData(playerData);

	    Bukkit.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	public PortalClass getPortalClass() {
		return portalClass;
	}
	
	public PortalListeners getPortalListeners() {
		return portalListeners;
	}
	
	public void onDisable() {
		save();
		@SuppressWarnings("unchecked")
		Iterator<CompletePortal> iter = ((ArrayList<CompletePortal>) portalClass.getCompletePortals().clone()).iterator();
		while (iter.hasNext()) {
			CompletePortal complete = iter.next();
			complete.getPortal().destroy(complete, true, DestroyCause.PLUGIN, null);
		}
	}
	
	public void save() {
		portalLocations.save();
		playerHistories.save();
		playerData.save();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		Object hold = playerData.getData(p.getUniqueId(), "Hold");
		if (hold!=null && (boolean) hold) {
			CompletePortal compl = portalClass.getPortalAtLocation(p.getLocation());
			if (compl!=null) {
				compl.addToHold(p, true);
			}
			playerData.setData(p.getUniqueId(), "Hold", false);
		}
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (portalClass.isOnHold(p)) {
			playerData.setData(p.getUniqueId(), "Hold", true);
		}
		
		for (PortalFrame frame : portalClass.getAllFrames()) {
			frame.remove(p);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSave(WorldSaveEvent e) {
		if ((System.currentTimeMillis()-lastSave)/1000>=5) {
			lastSave = System.currentTimeMillis();
			portalClass.debug("Saved Dimensions portal locations and player histories", 1);
			save();
		}
	}

	public boolean reloadPortals() {
		File portalFolder = new File("plugins/Dimensions/Portals");
		if (!portalFolder.exists()) portalFolder.mkdir();
		File[] portals = portalFolder.listFiles();
		
		
		lighters.clear();
		frameMaterials.clear();
		blocks.clear();

	  	portalClass.debug("Loading Portals...",2);
	  	
		@SuppressWarnings("unchecked")
		ArrayList<CustomPortal> oldPortals = (ArrayList<CustomPortal>) createdPortals.clone();
		
		for (File portal : portals) {
			String fileName = portal.getName();
		  	portalClass.debug("Testing "+fileName,2);
			if (fileName.contentEquals("portalLocations.json") || fileName.contains(" ")) continue;
		  	portalClass.debug("Loading "+fileName,2);
			
			YamlConfiguration portalConfig = YamlConfiguration.loadConfiguration(portal);
			
			//Load portal settings
			String name = fileName.replace(".yml", "");
			
			boolean enabled = portalConfig.getBoolean("Enable", false);
			if (!enabled) continue;
			String displayName = portalConfig.getString("DisplayName", "Unnamed");

			boolean horizontal = portalConfig.getBoolean("Portal.Horizontal", false);
			Material material = Material.matchMaterial(portalConfig.getString("Portal.Block.Material", "COBBLESTONE"));
			String face = portalConfig.getString("Portal.Block.Face", "all");
			Material frame =  Material.matchMaterial(portalConfig.getString("Portal.Frame", "NETHER_PORTAL"));
			Material lighter = Material.matchMaterial(portalConfig.getString("Portal.Lighter", "FLINT_AND_STEEL"));
			int minPortalWidth = portalConfig.getInt("Portal.MinWidth", 4);
			int minPortalHeight = portalConfig.getInt("Portal.MinHeight", 5);
			
			
			boolean needsWorld = portalConfig.getBoolean("World.isNeeded", true);
			String worldName = portalConfig.getString("World.Name", "world");
			World world = Bukkit.getWorld(worldName);
			if (needsWorld) {
				if (!Bukkit.getServer().getWorlds().contains(world)) {
					if (DimensionsSettings.isGenerateWorlds()) {
						world = Bukkit.getServer().createWorld(new WorldCreator(worldName));
					} else {
						Dimensions.debug("Disabling portal: "+name, 0);
						Dimensions.debug("Reason: There is no world "+worldName+" and \"GenerateNewWorlds\" is set to \"false\"", 0);
						enabled = false;
						continue;
					}
				}
				if (world.equals(DimensionsSettings.getDefaultWorld())) {
					Dimensions.debug("Disabling portal: "+name, 0);
					Dimensions.debug("Reason: There cannot be a portal that leads to the default world", 0);
					enabled = false;
					continue;
				}
			}
			int worldHeight = portalConfig.getInt("World.MaxHeight", 256);
			String ratio = portalConfig.getString("World.Ratio", "1:1");
			
			HashMap<EntityType,EntityType> entityTransformation = new HashMap<EntityType,EntityType>();
			for (String entity : portalConfig.getStringList("Entities.Transformation")) {
				String[] spl = entity.toUpperCase().split("->");
				entityTransformation.put(EntityType.valueOf(spl[0]), EntityType.valueOf(spl[1]));
			}
			
			String[] spawningDelayString = portalConfig.getString("Entities.Spawning.Delay", "5000-10000").split("-");
			int[] spawningDelay = {Integer.parseInt(spawningDelayString[0]),Integer.parseInt(spawningDelayString[1])};
			HashMap<EntityType,Integer> entitySpawning = new HashMap<EntityType,Integer>();
			for (String entity : portalConfig.getStringList("Entities.Spawning.List")) {
				String[] spl = entity.toUpperCase().split(";");
				entitySpawning.put(EntityType.valueOf(spl[0]), Integer.parseInt(spl[1]));
			}
			
			boolean buildExitPortal = portalConfig.getBoolean("BuildExitPortal", true);
			boolean spawnOnAir = portalConfig.getBoolean("SpawnOnAir", false);
			
			ArrayList<World> disabledWorlds = new ArrayList<World>();
			for (String disabledWorld : portalConfig.getStringList("DisabledWorlds")) {
				disabledWorlds.add(Bukkit.getWorld(disabledWorld));
			}
			
			String particlesColor = portalConfig.getString("Portal.ParticlesColor", "0;0;0");
			
			if (!lighters.contains(lighter)) {
				lighters.add(lighter);
			}
			if (!frameMaterials.contains(frame)) {
				frameMaterials.add(frame);
			}
			if (!blocks.contains(material)) {
				blocks.add(material);
			}

			//add the custom portal to the list so it can be used for later calculations
			CustomPortal oldPortal = portalClass.getPortalByName(name);
			if (oldPortal!=null) {
				oldPortal.update(name, enabled, displayName, horizontal, material, face, frame, lighter, world, needsWorld, worldHeight, ratio, minPortalWidth, minPortalHeight, entityTransformation, spawningDelay, entitySpawning, buildExitPortal, spawnOnAir, disabledWorlds, particlesColor);
				oldPortals.remove(oldPortal);
			} else {
				createdPortals.add(new CustomPortal(portalClass, name, enabled, displayName, horizontal, material, face, frame, lighter, world, needsWorld, worldHeight, ratio, minPortalWidth, minPortalHeight, entityTransformation, spawningDelay, entitySpawning, buildExitPortal, spawnOnAir, disabledWorlds, particlesColor, plugin));
			}
		  	portalClass.debug("Loaded "+fileName,2);
		}
		
		for (CustomPortal portal : oldPortals) {
			portal.disable();
			createdPortals.remove(portal);
		}
		

		portalClass.setPortals(createdPortals, lighters, frameMaterials, blocks);

		return true;
	}
	
	public boolean reloadAll() {
		save();
		return DimensionsSettings.reloadSettings() && reloadPortals() && Messages.reload();
	}
	
}
