package me.xxastaspastaxx.dimensions.fileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class PlayerData {

	private HashMap<UUID, HashMap<String, Object>> data = new HashMap<UUID, HashMap<String, Object>>();
	
	private final String filePath = "./plugins/Dimensions/PlayerData/playerData.json";
	
	@SuppressWarnings("unchecked")
	public PlayerData(PortalClass portalClass) {
		
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
			JSONObject dataObject = (JSONObject) jsonObject.get("loadData");

			((JSONObject) dataObject).keySet().forEach(uuid ->
	        {
	        	HashMap<String, Object> datas = new HashMap<String, Object>();
	        	
	            Object stuff = ((JSONObject) dataObject).get(uuid);
	            if (stuff instanceof JSONObject) {
	            	((JSONObject) stuff).keySet().forEach(key ->
	    	        {
	    	            datas.put(key.toString(), ((JSONObject) stuff).get(key));
	    	        	
	    	        });
	            }
	            
            	data.put(UUID.fromString(uuid.toString()), datas);
	        });
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<UUID, HashMap<String, Object>> getData() { 
		return data;
	}
	
	public Object getData(UUID uuid, String key) { 
		if (!data.containsKey(uuid) || !data.get(uuid).containsKey(key)) return null;
		return data.get(uuid).get(key);
	}
	
	public void save() {
		
		HashMap<String, HashMap<String, Object>> res = new HashMap<String, HashMap<String, Object>>();
		for (UUID uuid : data.keySet()) {
			HashMap<String, Object> dat = new HashMap<String, Object>();
			for (String str : data.get(uuid).keySet()) {
				dat.put(str, data.get(uuid).get(str));
			}
			res.put(uuid.toString(), dat);
		}
		
		try{
		    PrintWriter writer = new PrintWriter(filePath, "UTF-8");
		    writer.println("{\"loadData\":"+new Gson().toJson(res)+"}");
		    writer.close();
		} catch (IOException e) {
		}
	}

	public void setData(UUID uuid, String key, Object value) {
		if (!data.containsKey(uuid)) data.put(uuid, new HashMap<String, Object>());
		data.get(uuid).put(key, value);
	}


}
