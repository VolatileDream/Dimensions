package me.xxastaspastaxx.dimensions.portal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.utility.MinecraftReflection;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.events.EntityStartedViewingCustomPortalEvent;
import me.xxastaspastaxx.dimensions.events.EntityStoppedViewingCustomPortalEvent;

public class CompletePortal {

	private Class<?> blockClass;
	private Class<?> craftBlockDataClass;
	private Method getCombinedIdMethod;
	private Method getStateMethod;
	
	private int combinedId = 0;
	
	private BlockData netherBlockData;
	
	/*******************************************************************************/
	
	private CustomPortal portal;
	private ArrayList<PortalFrame> frames;
	private List<Object> portalInfo;
	private boolean zAxis;
	
	private HashMap<Entity,Long> timer = new HashMap<Entity,Long>();
	private ArrayList<Entity> hold = new ArrayList<Entity>();
	private ArrayList<Entity> shown = new ArrayList<Entity>();

	private HashMap<String, Object> tags = new HashMap<String, Object>();
	
	boolean isEntity = false;
	
	public CompletePortal(CustomPortal portal, ArrayList<PortalFrame> frames, List<Object> portalInfo, boolean zAxis) {
		this(portal,zAxis);
		this.portalInfo = portalInfo;
		setFrames(frames);
	}
	
	public CompletePortal(CustomPortal portal, List<Object> portalInfo, boolean zAxis) {
		this(portal,zAxis);
		this.portalInfo = portalInfo;
	}
	
	public CompletePortal(CustomPortal portal, boolean zAxis) {
		this.portal = portal;
		this.zAxis = zAxis;
		setup();
		
	}

	public void setup() {
		
		if (getPortal().getFrame().isSolid() || getPortal().getFrame()==Material.NETHER_PORTAL) {
			try {
				blockClass = MinecraftReflection.getBlockClass();
				craftBlockDataClass = MinecraftReflection.getCraftBukkitClass("block.data.CraftBlockData");
				getCombinedIdMethod = blockClass.getMethod("getCombinedId",MinecraftReflection.getIBlockDataClass());
				getStateMethod = craftBlockDataClass.getMethod("getState");
				
			} catch (NoSuchMethodException | IllegalArgumentException e) {
				e.printStackTrace();
				return;
			}
			
			isEntity = true;
		}
		
		Orientable orientable = (Orientable) Material.NETHER_PORTAL.createBlockData();
		orientable.setAxis(zAxis ? Axis.Z : Axis.X);
		netherBlockData = orientable;
	}
	
	public void reload() {
		if (isEntity) {
			try {
				Object nmsBlockData = getStateMethod.invoke(getPortal().getFrameBlockData(zAxis));
				combinedId = (int) getCombinedIdMethod.invoke(blockClass,nmsBlockData);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}
		for (PortalFrame frame : frames) {
			frame.reload(combinedId);
		}
		summon();
	}
	
	public CustomPortal getPortal() {
		return portal;
	}
	
	public Location getLocation() {
		return frames.get(0).getLocation().clone();
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
		timer.clear();
		for (Entity en : hold) {
			portal.portalClass.removeFromHold(en);
		}
		hold.clear();
		portal.portalClass.removeCompletePortal(this, remove);
		return true;
	}
	
	public void summon() {
		for (PortalFrame frame : frames) {
			frame.summon(null);
		}
	}
	
	public void remove() {
		for (PortalFrame frame : frames) {
			frame.remove(null);
		}
	}
	
	public boolean isZAxis() {
		return zAxis;
	}
	
	public BlockData getNetherBlockData() {
		return netherBlockData;
	}
	
	public String toString() {
		String res = portal.getName()+";"+zAxis+";"+getLocation().getWorld().getName()+";";
		for (PortalFrame frame : frames) {
			Location location = frame.getLocation();
			
			res+="l/l"+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ();
		}
		res = res.replaceFirst("l/l", "");
		res+=";";
		for (String tag : tags.keySet()) {
			res+="t/t"+tag+","+tags.get(tag);
		}
		res = res.replaceFirst("t/t", "");
		return res;
	}
	
	public static CompletePortal parseCompletePortal(String str) {
		String[] spl = str.split(";");
		CustomPortal portal = Dimensions.portalClass.getPortalFromName(spl[0]);
		boolean zAxis = Boolean.parseBoolean(spl[1]);
		String worldName = spl[2];
		

		CompletePortal complete = new CompletePortal(portal, zAxis);
		ArrayList<PortalFrame> frames = new ArrayList<PortalFrame>();
		boolean broken = false;
		for (String loc : spl[3].split("l/l")) {
			String[] locSplit = loc.split(",");
			Location loca = new Location(Bukkit.getWorld(worldName), Integer.parseInt(locSplit[0]),Integer.parseInt(locSplit[1]),Integer.parseInt(locSplit[2]));
			try {
				loca.getChunk();
			} catch (NullPointerException e) {
				broken = true;
				break;
			}
			frames.add(new PortalFrame(complete, loca));
		}
		if (broken) {
			complete = null;
		} else {
			complete.setFrames(frames);
			
			if (spl.length>=5) {
				for (String tag : spl[4].split("t/t")) {
					String[] tagSplit = tag.split(",");
					complete.setTag(tagSplit[0], parseTag(tagSplit[1]));
				}
			}
		}
		return complete;
	}

	private static Object parseTag(String string) {
		
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {}
		
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {}
		
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {}

		if (string.equals("true")) return true;
		if (string.equals("false")) return false;
		
		return string;
	}

	public void setFrames(ArrayList<PortalFrame> completeFrames) {
		this.frames = completeFrames;
		reload();

		for (PortalFrame frame : frames) {
			frame.summon(null);
		}
	}
	
	public void addToHold(Entity en, boolean pc) {
		hold.add(en);
		if (pc) portal.portalClass.addToHold(en);
	}
	
	public void removeFromHold(Entity en, boolean pc) {
		hold.remove(en);
		if (pc) portal.portalClass.removeFromHold(en);
	}

	public HashMap<Entity, Long> getTimer() {
		return timer;
	}
	
	public Object getTag(Object key) {
		return tags.get(key);
	}
	
	public void setTag(String key, Object value) {
		tags.put(key, value);
	}

	public boolean isOnHold(Entity p) {
		return hold.contains(p);
	}
	
	public ArrayList<Entity> getHold() {
		return hold;
	}

	public void addToShown(Player p) {
		if (shown.contains(p)) return;
		shown.add(p);
		EntityStartedViewingCustomPortalEvent event = new EntityStartedViewingCustomPortalEvent(this, p);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
	}

	public void removeFromShown(Player p) {
		if (!shown.contains(p)) return;
		shown.remove(p);
		EntityStoppedViewingCustomPortalEvent event = new EntityStoppedViewingCustomPortalEvent(this, p);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
	}
}
