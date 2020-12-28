package me.xxastaspastaxx.dimensions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import me.xxastaspastaxx.dimensions.commands.DimensionsCommands;
import me.xxastaspastaxx.dimensions.files.Files;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.WorldGuardFlags;
import me.xxastaspastaxx.dimensions.utils.DimensionsExpansion;
import me.xxastaspastaxx.dimensions.utils.Messages;
import me.xxastaspastaxx.dimensions.utils.Metrics;
	
public class Main extends JavaPlugin {
	
	public Files files;	

	WorldGuardFlags worldGuardFlags;
	
	public void onEnable() {
		
		instance = this;
		
		try {

		  	Messages.load();
			
			//Config files setup
			files = new Files(this);
	    
			//World guard
			if (worldGuardFlags!=null) {
				worldGuardFlags.enablePlatform();
			}
			
	        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
	              new DimensionsExpansion(this).register();
	        }
			
			//Commands setup
			this.getCommand("dimensions").setExecutor(new DimensionsCommands(this,files.portalFiles.getPortalClass()));
			
			
	        int pluginId = 6978;
	        Metrics metrics = new Metrics(this, pluginId);
			
	        metrics.addCustomChart(new Metrics.MultiLineChart("players_and_servers", new Callable<Map<String, Integer>>() {
	            @Override
	            public Map<String, Integer> call() throws Exception {
	                Map<String, Integer> valueMap = new HashMap<>();
	                valueMap.put("servers", 1);
	                valueMap.put("players", Bukkit.getOnlinePlayers().size());
	                return valueMap;
	            }
	        }));
	        
	        metrics.addCustomChart(new Metrics.DrilldownPie("portal_blocks_frames", () -> {
	            Map<String, Map<String, Integer>> map = new HashMap<>();
	            for (CustomPortal portal : files.portalFiles.getPortalClass().getPortals()) {
	                Map<String, Integer> entry = new HashMap<>();
	                entry.put(portal.getFrame().toString(),1);
	                map.put(portal.getMaterial().toString(), entry);
	            }
	            return map;
	        }));
	        
	        metrics.addCustomChart(new Metrics.DrilldownPie("portal_blocks_lighters", () -> {
	            Map<String, Map<String, Integer>> map = new HashMap<>();
	            for (CustomPortal portal : files.portalFiles.getPortalClass().getPortals()) {
	                Map<String, Integer> entry = new HashMap<>();
	                entry.put(portal.getLighter().toString(),1);
	                map.put(portal.getMaterial().toString(), entry);
	            }
	            return map;
	        }));
        
		} catch (Exception e) {
			System.out.println("[DimensionsDebugger] There was an error with Dimensions. The plugin has been disabled. More info:");
			e.printStackTrace();
			getPluginLoader().disablePlugin(this);
		}
	}
	
	public void onLoad() {
	    Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
	    if (worldGuardPlugin!=null && worldGuardPlugin instanceof WorldGuardPlugin) {
			worldGuardFlags = new WorldGuardFlags();
	    }
	}
	
	public WorldGuardFlags getWorldGuardFlags() {
		return worldGuardFlags;
	}
	
	public void onDisable() {
		if (files!=null) files.onDisable();
	}
	
	private static Main instance;
	public static Main getInstance() {
	  return instance;
	}
	
}

	
