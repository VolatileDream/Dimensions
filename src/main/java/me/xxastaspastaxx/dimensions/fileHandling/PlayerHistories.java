package me.xxastaspastaxx.dimensions.fileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class PlayerHistories {

	private HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> histories = new HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>>();
	
	private final String filePath = "./plugins/Dimensions/PlayerData/playerHistories.json";
	
	@SuppressWarnings("unchecked")
	public PlayerHistories(PortalClass portalClass) {
		
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
			JSONObject historiesObject = (JSONObject) jsonObject.get("loadHistories");
			
	        historiesObject.keySet().forEach(portalName ->
	        {
	            Object keyvalue = historiesObject.get(portalName);
	        	HashMap<UUID, ArrayList<World>> historiesString = new HashMap<UUID, ArrayList<World>>();

	            if (keyvalue instanceof JSONObject) {
	            	((JSONObject) keyvalue).keySet().forEach(uuid ->
	    	        {
		            	ArrayList<World> worlds = new ArrayList<World>();
		            	
	    	            Object list = ((JSONObject) keyvalue).get(uuid);
	    	            if (list instanceof JSONObject) {
	    	            	((JSONObject) list).keySet().forEach(world ->
	    	    	        {
	    	    	            worlds.add(Bukkit.getWorld(world.toString()));
	    	    	        });
	    	            }

		            	historiesString.put(UUID.fromString(uuid.toString()), worlds);
	    	        });
	            	
	            }
				histories.put(portalClass.getPortalFromName(portalName.toString()), historiesString);
	        });
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		for (CustomPortal portal : portalClass.getPortals()) {
			if (!histories.containsKey(portal)) histories.put(portal, new HashMap<UUID, ArrayList<World>>());
		}
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
		
		HashMap<String, HashMap<String, ArrayList<String>>> res = new HashMap<String, HashMap<String, ArrayList<String>>>();
		for (CustomPortal portal : histories.keySet()) {
			HashMap<String, ArrayList<String>> worldHistories = new HashMap<String, ArrayList<String>>();
			for (UUID uuid : histories.get(portal).keySet()) {
				ArrayList<String> worlds = new ArrayList<String>();
				for (World world : histories.get(portal).get(uuid)) {
					worlds.add(world.getName());
				}
				worldHistories.put(uuid.toString(), worlds);
			}
			res.put(portal.getName(), worldHistories);
		}
		
		try{
		    PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		    writer.println("{\"loadHistories\":"+new Gson().toJson(res)+"}");
		    writer.close();
		} catch (IOException e) {
		}
	}


}
