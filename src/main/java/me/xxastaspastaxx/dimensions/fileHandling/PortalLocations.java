package me.xxastaspastaxx.dimensions.fileHandling;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class PortalLocations {
	
	private HashMap<CustomPortal, HashMap<World, ArrayList<Location>>> locations = new HashMap<CustomPortal, HashMap<World, ArrayList<Location>>>();
		
	private PortalClass portalClass;
	private LocationsFile locationFile;
	
	public PortalLocations(PortalClass portalClass, LocationsFile locationFile) {
		this.locationFile = locationFile;
		this.portalClass = portalClass;
	}
	
	public HashMap<CustomPortal, HashMap<World, ArrayList<Location>>> getLocations() { 
		return locations;
	}
	
	public ArrayList<Location> getAllLocations() {
		ArrayList<Location> allLocations = new ArrayList<Location>();
		for (CustomPortal portal : locations.keySet()) {
			for (World world : locations.get(portal).keySet()) {
				for (Location location : locations.get(portal).get(world)) {
					allLocations.add(location);
				}
			}
		}
		return allLocations;
	}
	
	public HashMap<World, ArrayList<Location>> getLocations(CustomPortal portal) { 
		if (!locations.containsKey(portal)) return new HashMap<World, ArrayList<Location>>();
		return locations.get(portal);
	}
	
	public ArrayList<Location> getLocations(CustomPortal portal, World world) {
		if (!locations.containsKey(portal) || !locations.get(portal).containsKey(world)) return new ArrayList<Location>();
		return locations.get(portal).get(world);
	}
	   
	public void setLocations(HashMap<CustomPortal, HashMap<World, ArrayList<Location>>> locations) { 
		this.locations = locations; 
	}
	
	public void removePortal(CustomPortal portal) {
		if (!locations.containsKey(portal)) return;
		locations.remove(portal);
		locationFile.removePortal(portal.getName());
	}
	   
	public String toString() { 
		return locations.toString(); 
	}
	
	public void removeLocation(CustomPortal portal, Location loc) {
		ArrayList<Location> newLocations = new ArrayList<Location>();
		if (!locations.containsKey(portal)) return;
		if (!locations.get(portal).containsKey(loc.getWorld())) return;
		
		for (Location location : locations.get(portal).get(loc.getWorld())) {
			if (!loc.equals(location)) {
				newLocations.add(location);
			}
		}
		locations.get(portal).put(loc.getWorld(), newLocations);
	}
	
	public void addLocation(CustomPortal portal, Location loc) {
		if (!locations.containsKey(portal)) locations.put(portal, new HashMap<World,ArrayList<Location>>());
		if (!locations.get(portal).containsKey(loc.getWorld())) locations.get(portal).put(loc.getWorld(), new ArrayList<Location>());
		if (locations.get(portal).get(loc.getWorld()).contains(loc)) return;
		locations.get(portal).get(loc.getWorld()).add(loc);
	}
	
	public CustomPortal getPortal(Location loc) {
		if (loc.getBlock().getType().isSolid()) return null;
		
        for (CustomPortal portal : locations.keySet()) {
			for (World world : locations.get(portal).keySet()) {
				for (Location location : locations.get(portal).get(world)) {
					if (loc.equals(location)) {
						return portal;
					}
				}
			}
		}
        
		return null;
	}
	
	public void save() {
		locationFile.save(locations);
	}
	
	public void convertStrings(HashMap<String, HashMap<String, ArrayList<String>>> loadLocations) {
		for (String portalName : loadLocations.keySet()) {
			HashMap<World, ArrayList<Location>> worlds = new HashMap<World, ArrayList<Location>>();
			for (String worldName : loadLocations.get(portalName).keySet()) {
				ArrayList<Location> locations = new ArrayList<Location>();
				World world = Bukkit.getWorld(worldName);
				for (String locationString : loadLocations.get(portalName).get(worldName)) {
					String[] cords = locationString.split(",");
					int x = Integer.parseInt(cords[0]);
					int y = Integer.parseInt(cords[1]);
					int z = Integer.parseInt(cords[2]);
					locations.add(new Location(world,x,y,z));
				}
				worlds.put(world, locations);
			}
			locations.put(portalClass.getPortalFromName(portalName), worlds);
		}
	}

}
