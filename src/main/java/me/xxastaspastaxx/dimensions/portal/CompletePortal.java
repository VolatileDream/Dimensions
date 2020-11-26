package me.xxastaspastaxx.dimensions.portal;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import me.xxastaspastaxx.dimensions.Utils.Dimensions;
import me.xxastaspastaxx.dimensions.events.EntityUseCustomPortalEvent;

public class CompletePortal {

	CustomPortal portal;
	ArrayList<PortalFrame> frames;
	List<Object> portalInfo;
	boolean zAxis;
	
	Location link;
	
	public CompletePortal(CustomPortal portal, ArrayList<PortalFrame> frames, List<Object> portalInfo, boolean zAxis) {
		this.portal = portal;
		this.frames = frames;
		this.portalInfo = portalInfo;
		this.zAxis = zAxis;
	}
	
	public CompletePortal(CustomPortal portal, List<Object> portalInfo, boolean zAxis) {
		this.portal = portal;
		this.portalInfo = portalInfo;
		this.zAxis = zAxis;
	}
	
	public CompletePortal(CustomPortal portal, boolean zAxis) {
		this.portal = portal;
		this.zAxis = zAxis;
	}

	public CustomPortal getPortal() {
		return portal;
	}
	
	public Location getLocation() {
		return frames.get(0).getLocation();
	}
	
	public ArrayList<PortalFrame> getFrames() {
		return frames;
	}
	
	public List<Object> getPortalInfo() {
		if (portalInfo==null && frames!=null) {
			portalInfo = portal.isPortal(frames.get(0).getLocation(), true, true);
		}
		return portalInfo;
	}
	
	public boolean destroy(boolean remove) {
		for (PortalFrame frame : frames) {
			frame.destroy(remove);
		}
		portal.portalClass.removeCompletePortal(this, remove);
		return true;
	}
	
	public boolean isZAxis() {
		return zAxis;
	}
	
	public Location getLinkedLocation() {
		if (link!=null && frames!=null) {
			if (!portal.portalClass.isPortalAtLocation(getLocation())) link=null;
		}
		if (link==null && frames!=null) {
			try {
				Entity en = getLocation().getWorld().getEntities().get(0);
				link = portal.calculateTeleportLocation(en, new EntityUseCustomPortalEvent(en, getLocation(), this, false, false, false));
			} catch (Exception e) {
				
			}
		}
		return link;
	}
	
	
	
	public String toString() {
		String res = portal.getName()+";"+zAxis+";";
		for (PortalFrame frame : frames) {
			Location location = frame.getLocation();
			
			res+="/"+location.getWorld().getName()+","+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ();
		}
		res = res.replaceFirst("/", "");
		return res;
	}
	
	public static CompletePortal parseCompletePortal(String str) {
		String[] spl = str.split(";");
		CustomPortal portal = Dimensions.getPortalFromName(spl[0]);
		boolean zAxis = Boolean.parseBoolean(spl[1]);
		

		CompletePortal complete = new CompletePortal(portal, zAxis);
		ArrayList<PortalFrame> frames = new ArrayList<PortalFrame>();
		for (String loc : spl[2].split("/")) {
			String[] locSplit = loc.split(",");
			frames.add(new PortalFrame(complete, new Location(Bukkit.getWorld(locSplit[0]), Integer.parseInt(locSplit[1]),Integer.parseInt(locSplit[2]),Integer.parseInt(locSplit[3])), zAxis));
		}
		for (PortalFrame frame : frames) {
			frame.summon(null);
		}
		complete.setFrames(frames);
		
		return complete;
	}

	public void setFrames(ArrayList<PortalFrame> completeFrames) {
		this.frames = completeFrames;
		
	}
}
