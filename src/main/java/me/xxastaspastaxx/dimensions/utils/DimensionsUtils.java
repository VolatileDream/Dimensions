package me.xxastaspastaxx.dimensions.utils;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class DimensionsUtils {
	
	public static File getPortalFile(CustomPortal portal) {
		return new File("plugins/Dimensions/Portals/"+portal.getName()+".yml");
	}
	
	public static YamlConfiguration getPortalConfiguration(CustomPortal portal) {
		return YamlConfiguration.loadConfiguration(getPortalFile(portal));
	}
	
	public static int getRandom(int min, int max) {
		return (int)(Math.random()*((max-min)+1))+min;
	}
	
	public static boolean isAir(Material mat) {
		return (mat==Material.AIR || mat==Material.CAVE_AIR);
	}
	
	public static boolean isWorldGuardEnabled() {
		return Dimensions.getWorldGuardFlags()!=null;
	}
	
	public static boolean testState(Player p, Location loc, StateFlag flag) {
		return isWorldGuardEnabled() && Dimensions.getWorldGuardFlags().testState(p, loc, flag);
	}
	
}