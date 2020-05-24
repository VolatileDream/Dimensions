package me.xxastaspastaxx.dimensions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.portal.PortalFrame;

public class Dimensions {
	
	public static PortalClass portalClass;
	
	public static ArrayList<CustomPortal> getPortals() {
		return portalClass.getPortals();
	}
	
	public static ArrayList<Location> getPortalsInWorld(CustomPortal portal, World world) {
		return portalClass.getPortalsInWorld(portal,world);
	}
	
	public static boolean isPortal(Location loc, boolean load) {
		return portalClass.isPortal(loc,load);
	}
	
	public static boolean isPortalAtLocation(Location loc) {
		return portalClass.isPortalAtLocation(loc);
	}
	
	//get the portal
	public static CustomPortal getPortal(Location loc,boolean load) {
		return portalClass.getPortal(loc, load);
	}

	public static CustomPortal getPortalAtLocation(Location loc) {
		return portalClass.getPortalAtLocation(loc);
	}
	
	public static ArrayList<Location> getPortalLocations() {
		return portalClass.getPortalLocations();
	}
	
	public static void addLocation(CustomPortal portal, Location loc) {
		portalClass.addLocation(portal, loc);
	}
	
	public static void removeFrame(CustomPortal portal, PortalFrame frame) {
		portalClass.removeFrame(portal, frame);
	}
	
	public static void removeLocation(CustomPortal portal, Location loc) {
		portalClass.removeLocation(portal, loc);
	}
	
	public static int getMaxRadius() {
		return portalClass.getMaxRadius();
	}
	
	public static World getDefaultWorld() {
		return portalClass.getDefaultWorld();
	}
	
	public static CustomPortal getPortalFromName(String portalName) {
		return portalClass.getPortalFromName(portalName);
	}
	
	public static Location getNearestPortalLocation(CustomPortal portal, Location loc) {
		return portalClass.getNearestPortalLocation(portal, loc);
	}
	
	public static World getReturnWorld(Player p, CustomPortal portal) {
		return portalClass.getReturnWorld(p, portal);
	}

	public static void addToUsedPortals(Player p, CustomPortal portal) {
		portalClass.addToUsedPortals(p, portal);
	}
	
	public static boolean isNetherPortalEnabled() {
		return portalClass.isNetherPortalEnabled();
	}
	
	public static File getPortalFile(CustomPortal portal) {
		return new File("plugins/Dimensions/Portals/"+portal.getName()+".yml");
	}
	
	public static File getPlayerFile(Player p, String file) {
		return new File("plugins/Dimensions/PlayerData/"+p.getName()+"/"+file+".yml");
	}
	
	public static void saveValueAs(File file, String data, Object value) {
	  	YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
	  	
	  	config.set(data, value);
	  	
	  	try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object getValue(File file, String data) {
	  	YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
	  	
	  	return config.get(data);
	}
	
	public static int getRandom(int min, int max) {
		return (int)(Math.random()*((max-min)+1))+min;
	}
}
