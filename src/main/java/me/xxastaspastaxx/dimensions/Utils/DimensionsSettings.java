package me.xxastaspastaxx.dimensions.Utils;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class DimensionsSettings {
	
	static File settings;
	static YamlConfiguration portalSettings;
	
	static int debugLevel;
	static boolean generateWorlds;
	static int maxRadius;
	static World defaultWorld;
	static boolean enableParticles;
	static boolean enableMobs;
	static boolean enableEntities;
	static int teleportDelay;
	static int searchRadius;
	static int spotSearchRadius;
	static boolean consumeItems;
	static boolean netherPortalEffect;
	
	public static boolean reloadSettings() {
		
		settings = new File("plugins/Dimensions/Settings.yml");
		portalSettings = YamlConfiguration.loadConfiguration(settings);

  	  	debugLevel = portalSettings.getInt("Debug.Level");
  	  	generateWorlds = portalSettings.getBoolean("GenerateNewWorlds");
  	  	maxRadius = portalSettings.getInt("MaxRadius");
  	  	defaultWorld = Bukkit.getWorld(portalSettings.getString("DefaultWorld"));
  	  	enableParticles = portalSettings.getBoolean("EnableParticles");
  	  	enableMobs = portalSettings.getBoolean("EnableMobsTeleportation");
  	  	enableEntities = portalSettings.getBoolean("EnableNonLivingEntitiesTeleportation");
  	  	teleportDelay = portalSettings.getInt("TeleportDelay");
  	  	searchRadius = portalSettings.getInt("SearchRadius");
  	  	spotSearchRadius = portalSettings.getInt("SafeSpotSearchRadius");
  	  	consumeItems = portalSettings.getBoolean("ConsumeItems");
  	  	netherPortalEffect = portalSettings.getBoolean("NetherPortalEffect");
		
		return true;
	}

	public static YamlConfiguration getConf() {
		return portalSettings;
	}
	
	public static int getDebugLevel() {
		return debugLevel;
	}

	public static boolean isGenerateWorlds() {
		return generateWorlds;
	}

	public static int getMaxRadius() {
		return maxRadius;
	}

	public static World getDefaultWorld() {
		return defaultWorld;
	}

	public static boolean isEnableParticles() {
		return enableParticles;
	}

	public static boolean enableMobsTeleportation() {
		return enableMobs;
	}

	public static boolean enableEntitiesTeleportation() {
		return  enableMobs && enableEntities;
	}

	public static int getTeleportDelay() {
		return teleportDelay;
	}

	public static int getSearchRadius() {
		return searchRadius;
	}

	public static int getSpotSearchRadius() {
		return spotSearchRadius;
	}

	public static boolean consumeItems() {
		return consumeItems;
	}

	public static boolean enableNetherPortalEffect() {
		return netherPortalEffect;
	}
	
}
