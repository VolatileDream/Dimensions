package me.xxastaspastaxx.dimensions.fileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalFrame;

public class PortalLocations {
	
	private ArrayList<CompletePortal> locations = new ArrayList<CompletePortal>();
	
	private final String filePath = "./plugins/Dimensions/Portals/portalLocations.json";
	
	public PortalLocations() {
		
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			save();
		}
		
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(filePath));
	        JSONArray jsonArray = (JSONArray) jsonObject.get("loadLocations");
	        @SuppressWarnings("unchecked")
			Iterator<String> iterator = jsonArray.iterator();
	        while(iterator.hasNext()) {
	        	locations.add(CompletePortal.parseCompletePortal(iterator.next()));
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
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
	}
	
	public void removePortal(CustomPortal portal) {
		Iterator<CompletePortal> iter = locations.iterator();
		while (iter.hasNext()) {
			CompletePortal complete = iter.next();
			if (!complete.getPortal().equals(portal)) continue;
			locations.remove(complete);
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

		
		ArrayList<String> res = new ArrayList<String>();
		for (CompletePortal complete : locations) {
			res.add(complete.toString());
		}
		try{
		    PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		    writer.println("{\"loadLocations\":"+new Gson().toJson(res)+"}");
		    writer.close();
		} catch (IOException e) {
		}
	}
	
	/*public void convertStrings(ArrayList<String> loadLocations) {
		for (String portalString : loadLocations) {
			locations.add(CompletePortal.parseCompletePortal(portalString));
		}
	}*/

}
