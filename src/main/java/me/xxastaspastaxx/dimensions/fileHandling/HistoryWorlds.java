package me.xxastaspastaxx.dimensions.fileHandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class HistoryWorlds {
	
	private HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> histories = new HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>>();
		
	private PortalClass portalClass;
	private HistoryFile historyFile;
	
	public HistoryWorlds(PortalClass portalClass, HistoryFile historyFile) {
		this.historyFile = historyFile;
		this.portalClass = portalClass;
	}
	
	public HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> getHistories() { 
		return histories;
	}
	
	public HashMap<UUID, ArrayList<World>> getHistories(CustomPortal portal) { 
		if (!histories.containsKey(portal)) return new HashMap<UUID, ArrayList<World>>();
		return histories.get(portal);
	}
	   
	public void setHistories(HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> histories) { 
		this.histories = histories; 
	}
	
	public void setHistories(CustomPortal portal, HashMap<UUID, ArrayList<World>> histories) { 
		this.histories.put(portal, histories);
	}
	
	public void removePortal(CustomPortal portal) {
		if (!histories.containsKey(portal)) return;
		histories.remove(portal);
		historyFile.removePortal(portal.getName());
	}
	   
	public String toString() { 
		return histories.toString(); 
	}
	
	public void update() {
		for (CustomPortal portal : histories.keySet()) {
			histories.put(portal, portal.getHistory());
		}
	}
	
	public void save() {
		update();
		historyFile.save(histories);
	}
	
	public void convertStrings(HashMap<String, HashMap<String, ArrayList<String>>> loadHistories) {
		for (String portalName : loadHistories.keySet()) {
			HashMap<UUID, ArrayList<World>> historiesString = new HashMap<UUID, ArrayList<World>>();
			for (String entityID : loadHistories.get(portalName).keySet()) {
				ArrayList<World> worlds = new ArrayList<World>();
				for (String worldString : loadHistories.get(portalName).get(entityID)) {
					World world = Bukkit.getWorld(worldString);
					if (world!=null) {
						worlds.add(world);
					}
				}
				historiesString.put(UUID.fromString(entityID), worlds);
			}
			histories.put(portalClass.getPortalFromName(portalName), historiesString);
		}
		
		for (CustomPortal portal : portalClass.getPortals()) {
			if (!histories.containsKey(portal)) histories.put(portal, new HashMap<UUID, ArrayList<World>>());
		}
	}

	public HistoryFile getHistoryFiles() {
		return historyFile;
	}

}
