package me.xxastaspastaxx.dimensions.fileHandling;

import java.util.ArrayList;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;

public class LocationsFile {
	private ArrayList<String> loadLocations = new ArrayList<String>();

	public LocationsFile() {
	}
	
	public ArrayList<String> getLocations() { 
		return loadLocations; 
	}
	   
	public void setLocations(ArrayList<String> loadLocations) { 
		this.loadLocations = loadLocations; 
	}
	   
	public String toString() { 
		return loadLocations.toString(); 
	}
	
	public void save(ArrayList<CompletePortal> locations) {
		loadLocations.clear();
		
		for (CompletePortal complete : locations) {
			loadLocations.add(complete.toString());
		}
        
	}

	public void removePortal(String name) {
		loadLocations.remove(name);
	}
	
}
