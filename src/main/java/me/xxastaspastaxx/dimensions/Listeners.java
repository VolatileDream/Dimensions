package me.xxastaspastaxx.dimensions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class Listeners implements Listener {
	
	  public Listeners(Plugin pl) {
		  
		  
		    Bukkit.getServer().getPluginManager().registerEvents(this, pl);
		  }

	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		//This is used to return players to the previous world
		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);
		
		//Portal name
          List<String> lup = lastPortalConfig.getStringList("LastUsedPortal");
          lastPortalConfig.set("LastUsedPortal", lup);

        //Worlds name
          List<String> luw = lastPortalConfig.getStringList("LastUsedWorld");
          lastPortalConfig.set("LastUsedWorld", luw);

		try {
			lastPortalConfig.save(lastPortalFile);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}
