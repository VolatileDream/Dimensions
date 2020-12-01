package me.xxastaspastaxx.dimensions.fileHandling;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalFrame;

public class PortalLocations {
	
	private ArrayList<CompletePortal> locations = new ArrayList<CompletePortal>();
		
	private LocationsFile locationFile;
	
	public PortalLocations(LocationsFile locationFile) {
		this.locationFile = locationFile;
	}
	
	public ArrayList<CompletePortal> getPortals() { 
		return locations;
	}
	
	public ArrayList<Location> getLocations() {
		ArrayList<Location> allLocations = new ArrayList<Location>();
		for (CompletePortal complete : locations) {
			for (PortalFrame frame : complete.getFrames()) {
				allLocations.add(frame.getLocation());
			}
		}
		return allLocations;
	}
	
	public ArrayList<Location> getLocations(CustomPortal portal) {
		ArrayList<Location> allLocations = new ArrayList<Location>();
		for (CompletePortal complete : locations) {
			if (!complete.getPortal().equals(portal)) continue;
			for (PortalFrame frame : complete.getFrames()) {
				allLocations.add(frame.getLocation());
			}
		}
		return allLocations;
	}
	
	public ArrayList<Location> getLocations(CustomPortal portal, World world) {
		ArrayList<Location> allLocations = new ArrayList<Location>();
		for (CompletePortal complete : locations) {
			if (!complete.getPortal().equals(portal)) continue;
			for (PortalFrame frame : complete.getFrames()) {
				if (!frame.getLocation().getWorld().equals(world)) break;
				allLocations.add(frame.getLocation());
			}
		}
		return allLocations;
	}
	
	public ArrayList<CompletePortal> getPortals(CustomPortal portal, World world) {
		ArrayList<CompletePortal> allLocations = new ArrayList<CompletePortal>();
		for (CompletePortal complete : locations) {
			if (complete.getPortal().equals(portal) && complete.getLocation().getWorld().equals(world)) allLocations.add(complete);
		}
		return allLocations;
	}
	   
	public void setLocations(ArrayList<CompletePortal> locations) { 
		this.locations = locations; 
	}
	
	public void removePortal(CompletePortal complete) {
		locations.remove(complete);
		locationFile.removePortal(complete.toString());
	}
	
	public void removePortal(CustomPortal portal) {
		Iterator<CompletePortal> iter = locations.iterator();
		while (iter.hasNext()) {
			CompletePortal complete = iter.next();
			if (!complete.getPortal().equals(portal)) continue;
			locations.remove(complete);
			locationFile.removePortal(portal.getName());
		}
	}
	   
	public String toString() { 
		return locations.toString(); 
	}
	
	public void addPortal(CompletePortal complete) {
		locations.add(complete);
	}
	
	public CompletePortal getPortal(Location loc) {
		if (loc.getBlock().getType()!=Material.AIR) return null;
		
        for (CompletePortal complete : locations) {
        	for (PortalFrame frame : complete.getFrames()) {
				if (loc.equals(frame.getLocation())) {
					return complete;
				}
			}
		}
        
		return null;
	}
	
	public void save() {
		locationFile.save(locations);
	}
	
	public void convertStrings(ArrayList<String> loadLocations) {
		for (String portalString : loadLocations) {
			locations.add(CompletePortal.parseCompletePortal(portalString));
		}
	}

}
