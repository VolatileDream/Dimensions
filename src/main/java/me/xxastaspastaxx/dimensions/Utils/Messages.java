package me.xxastaspastaxx.dimensions.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {
	
	public static HashMap<String, String> messages = new HashMap<String, String>();
	
	public static void load() {
		File messages = new File("plugins/Dimensions/Messages.yml");
		YamlConfiguration portalMessages = YamlConfiguration.loadConfiguration(messages);
		

		portalMessages.addDefault("Prefix", "§7[§cDimensions§7]");
		portalMessages.addDefault("WorldGuardDenyMessage", "&c&lHey, &7sorry but you can't do that here");
		portalMessages.addDefault("MaxHeightExceededDenyMessage", "%prefix% &cYou cannot build a portal above Y %maxWorldHeight%.");
		portalMessages.addDefault("DisabledWorldMessage", "%prefix% &cYou cannot build a portal in %world% because its disabled.");
		
		
		portalMessages.options().copyDefaults(true);
  	  	
  	  	try {
  	  	portalMessages.save(messages);
  	  	} catch (IOException e) {
			e.printStackTrace();
		}
  	  	
  	  	reload();
	}
	
	public static boolean reload() {
		File settings = new File("plugins/Dimensions/Messages.yml");
		YamlConfiguration portalSettings = YamlConfiguration.loadConfiguration(settings);

  	  	String prefix = portalSettings.getString("Prefix").replace("&", "§");
  	  	messages.put("prefix", prefix);
  	  	messages.put("worldGuardDenyMessage", portalSettings.getString("WorldGuardDenyMessage").replace("&", "§").replace("%prefix%", prefix));
  	  	messages.put("maxHeightExceededDenyMessage", portalSettings.getString("MaxHeightExceededDenyMessage").replace("&", "§").replace("%prefix%", prefix));
  	  	messages.put("disabledWorldMesasge", portalSettings.getString("DisabledWorldMessage").replace("&", "§").replace("%prefix%", prefix));
  	  	
  	  	return true;
	}
	
	public static String get(String key) {
		
		return messages.get(key);
	}
}
