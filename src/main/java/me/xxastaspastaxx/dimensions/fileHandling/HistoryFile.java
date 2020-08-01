package me.xxastaspastaxx.dimensions.fileHandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class HistoryFile {
	private HashMap<String, HashMap<String, ArrayList<String>>> loadHistories = new HashMap<String, HashMap<String, ArrayList<String>>>();

	public HistoryFile() {
	}
	
	public HashMap<String, HashMap<String, ArrayList<String>>> getHistories() { 
		return loadHistories; 
	}
	   
	public void setLocations(HashMap<String, HashMap<String, ArrayList<String>>> loadHistories) { 
		this.loadHistories = loadHistories; 
	}
	   
	public String toString() { 
		return loadHistories.toString(); 
	}
	
	public void save(HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> histories) {
		loadHistories.clear();
		
        for (CustomPortal portal : histories.keySet()) {
			HashMap<String, ArrayList<String>> worlds = new HashMap<String, ArrayList<String>>();
			for (UUID uuid : histories.get(portal).keySet()) {
				ArrayList<String> historiesStrings = new ArrayList<String>();
				for (World world : histories.get(portal).get(uuid)) {
					if (Bukkit.getWorlds().contains(world)) {
						historiesStrings.add(world.getName());
					}
				}
				worlds.put(uuid.toString(), historiesStrings);
			}
			loadHistories.put(portal.getName(), worlds);
		}
        
	}

	public void removePortal(String name) {
		if (!loadHistories.containsKey(name)) return;
		loadHistories.remove(name);
	}
	
}
