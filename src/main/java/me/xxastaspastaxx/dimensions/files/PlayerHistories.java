package me.xxastaspastaxx.dimensions.files;

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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class PlayerHistories {

	private static final double version = 1.0;
	
	private HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> histories = new HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>>();
	
	private final String filePath = "./plugins/Dimensions/PlayerData/playerHistories.json";

	private Gson gson;
	
	public PlayerHistories(PortalClass portalClass, double fileVesrion) {
		
		gson = new Gson();
		
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			save();
		}

		if (fileVesrion!=version) {
			
		} else {
			HashMap<String, HashMap<String, ArrayList<String>>> res = new HashMap<String, HashMap<String, ArrayList<String>>>();
			try {
				res = gson.fromJson(new FileReader(filePath), new TypeToken<HashMap<String, HashMap<String, ArrayList<String>>>>() {}.getType());
			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
				e.printStackTrace();
			}
			
			for (String portalString : res.keySet()) {
				CustomPortal portal = portalClass.getPortalFromName(portalString);
				HashMap<UUID, ArrayList<World>> worldHistories = new HashMap<UUID, ArrayList<World>>();
				for (String uuidString : res.get(portalString).keySet()) {
					ArrayList<World> worlds = new ArrayList<World>();
					for (String worldString : res.get(portalString).get(uuidString)) {
						World world = Bukkit.getWorld(worldString);
						if (world!=null) worlds.add(world);
					}
					worldHistories.put(UUID.fromString(uuidString), worlds);
				}
				histories.put(portal, worldHistories);
			}
			
			for (CustomPortal portal : portalClass.getPortals()) {
				if (!histories.containsKey(portal)) histories.put(portal, new HashMap<UUID, ArrayList<World>>());
			}
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
		    writer.println(gson.toJson(res));
		    writer.close();
		} catch (IOException e) {
		}
	}


}
