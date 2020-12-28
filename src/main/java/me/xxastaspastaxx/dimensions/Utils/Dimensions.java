package me.xxastaspastaxx.dimensions.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.flags.StateFlag;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.portal.PortalFrame;

public class Dimensions {
	
	public static PortalClass portalClass;
	
	
	public static ArrayList<CustomPortal> getPortals() {
		return portalClass.getPortals();
	}
	
	public static ArrayList<Material> getLighters() {
		return portalClass.getLighters();
	}
	
	public static ArrayList<Material> getFrameMaterials() {
		return portalClass.getFrameMaterials();
	}
	
	public static ArrayList<Material> getBlocks() {
		return portalClass.getBlocks();
	}
	
	public static boolean lightPortal(Location loc, IgniteCause cause, LivingEntity entity, ItemStack lighter) {
		return portalClass.lightPortal(loc, cause, entity, lighter);
	}
	
	public static ArrayList<CompletePortal> getPortalsInWorld(CustomPortal portal, World world) {
		return portalClass.getPortalsInWorld(portal,world);
	}
	
	public static boolean isPortal(Location loc, boolean load) {
		return portalClass.isPortal(loc,load);
	}
	
	public static boolean isPortalAtLocation(Location loc) {
		return portalClass.isPortalAtLocation(loc);
	}
	
	public static void addToHold(Entity p) {
		portalClass.addToHold(p);
	}
	
	//get the portal
	public static CustomPortal getPortal(Location loc,boolean load) {
		return portalClass.getPortal(loc, load);
	}

	public static CompletePortal getPortalAtLocation(Location loc) {
		return portalClass.getPortalAtLocation(loc);
	}
	
	public static PortalFrame getFrameAtLocation(Location loc) {
		return portalClass.getFrameAtLocation(loc);
	}
	
	public static ArrayList<Location> getPortalLocations() {
		return portalClass.getPortalLocations();
	}
	
	public static void addPortal(CompletePortal complete) {
		portalClass.addPortal(complete);
	}
	
	public static void removePortal(CustomPortal portal) {
		portalClass.removePortal(portal);
	}
	
	public static void removeCompletePortal(CompletePortal complete) {
		portalClass.removeCompletePortal(complete, true);
	}
	
	public static CustomPortal getPortalFromName(String portalName) {
		return portalClass.getPortalFromName(portalName);
	}
	
	public static Location getNearestPortalLocation(CustomPortal portal, Location loc) {
		return portalClass.getNearestPortalLocation(portal, loc);
	}
	
	public static boolean isNetherPortalEnabled() {
		return portalClass.isNetherPortalEnabled();
	}
	
	public static File getPortalFile(CustomPortal portal) {
		return new File("plugins/Dimensions/Portals/"+portal.getName()+".yml");
	}
	
	public static YamlConfiguration getPortalConfiguration(CustomPortal portal) {
		return YamlConfiguration.loadConfiguration(getPortalFile(portal));
	}
	
	public static void saveValueAs(UUID uuid, String data, Object value) {
		portalClass.setData(uuid, data, value);
	}
	
	public static Object getValue(UUID uuid, String data) {
	  	return portalClass.getData(uuid, data);
	}
	
	public static int getRandom(int min, int max) {
		return (int)(Math.random()*((max-min)+1))+min;
	}
	
	public static boolean isAir(Material mat) {
		return (mat==Material.AIR || mat==Material.CAVE_AIR);
	}
	
	public static ArrayList<CompletePortal> getNearbyPortals(Location loc, int radius) {
		return portalClass.getNearbyPortals(loc, radius);
	}
	
	public static ArrayList<CompletePortal> getNearbyPortals(CustomPortal portal, Location loc, int radius) {
		return portalClass.getNearbyPortals(portal, loc, radius);
	}
	
	public static boolean isWorldGuardEnabled() {
		return portalClass.getPlugin().getWorldGuardFlags()!=null;
	}
	
	public static boolean testState(Player p, Location loc, StateFlag flag) {
		return isWorldGuardEnabled() && portalClass.getPlugin().getWorldGuardFlags().testState(p, loc, flag);
	}
	
	public static ArrayList<CompletePortal> getPortalsVisibleFromPlayer(Player p) {
		return portalClass.getPortalsVisibleFromPlayer(p);
	}
	
	public static boolean isOnHold(Entity en) {
		return portalClass.isOnHold(en);
	}
	
	public static void debug(String msg, int lvl) {
		portalClass.debug(msg, lvl);
	}
	
	public static CompletePortal getLookingPortal(LivingEntity e) {
		return portalClass.getLookingPortal(e);
	}
}
