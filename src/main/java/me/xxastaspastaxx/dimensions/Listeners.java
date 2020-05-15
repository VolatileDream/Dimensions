package me.xxastaspastaxx.dimensions;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Listeners implements Listener {
	
	public Listeners(Plugin pl) {
		  
		  
		Bukkit.getServer().getPluginManager().registerEvents(this, pl);
	}

}
