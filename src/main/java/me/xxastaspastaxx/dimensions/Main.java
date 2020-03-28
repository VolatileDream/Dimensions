package me.xxastaspastaxx.dimensions;

import org.bukkit.plugin.java.JavaPlugin;

import me.xxastaspastaxx.dimensions.commands.DimensionsCommands;
import me.xxastaspastaxx.dimensions.fileHandling.Files;
	
public class Main extends JavaPlugin {
	
	public Files files;	
	public Listeners listeners;
	
	public void onEnable() {
		
		//Setup listeners
		listeners = new Listeners(this);
		
		//Config files setup
		files = new Files(this);
		
		//Commands setup
		this.getCommand("dimensions").setExecutor(new DimensionsCommands(this));
		
		instance = this;
	}
	
	public void onDisable() {
		if (files!=null) files.onDisable();
	}
	
	private static Main instance;
	public static Main getInstance() {
	  return instance;
	}
	
}

	
