
package me.xxastaspastaxx.dimensions.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.files.PlayerData;
import me.xxastaspastaxx.dimensions.files.PlayerHistories;
import me.xxastaspastaxx.dimensions.files.PortalLocations;
import me.xxastaspastaxx.dimensions.portal.listeners.PortalListeners;
import me.xxastaspastaxx.dimensions.utils.DimensionsSettings;
import me.xxastaspastaxx.dimensions.utils.DimensionsUtils;
import me.xxastaspastaxx.dimensions.utils.Messages;


public class PortalClass {
	
	ArrayList<Material> lighters = new ArrayList<Material>();
	ArrayList<Material> frameMaterials = new ArrayList<Material>();
	ArrayList<Material> blocks = new ArrayList<Material>();

	PlayerHistories playerHistories;
	PortalLocations portalLocations;
	PlayerData playerData;
	PortalListeners portalListeners;
	
	boolean allowNetherPortal = true;

	ArrayList<CustomPortal> portals = new ArrayList<CustomPortal>();
	ArrayList<CompletePortal> completePortals = new ArrayList<CompletePortal>();
	
	Dimensions pl;
	
	ArrayList<Entity> hold = new ArrayList<Entity>();
	
	public PortalClass(Dimensions pl) {
		this.pl = pl;
		
		Dimensions.portalClass = this;
	}
	
	public Dimensions getPlugin() {
		return pl;
	}
	
	public void setPortalLocations(PortalLocations portalLocations, PortalListeners portalListeners) {

		this.portalListeners = portalListeners;
		this.portalLocations = portalLocations;
		completePortals = portalLocations.getPortals();
	}
	
	public void setPlayerHistories(PlayerHistories playerHistories) {
	  	debug("Loading histories",2);
	  	HashMap<CustomPortal, HashMap<UUID, ArrayList<World>>> histories = playerHistories.getHistories();
	  	Iterator<CustomPortal> portalIter = histories.keySet().iterator();
		while (portalIter.hasNext()) {
			CustomPortal portal = portalIter.next();
	    	try {
	        	portal.setHistories(histories.get(portal));
	    	} catch (NullPointerException e) {
	    		portalIter.remove();
	    		playerHistories.removePortal(portal);
				
			}
        }
		this.playerHistories = playerHistories;
	}
	
	public void setPlayerData(PlayerData playerData) {
		this.playerData = playerData;
	}

	private PacketListener packetListener;
	@SuppressWarnings("serial")
	public void setPortals(ArrayList<CustomPortal> portals, ArrayList<Material> lighters, ArrayList<Material> frameMaterials, ArrayList<Material> blocks) {
		this.portals = portals;
		this.lighters = lighters;
		this.frameMaterials = frameMaterials;
		this.blocks = blocks;
		
		allowNetherPortal = true;
		
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled()) continue;
			if (portal.getMaterial()==Material.OBSIDIAN) {
				if (portal.getFrame()==Material.NETHER_PORTAL && portal.getWorld().equals(Bukkit.getServer().getWorlds().get(1))) {
					allowNetherPortal = false;
				}
			}
		}

		if (packetListener!=null) ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
		
		packetListener = new PacketAdapter(pl, ListenerPriority.NORMAL, new ArrayList<PacketType>(){{add(PacketType.Play.Server.MAP_CHUNK); add(PacketType.Play.Server.UNLOAD_CHUNK);}}) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					int x = event.getPacket().getIntegers().read(0);
					int z = event.getPacket().getIntegers().read(1);
					for (PortalFrame frame : getAllFrames()) {
						frame.summon(event.getPlayer(),x,z);
					}
				}
				
				if (event.getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
					int x = event.getPacket().getIntegers().read(0);
					int z = event.getPacket().getIntegers().read(1);
					for (PortalFrame frame : getAllFrames()) {
						frame.remove(event.getPlayer(),x,z);
					}
				}
			}
		};
		
		ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
	}
	
	public ArrayList<Material> getLighters() {
		return lighters;
	}
	
	public ArrayList<Material> getFrameMaterials() {
		return frameMaterials;
	}
	
	public ArrayList<Material> getBlocks() {
		return blocks;
	}
	
	public ArrayList<CustomPortal> getPortals() {
		return portals;
	}
	
	public ArrayList<CompletePortal> getPortalsInWorld(CustomPortal portal, World world) {
		return portalLocations.getPortals(portal, world);
	}
	
	//Check if any of the saved portals can be lit in this location
	public boolean lightPortal(Location loc, IgniteCause cause, LivingEntity entity, ItemStack lighter) {
		
		if (isPortalAtLocation(loc)) return false;
		
		
		debug("Attempting to light a portal at "+loc,2);
		if ((entity instanceof Player) && !DimensionsUtils.testState((Player) entity, loc,WorldGuardFlags.IgniteCustomPortal)) {
			entity.sendMessage(Messages.get("worldGuardDenyMessage"));
			debug("Player does not have permission to light a portal at current location",2);
			return false;
		}

		for (CustomPortal portal : portals) {
			if (!portal.isEnabled()) continue; 
			if (portal.lightPortal(loc, cause, entity, false,lighter)) {
				debug("Portal lit at "+loc,2);
				return true;
			}
		}
		
		debug("No portal could be found in "+loc,2);
		return false;
	}
	
	//check if there is a portal
	public boolean isPortal(Location loc, boolean load) {
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled() || portal.getDisabledWorlds().contains(loc.getWorld())) continue; 
			if (portal.isPortal(loc.getBlock().getLocation(), true, load)!=null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPortalAtLocation(Location loc) {
		if (getPortalAtLocation(loc.getBlock().getLocation())!=null) {
			debug("Check for portal at "+loc+" | Result = true",3);
			return true;
		}
		debug("Check for portal at "+loc+" | Result = false",3);
		return false;
	}
	
	//get the portal
	public CustomPortal getPortal(Location loc, boolean load) {
		CustomPortal result = null;
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled() || portal.getDisabledWorlds().contains(loc.getWorld())) continue; 
			if (portal.isPortal(loc.getBlock().getLocation(), true, load)!=null) {
				result = portal;
				break;
			}
		}
		
		return result;
	}

	public CompletePortal getPortalAtLocation(Location loc) {
		if (loc==null) return null;
		return portalLocations.getPortal(loc.getBlock().getLocation());
	}
	
	public PortalFrame getFrameAtLocation(Location loc) {
		for (CompletePortal portal : completePortals) {
			for (PortalFrame frame : portal.getFrames()) {
				if (frame.getLocation().equals(loc.getBlock().getLocation())) {
					debug("Check for frame at "+loc+" | Result = true",3);
					return frame;
				}
			}
		}
		debug("Check for frame at "+loc+" | Result = false",3);
		return null;
	}
	
	public ArrayList<CompletePortal> getCompletePortals() {
		
		return completePortals;
	}
	
	public ArrayList<CompletePortal> getCompletePortals(CustomPortal portal) {
		
		ArrayList<CompletePortal> result = new ArrayList<CompletePortal>();
		
		for (CompletePortal complete : completePortals) {
			if (portal.equals(complete.getPortal())) result.add(complete);
		}
		
		return result;
	}
	
	/*public boolean addFrame(CustomPortal portal, PortalFrame frame) {
		if (frame==null) return false;
		debug("Added new frame",3);
		if (frames.get(portal)==null) frames.put(portal, new ArrayList<PortalFrame>());
		frames.get(portal).add(frame);
		return true;
	}*/
	
	public ArrayList<Location> getPortalLocations() {
		return portalLocations.getLocations();
	}
	
	public void addPortal(CompletePortal complete) {
		portalLocations.addPortal(complete);
	}
	
	public void removeCompletePortal(CompletePortal complete, boolean remove) {
		if (remove) {
			portalLocations.removePortal(complete);
		}
		completePortals.remove(complete);
	}
	
	public void removePortal(CustomPortal portal) {
		portalLocations.removePortal(portal);
	}
	
	public CustomPortal getPortalFromName(String portalName) {
		for (CustomPortal portal : portals) {
			if (portal.getName().contentEquals(portalName)) {
				return portal;
			}
		}
		
		return null;
	}
	
	public Location getNearestPortalLocation(CustomPortal portal, Location loc) {
		if (loc==null) return null;
		ArrayList<Location> portals = portalLocations.getLocations(portal,loc.getWorld());
		if (portals==null) return null;
		
		Location closestLocation = null;
		int searchRadius = DimensionsSettings.getSearchRadius();
		double closestDistance = (searchRadius/2+1)+searchRadius*portal.getRatio()*0.5;
		for(Location location : portals) {
			if (!portal.isHorizontal() && (location.getBlock().getRelative(BlockFace.DOWN).getType()!=portal.material)) continue;
			double dist = location.distance(loc);
			if (closestDistance>dist) {
	    		closestLocation = location;
	    		closestDistance = dist;
	    	}
		}
		
		return closestLocation;
	}

	public boolean isOnHold(Entity p) {
		return hold.contains(p);
	}
	
	public void addToHold(Entity p) {
		if (!hold.contains(p)) {
			hold.add(p);
		}
	}
	
	public void removeFromHold(Entity p) {
		hold.remove(p);
	}
	
	public boolean isNetherPortalEnabled() {
		return allowNetherPortal;
	}
	
	public void findBestPathAndUse(Player p, World from, World to) {
	
		HashMap<String,ArrayList<String>> rules = DimensionsSettings.getPathRules();
		//forceReturnWorld
		//ignoreIrrelevantWorld # (string can start with startsWith:)

		if (from.equals(to) || isOnHold(p)) return;
		
		CustomPortal head = null;
		for (CustomPortal portal : portals) {
			if (!portal.isWorldNeeded()) continue;
			if (portal.getWorld().equals(from)) head = portal;
		}
		
		
		boolean found = false;
		while (!found && head!=null) {
			if (head.isReturnWorld(p, to)) {
				head.getReturnWorld(p, head.getWorld(), true, true);
				return;
			}
			
			for (String worldRule : rules.get("forceReturnWorld")) {
				if (head.isReturnWorld(p, Bukkit.getWorld(worldRule))) {
					findBestPathAndUse(p, Bukkit.getWorld(worldRule), to);
					head.getReturnWorld(p, from, true, true);
					return;
				}
			}
			
			boolean noPortal = true;
			for (CustomPortal portal : portals) {
				if (!portal.isWorldNeeded()) continue;
				if (head.isReturnWorld(p, portal.getWorld())) {
					head.getReturnWorld(p, head.getWorld(), true, true);
					head = portal;
					noPortal = false;
					break;
				}
			}
			if (noPortal) found = true;
		}
	
		if (head == null && !containsStartsWith(rules.get("ignoreIrrelevantWorld"), from.getName())) {
			for (CustomPortal portal : portals) {
				if (!portal.isWorldNeeded()) continue;
				if (portal.getWorld().equals(to)) {
					if (!portal.getDisabledWorlds().contains(from)) {
						portal.addToUsedPortals(p, from);
						return;
					} else {
						head = portal;
						break;
					}
				}
			}
		}

		if (head == null) return;
		
		ArrayList<CustomPortal> historyPortals = new ArrayList<CustomPortal>();
		historyPortals.add(head);
		
		found = false;
		while (!found) {
			boolean noPortal = true;
			for (CustomPortal portal : portals) {
				if (!portal.isWorldNeeded()) continue;
				if (portal.equals(head)) continue;
				if (!head.getDisabledWorlds().contains(portal.getWorld())) {
					historyPortals.add(0,portal);
					head = portal;
					noPortal = false;
					if (!head.getDisabledWorlds().contains(from)) found = true;
					break;
				}

			}
			
			if (noPortal) return;
		}
		
		
		for (int i = 0;i<historyPortals.size();i++) {
			CustomPortal portal = historyPortals.get(i);
			portal.addToUsedPortals(p, (i==0)?from:historyPortals.get(i-1).getWorld());
		}
		
	}
	
	private boolean containsStartsWith(ArrayList<String> arrayList, String name) {
		for (String str : arrayList) {
			if (str.equals(name) || str.startsWith("startsWith:"+name)) return true;
		}
		return false;
	}
	
	public void debug(String msg, int lvl) {
		if (DimensionsSettings.getDebugLevel()>=lvl)
		System.out.println("[DimensionsDebugger] " + msg);
	}
	
	public ArrayList<PortalFrame> getAllFrames() {
		ArrayList<PortalFrame> result = new ArrayList<PortalFrame>();
		for (CompletePortal complete : completePortals) {
			result.addAll(complete.getFrames());
		}
		
		return result;
	}
	
	public ArrayList<PortalFrame> getNearbyPortalFrames(CustomPortal portal, Location loc, int radius) {
		ArrayList<PortalFrame> result = new ArrayList<PortalFrame>();
		
		for (PortalFrame frame : getFrames(portal)) {
			if (!frame.getLocation().getWorld().equals(loc.getWorld())) continue;
			if (frame.getLocation().distance(loc)<radius) result.add(frame);
		}
		
		return result;
	}
	
	public ArrayList<CompletePortal> getNearbyPortals(Location loc, int radius) {
		ArrayList<CompletePortal> result = new ArrayList<CompletePortal>();
		
		for (CompletePortal complete : completePortals) {
			PortalFrame frame = complete.getFrames().get(0);
			if (!frame.getLocation().getWorld().equals(loc.getWorld())) continue;
			if (frame.getLocation().distance(loc)<radius) result.add(complete);
		}
		
		return result;
	}
	
	public ArrayList<CompletePortal> getNearbyPortals(CustomPortal portal, Location loc, int radius) {
		ArrayList<CompletePortal> result = new ArrayList<CompletePortal>();
		
		for (CompletePortal complete : completePortals) {
			if (complete.getPortal().equals(portal)) continue;
			PortalFrame frame = complete.getFrames().get(0);
			if (!frame.getLocation().getWorld().equals(loc.getWorld())) continue;
			if (frame.getLocation().distance(loc)<radius) result.add(complete);
		}
		
		return result;
	}

	public boolean existsPortal(String name) {
		for (CustomPortal portal : portals) {
			if (portal.getName().contentEquals(name)) return true;
		}
		return false;
	}

	public CustomPortal getPortalByName(String name) {
		for (CustomPortal portal : portals) {
			if (portal.getName().contentEquals(name)) return portal;
		}
		return null;
	}

	public void clearHistory() {
		for (CustomPortal portal : portals) {
			portal.setHistories(new HashMap<UUID, ArrayList<World>>());
		}
		save();
	}
	
	public void save() {
		Dimensions.getInstance().files.portalFiles.save();
	}

	public ArrayList<PortalFrame> getFrames(CustomPortal customPortal) {
		ArrayList<PortalFrame> result = new ArrayList<PortalFrame>();
		for (CompletePortal complete : getCompletePortals(customPortal)) {
			for (PortalFrame frame : complete.getFrames()) {
				result.add(frame);
			}
		}
		
		return result;
	}

	public void removePortals(CustomPortal customPortal) {
		Iterator<CompletePortal> completeIter = completePortals.iterator();
		while (completeIter.hasNext()) {
			CompletePortal complete = completeIter.next();
			if (complete.getPortal().equals(customPortal)) {
				completeIter.remove();
			}
		}
		
	}

	public ArrayList<CompletePortal> getPortalsVisibleFromPlayer(Player p) {
		ArrayList<CompletePortal> res = new ArrayList<CompletePortal>();
		for (CompletePortal complete : completePortals) {
			if (complete.getFrames().get(0).isShown(p)) res.add(complete);
		}
		return res;
	}

	public void setData(UUID uuid, String data, Object value) {
		playerData.setData(uuid, data, value);
	}

	public Object getData(UUID uuid, String data) {
		return playerData.getData(uuid, data);
	}

	public CompletePortal getLookingPortal(LivingEntity e) {
		List<Block> los = e.getLineOfSight(null, 5);
		for (Block block : los) {
			if (!DimensionsUtils.isAir(block.getType())) break;
			CompletePortal compl = getPortalAtLocation(block.getLocation());
			if (compl!=null) return compl;
		}
		return null;
	}

	public void clearData() {
		playerData.clear();
		save();
	}

}
