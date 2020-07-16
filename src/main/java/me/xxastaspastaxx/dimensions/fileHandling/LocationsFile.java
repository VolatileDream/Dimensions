package me.xxastaspastaxx.dimensions.fileHandling;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class LocationsFile {
	private HashMap<String, HashMap<String, ArrayList<String>>> loadLocations = new HashMap<String, HashMap<String, ArrayList<String>>>();

	public LocationsFile() {
	}
	
	public HashMap<String, HashMap<String, ArrayList<String>>> getLocations() { 
		return loadLocations; 
	}
	   
	public void setLocations(HashMap<String, HashMap<String, ArrayList<String>>> loadLocations) { 
		this.loadLocations = loadLocations; 
	}
	   
	public String toString() { 
		return loadLocations.toString(); 
	}
	
	public void save(HashMap<CustomPortal, HashMap<World, ArrayList<Location>>> locations) {
		loadLocations.clear();
		
        for (CustomPortal portal : locations.keySet()) {
			HashMap<String, ArrayList<String>> worlds = new HashMap<String, ArrayList<String>>();
			for (World world : locations.get(portal).keySet()) {
				ArrayList<String> locationStrings = new ArrayList<String>();
				for (Location location : locations.get(portal).get(world)) {
					locationStrings.add(location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ());
				}
				if (Bukkit.getWorlds().contains(world)) {
					worlds.put(world.getName(), locationStrings);
				}
			}
			loadLocations.put(portal.getName(), worlds);
		}
        
	}

	public void removePortal(String name) {
		if (!loadLocations.containsKey(name)) return;
		loadLocations.remove(name);
	}
	
}
