package me.xxastaspastaxx.dimensions.fileHandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.portal.listeners.PortalListeners;

public class PortalFiles {

	PortalClass portalClass;
	PortalListeners portalListeners;
	
	PortalLocations portalLocations;
	LocationsFile locationsFile;
	
	Plugin plugin;
	
	public PortalFiles(Plugin pl) {
		
		plugin = pl;
	  	
		//Create general settings config file
		File settings = new File("plugins/Dimensions/Settings.yml");
		YamlConfiguration portalSettings = YamlConfiguration.loadConfiguration(settings);
		

		portalSettings.addDefault("MaxRadius", 10);
		portalSettings.addDefault("DefaultWorld", "world");
		portalSettings.addDefault("EnableParticles", false);
		portalSettings.addDefault("TeleportDelay", 4);
		
		
		portalSettings.options().copyDefaults(true);
  	  	
  	  	try {
  	  		portalSettings.save(settings);
  	  	} catch (IOException e) {
			e.printStackTrace();
		}
  	  	
  	  	int maxRadius = portalSettings.getInt("MaxRadius");
  	  	World defaultWorld = Bukkit.getWorld(portalSettings.getString("DefaultWorld"));
  	  	boolean portalParticles = portalSettings.getBoolean("EnableParticles");
  	  	int teleportDelay = portalSettings.getInt("TeleportDelay");
  	  	
	  	portalClass = new PortalClass(pl, maxRadius, defaultWorld, portalParticles, teleportDelay);
	  	
	  	ArrayList<CustomPortal> createdPortals = new ArrayList<CustomPortal>();
	  	ArrayList<Material> lighters = new ArrayList<Material>();
	  	ArrayList<Material> frameMaterials = new ArrayList<Material>();
	  	ArrayList<Material> blocks = new ArrayList<Material>();
	  	
		//Create and register all portals
		File portalFolder = new File("plugins/Dimensions/Portals");
		if (!portalFolder.exists()) portalFolder.mkdir();
		File[] portals = portalFolder.listFiles();
		for (File portal : portals) {
			if (portal.getName().contentEquals("portalLocations.json") || portal.getName().contains(" ")) continue;
			
			YamlConfiguration portalConfig = YamlConfiguration.loadConfiguration(portal);
			
			//Add strings added in new version that are missing and will crash plugin
			fixOutdatedPortalFile(portal);
			
			//Load portal settings
			String name = portal.getName().replace(".yml", "");
			
			boolean enabled = portalConfig.getBoolean("Enable");
			String displayName = portalConfig.getString("DisplayName");
			
			Material material = Material.matchMaterial(portalConfig.getString("Portal.Block.Material"));
			String face = portalConfig.getString("Portal.Block.Face");
			Material frame = Material.matchMaterial(portalConfig.getString("Portal.Frame"));
			Material lighter = Material.matchMaterial(portalConfig.getString("Portal.Lighter"));
			
			String worldName = portalConfig.getString("World");
			World world = Bukkit.getWorld(worldName);
			if (!Bukkit.getServer().getWorlds().contains(world)) {
				world = Bukkit.getServer().createWorld(new WorldCreator(worldName));
			}
			
			String ratio = portalConfig.getString("Ratio");
			
			int minPortalWidth = portalConfig.getInt("MinPortalWidth");
			int minPortalHeight = portalConfig.getInt("MinPortalHeight");
			
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
			createdPortals.add(new CustomPortal(portalClass, name, enabled, displayName, material, face, frame, lighter, world, ratio, minPortalWidth, minPortalHeight, buildExitPortal, spawnOnAir, disabledWorlds, particlesColor, pl));
		}
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			//This is used to return players to the previous world
			File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
			YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);
			
			//Portal name
	          List<String> lup = lastPortalConfig.getStringList("LastUsedPortal");
	          lastPortalConfig.set("LastUsedPortal", lup);

	        //Worlds name
	          List<String> luw = lastPortalConfig.getStringList("LastUsedWorld");
	          lastPortalConfig.set("LastUsedWorld", luw);

			try {
				lastPortalConfig.save(lastPortalFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		portalClass.setPortals(createdPortals);
		portalListeners = new PortalListeners(pl, portalClass, lighters, frameMaterials, blocks);
		
		try {
			createJSON(false);
			LocationsFile locationsFile = readJSON();
			while (locationsFile==null) {
				createJSON(true);
				locationsFile = readJSON();
			}
			PortalLocations portalLocations = new PortalLocations(portalClass,locationsFile);
			
			portalLocations.convertStrings(locationsFile.getLocations());
	        
	        this.portalLocations = portalLocations;
	        this.locationsFile = locationsFile;
	        portalClass.setPortalLocations(portalLocations);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        /*=============================================================*/
	}
	
	public void fixOutdatedPortalFile(File portal) {
		YamlConfiguration portalConfig = YamlConfiguration.loadConfiguration(portal);
		
		portalConfig.addDefault("Enable", true);
		portalConfig.addDefault("DisplayName", "&5Nether portal");

  	  	portalConfig.addDefault("Portal.Block.Material", "obsidian");
  	  	portalConfig.addDefault("Portal.Block.Face", "all");
  	  	portalConfig.addDefault("Portal.ParticlesColor", "75;0;130");
  	  	portalConfig.addDefault("Portal.Frame", "nether_portal");
  	  	portalConfig.addDefault("Portal.Lighter", "flint_and_steel");
  	  	portalConfig.addDefault("World", "world_nether");
  	  	portalConfig.addDefault("Ratio", "1:8");

  	  	portalConfig.addDefault("MinPortalWidth", 4);
  	  	portalConfig.addDefault("MinPortalHeight", 5);
  	  	
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
	
	private void createJSON(boolean recreate) {
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
	
	private void writeJSON(PortalLocations portalLocations, LocationsFile locationsFile) throws IOException { 
		GsonBuilder builder = new GsonBuilder(); 
		Gson gson = builder.create(); 
		FileWriter writer = new FileWriter("plugins/Dimensions/Portals/portalLocations.json");
		portalLocations.save();
		writer.write(gson.toJson(locationsFile));   
		writer.close(); 
	}  
	   
	private LocationsFile readJSON() throws FileNotFoundException { 
		GsonBuilder builder = new GsonBuilder(); 
		Gson gson = builder.create(); 
		BufferedReader bufferedReader = new BufferedReader(new FileReader("plugins/Dimensions/Portals/portalLocations.json"));   

		LocationsFile locationsFile = gson.fromJson(bufferedReader, LocationsFile.class); 
		return locationsFile; 
	} 
	
	public PortalClass getPortalClass() {
		return portalClass;
	}
	
	public void onDisable() {
		try {
			writeJSON(portalLocations, locationsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Location loc : portalClass.getPortalLocations()) {
			loc.getChunk().load();
			CustomPortal portal = portalClass.getPortalAtLocation(loc);
			if (portal.getMaterial()==Material.WATER || portal.getMaterial()==Material.FIRE) {
				loc.getBlock().setType(Material.AIR);
			} else {
				for (Entity en : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
	        		if (!(en instanceof FallingBlock)) continue;
	        		FallingBlock fallingBlock = (FallingBlock) en;
	        		
	            	if (fallingBlock.getBlockData().getMaterial()==portal.getFrame()) {
	            		en.remove();
	            	}
	        	}
			}
		}
	}
}
