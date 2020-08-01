package me.xxastaspastaxx.dimensions.fileHandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.Main;
import me.xxastaspastaxx.dimensions.Messages;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.portal.PortalFrame;
import me.xxastaspastaxx.dimensions.portal.listeners.PortalListeners;

public class PortalFiles implements Listener {

	PortalClass portalClass;
	PortalListeners portalListeners;

	PortalLocations portalLocations;
	LocationsFile locationsFile;
	
	HistoryWorlds historyWorlds;
	HistoryFile historyFile;
	
  	public static ArrayList<CustomPortal> createdPortals = new ArrayList<CustomPortal>();
  	public static ArrayList<Material> lighters = new ArrayList<Material>();
  	public static ArrayList<Material> frameMaterials = new ArrayList<Material>();
  	public static ArrayList<Material> blocks = new ArrayList<Material>();
	
	Main plugin;
	
	public PortalFiles(Main pl) {
		
		plugin = pl;
	  	
		//Create general settings config file
		File settings = new File("plugins/Dimensions/Settings.yml");
		YamlConfiguration portalSettings = YamlConfiguration.loadConfiguration(settings);
		

		portalSettings.addDefault("DebugLevel", 0);
		portalSettings.addDefault("MaxRadius", 10);
		portalSettings.addDefault("DefaultWorld", "world");
		portalSettings.addDefault("EnableParticles", false);
		portalSettings.addDefault("EnableMobsTeleportation", false);
		portalSettings.addDefault("TeleportDelay", 4);
		portalSettings.addDefault("SearchRadius", 128);
		portalSettings.addDefault("SafeSpotSearchRadius", 16);
		portalSettings.addDefault("ConsumeItems", true);
		portalSettings.addDefault("NetherPortalEffect", true);
		
		
		portalSettings.options().copyDefaults(true);
  	  	
  	  	try {
  	  		portalSettings.save(settings);
  	  	} catch (IOException e) {
			e.printStackTrace();
		}
  	  	
	  	portalClass = new PortalClass(pl);

	  	reloadSettings();
	  	
	  	
		//Create and register all portals
		reloadPortals();

	  	portalClass.debug("Loaded "+ createdPortals.size() +" portals",1);
		
		portalListeners = new PortalListeners(pl, portalClass);
		
		try {
			writeJSONLocations(false);
			LocationsFile locationsFile = readJSONLocations();
			while (locationsFile==null) {
				writeJSONLocations(true);
				locationsFile = readJSONLocations();
			}

	        this.locationsFile = locationsFile;
	        this.portalLocations = new PortalLocations(portalClass, locationsFile);
	        portalLocations.convertStrings(locationsFile.getLocations());
	        portalClass.setPortalLocations(portalLocations, locationsFile,portalListeners);
	        
	        
			writeJSONHistories(false);
			HistoryFile historyFile = readJSONHistories();
			while (historyFile==null) {
				writeJSONHistories(true);
				historyFile = readJSONHistories();
			}

	        this.historyFile = historyFile;
	        this.historyWorlds = new HistoryWorlds(portalClass, historyFile);
	        historyWorlds.convertStrings(historyFile.getHistories());
	        portalClass.setPlayerHistories(historyWorlds);
		} catch (IOException e) {
			e.printStackTrace();
		}

	    Bukkit.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	public void fixOutdatedPortalFile(File portal) {
		YamlConfiguration portalConfig = YamlConfiguration.loadConfiguration(portal);
		
		portalConfig.addDefault("Enable", true);
		portalConfig.addDefault("DisplayName", "&SamplePortal");

  	  	portalConfig.addDefault("Portal.Block.Material", "stone");
  	  	portalConfig.addDefault("Portal.Block.Face", "all");
  	  	portalConfig.addDefault("Portal.ParticlesColor", "75;75;75");
  	  	portalConfig.addDefault("Portal.Frame", "nether_portal");
  	  	portalConfig.addDefault("Portal.Lighter", "flint_and_steel");
  	  	portalConfig.addDefault("Portal.MinWidth", 4);
  	  	portalConfig.addDefault("Portal.MinHeight", 5);
  	  	
  	  	portalConfig.addDefault("World.Name", "world");
  	  	portalConfig.addDefault("World.MaxHeight", 256);
  	  	portalConfig.addDefault("World.Ratio", "1:1");
  	  	
  	  	if (portalConfig.getStringList("Entities.Transformation").isEmpty()) {
  	  		List<String> entitiesTransformation = portalConfig.getStringList("Entities.Transformation");
  	  		//entitiesTransformation.add("SKELETON->WITHER_SKELETON");
  	  		portalConfig.set("Entities.Transformation", entitiesTransformation);
  	  	}
  	  	

  	  	portalConfig.addDefault("Entities.Spawning.Delay", "5000-10000");
  	  	if (portalConfig.getStringList("Entities.Spawning.List").isEmpty()) {
  	  		List<String> entitySpawning = portalConfig.getStringList("Entities.Spawning.List");
  	  		//entitySpawning.add("SKELETON;50");
  	  		portalConfig.set("Entities.Spawning.List", entitySpawning);
  	  	}
  	  	
  	  	portalConfig.addDefault("BuildExitPortal", true);
  	  	portalConfig.addDefault("SpawnOnAir", false);

  	  	if (portalConfig.getStringList("DisabledWorlds").isEmpty()) {
  	  		List<String> disabledWolrds = portalConfig.getStringList("DisabledWorlds");
  	  		//disabledWolrds.add("world_the_end");
  	  		portalConfig.set("DisabledWorlds", disabledWolrds);
  	  	}
  	  	
  	  	portalConfig.options().copyDefaults(true);

  	  	try {
  	  		portalConfig.save(portal);
  	  	} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeJSONLocations(boolean recreate) {
		try {
			File file = new File("plugins/Dimensions/Portals/portalLocations.json");
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists() || recreate) {
				FileWriter fw = new FileWriter(file);
			    fw.write("{}");
			    fw.flush();
			    fw.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeJSONHistories(boolean recreate) {
		try {
			File file = new File("plugins/Dimensions/PlayerData/playerHistories.json");
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists() || recreate) {
				FileWriter fw = new FileWriter(file);
			    fw.write("{}");
			    fw.flush();
			    fw.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeJSONLocations(LocationsFile locationsFile) throws IOException {
		GsonBuilder builder = new GsonBuilder(); 
		Gson gson = builder.create(); 
		FileWriter writer = new FileWriter("plugins/Dimensions/Portals/portalLocations.json");
		locationsFile.save(portalLocations.getLocations());
		writer.write(gson.toJson(locationsFile));   
		writer.close(); 
	}
	
	private void writeJSONHistories(HistoryFile historyFile) throws IOException {
		
		GsonBuilder builder = new GsonBuilder(); 
		Gson gson = builder.create(); 
		FileWriter writer = new FileWriter("plugins/Dimensions/PlayerData/playerHistories.json");
		historyWorlds.save();
		writer.write(gson.toJson(historyFile));   
		writer.close(); 
	}
	   
	private LocationsFile readJSONLocations() throws FileNotFoundException { 
		GsonBuilder builder = new GsonBuilder(); 
		Gson gson = builder.create(); 
		BufferedReader bufferedReader = new BufferedReader(new FileReader("plugins/Dimensions/Portals/portalLocations.json"));   

		LocationsFile locationsFile = gson.fromJson(bufferedReader, LocationsFile.class); 
		return locationsFile; 
	}
	
	private HistoryFile readJSONHistories() throws FileNotFoundException { 
		GsonBuilder builder = new GsonBuilder(); 
		Gson gson = builder.create(); 
		BufferedReader bufferedReader = new BufferedReader(new FileReader("plugins/Dimensions/PlayerData/playerHistories.json"));   

		HistoryFile historyFile = gson.fromJson(bufferedReader, HistoryFile.class); 
		return historyFile; 
	}
	
	public PortalClass getPortalClass() {
		return portalClass;
	}
	
	public PortalListeners getPortalListeners() {
		return portalListeners;
	}
	
	public void onDisable() {
		
		save();
		for (PortalFrame frame : portalClass.getFrames()) {
			frame.remove(null);
		}
	}
	
	public void save() {
		try {
			writeJSONLocations(locationsFile);
			writeJSONHistories(historyFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		Object hold = Dimensions.getValue(Dimensions.getPlayerFile(p, "Hold"), "Hold");
		if (hold!=null && (boolean) hold) {
			PortalFrame frame = portalClass.getFrameAtLocation(p.getLocation());
			if (frame!=null) {
				frame.addToHold(p);
			}
			Dimensions.saveValueAs(Dimensions.getPlayerFile(p, "Hold"), "Hold", false);
		}
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (portalClass.isOnHold(p)) {
			Dimensions.saveValueAs(Dimensions.getPlayerFile(p, "Hold"), "Hold", true);
		}
		
		for (PortalFrame frame : portalClass.getFrames()) {
			frame.remove(p);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSave(WorldSaveEvent e) {
		try {
			writeJSONLocations(locationsFile);
			writeJSONHistories(historyFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean reloadSettings() {
		
		File settings = new File("plugins/Dimensions/Settings.yml");
		YamlConfiguration portalSettings = YamlConfiguration.loadConfiguration(settings);
		
  	  	int debugLevel = portalSettings.getInt("DebugLevel");
  	  	int maxRadius = portalSettings.getInt("MaxRadius");
  	  	World defaultWorld = Bukkit.getWorld(portalSettings.getString("DefaultWorld"));
  	  	boolean portalParticles = portalSettings.getBoolean("EnableParticles");
  	  	boolean enableMobs = portalSettings.getBoolean("EnableMobsTeleportation");
  	  	int teleportDelay = portalSettings.getInt("TeleportDelay");
  	  	int searchRadius = portalSettings.getInt("SearchRadius");
  	  	int spotSearchRadius = portalSettings.getInt("SafeSpotSearchRadius");
  	  	boolean consumeItems = portalSettings.getBoolean("ConsumeItems");
  	  	boolean netherPortalEffect = portalSettings.getBoolean("NetherPortalEffect");
		
  	  	portalClass.setSettings(maxRadius, defaultWorld, portalParticles, enableMobs, teleportDelay, debugLevel, searchRadius, spotSearchRadius, consumeItems, netherPortalEffect);
  	  	
		return true;
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
			
			//Add strings added in new version that are missing and will crash plugin
			fixOutdatedPortalFile(portal);

			YamlConfiguration portalConfig = YamlConfiguration.loadConfiguration(portal);
			
			//Load portal settings
			String name = fileName.replace(".yml", "");
			
			boolean enabled = portalConfig.getBoolean("Enable");
			//if (!enabled) continue;
			String displayName = portalConfig.getString("DisplayName");
			
			Material material = Material.matchMaterial(portalConfig.getString("Portal.Block.Material"));
			String face = portalConfig.getString("Portal.Block.Face");
			Material frame = Material.matchMaterial(portalConfig.getString("Portal.Frame"));
			Material lighter = Material.matchMaterial(portalConfig.getString("Portal.Lighter"));
			int minPortalWidth = portalConfig.getInt("Portal.MinWidth");
			int minPortalHeight = portalConfig.getInt("Portal.MinHeight");
			
			String worldName = portalConfig.getString("World.Name");
			World world = Bukkit.getWorld(worldName);
			if (!Bukkit.getServer().getWorlds().contains(world)) {
				world = Bukkit.getServer().createWorld(new WorldCreator(worldName));
			}
			int worldHeight = portalConfig.getInt("World.MaxHeight");
			String ratio = portalConfig.getString("World.Ratio");
			
			HashMap<EntityType,EntityType> entityTransformation = new HashMap<EntityType,EntityType>();
			for (String entity : portalConfig.getStringList("Entities.Transformation")) {
				String[] spl = entity.toUpperCase().split("->");
				entityTransformation.put(EntityType.valueOf(spl[0]), EntityType.valueOf(spl[1]));
			}
			
			String[] spawningDelayString = portalConfig.getString("Entities.Spawning.Delay").split("-");
			int[] spawningDelay = {Integer.parseInt(spawningDelayString[0]),Integer.parseInt(spawningDelayString[1])};
			HashMap<EntityType,Integer> entitySpawning = new HashMap<EntityType,Integer>();
			for (String entity : portalConfig.getStringList("Entities.Spawning.List")) {
				String[] spl = entity.toUpperCase().split(";");
				entitySpawning.put(EntityType.valueOf(spl[0]), Integer.parseInt(spl[1]));
			}
			
			boolean buildExitPortal = portalConfig.getBoolean("BuildExitPortal");
			boolean spawnOnAir = portalConfig.getBoolean("SpawnOnAir");
			
			ArrayList<World> disabledWorlds = new ArrayList<World>();
			for (String disabledWorld : portalConfig.getStringList("DisabledWorlds")) {
				disabledWorlds.add(Bukkit.getWorld(disabledWorld));
			}
			
			String particlesColor = portalConfig.getString("Portal.ParticlesColor");
			
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
				oldPortal.update(name, enabled, displayName, material, face, frame, lighter, world, worldHeight, ratio, minPortalWidth, minPortalHeight, entityTransformation, spawningDelay, entitySpawning, buildExitPortal, spawnOnAir, disabledWorlds, particlesColor);
				oldPortals.remove(oldPortal);
			} else {
				createdPortals.add(new CustomPortal(portalClass, name, enabled, displayName, material, face, frame, lighter, world, worldHeight, ratio, minPortalWidth, minPortalHeight, entityTransformation, spawningDelay, entitySpawning, buildExitPortal, spawnOnAir, disabledWorlds, particlesColor, plugin));
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
		return reloadSettings() && reloadPortals() && Messages.reload();
	}

}
