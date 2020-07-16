package me.xxastaspastaxx.dimensions.fileHandling;

import me.xxastaspastaxx.dimensions.Main;

public class Files {
	
	public PortalFiles portalFiles;
	
	public Files(Main pl) {
		portalFiles = new PortalFiles(pl);
	}
	
	public void onDisable() {
		//Functions when unloading plugin
		portalFiles.onDisable();
	}

	public boolean reload() {
		return portalFiles.reloadAll();
	}
	
}
