package me.xxastaspastaxx.dimensions.fileHandling;

import org.bukkit.plugin.Plugin;

public class Files {
	
	public PortalFiles portalFiles;
	
	public Files(Plugin pl) {
		portalFiles = new PortalFiles(pl);
	}
	
	public void onDisable() {
		//Functions when unloading plugin
		portalFiles.onDisable();
	}
	
}
