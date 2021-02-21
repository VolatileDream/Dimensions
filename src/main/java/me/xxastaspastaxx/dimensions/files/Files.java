package me.xxastaspastaxx.dimensions.files;

import me.xxastaspastaxx.dimensions.Dimensions;

public class Files {
	
	public PortalFiles portalFiles;
	
	public Files(Dimensions pl) {
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
