package me.xxastaspastaxx.dimensions.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class PlayerData {

	private static final double version = 1.0;

	private HashMap<UUID, HashMap<String, Object>> data = new HashMap<UUID, HashMap<String, Object>>();
	
	private final String filePath = "./plugins/Dimensions/PlayerData/playerData.json";

	Gson gson;
	
	public PlayerData(double fileVesrion) {

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
			HashMap<String, HashMap<String, Object>> res = new HashMap<String, HashMap<String, Object>>();
			try {
				res = gson.fromJson(new FileReader(filePath), new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType());
			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
				e.printStackTrace();
			}
			
			for (String uuidString : res.keySet()) {
	        	HashMap<String, Object> datas = new HashMap<String, Object>();
				for (String key : res.get(uuidString).keySet()) {
					datas.put(key, res.get(uuidString).get(key));
				}
				data.put(UUID.fromString(uuidString), datas);
			}
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
		    writer.println(gson.toJson(res));
		    writer.close();
		} catch (IOException e) {
		}
	}

	public void setData(UUID uuid, String key, Object value) {
		if (!data.containsKey(uuid)) data.put(uuid, new HashMap<String, Object>());
		data.get(uuid).put(key, value);
	}

	public void clear() {
		 data = new HashMap<UUID, HashMap<String, Object>>();
	}


}
