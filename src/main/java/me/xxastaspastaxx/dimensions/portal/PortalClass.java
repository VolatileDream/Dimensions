
package me.xxastaspastaxx.dimensions.portal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
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
import me.xxastaspastaxx.dimensions.Main;
import me.xxastaspastaxx.dimensions.fileHandling.PortalLocations;
import me.xxastaspastaxx.dimensions.portal.listeners.PortalListeners;


public class PortalClass {
	
	ArrayList<Material> lighters = new ArrayList<Material>();
	ArrayList<Material> frameMaterials = new ArrayList<Material>();
	ArrayList<Material> blocks = new ArrayList<Material>();
	
	int debugLevel;
	int maxRadius;
	World defaultWorld;
	boolean enableParticles;
	boolean enableMobs;
	int teleportDelay;
	int searchRadius;
	int spotSearchRadius;

	PortalLocations portalLocations;
	PortalListeners portalListeners;
	
	boolean allowNetherPortal = true;

	ArrayList<CustomPortal> portals = new ArrayList<CustomPortal>();
	HashMap<CustomPortal,ArrayList<PortalFrame>> frames = new HashMap<CustomPortal,ArrayList<PortalFrame>>();
	
	Main pl;
	
	ArrayList<LivingEntity> hold = new ArrayList<LivingEntity>();
	//HashMap<LivingEntity,Entry<CustomPortal,Long>> timer = new HashMap<LivingEntity,Entry<CustomPortal,Long>>();
	
	public PortalClass(Main pl) {
		this.pl = pl;
		
		Dimensions.portalClass = this;
	}
	
	public void setSettings(int maxRadius, World defaultWorld, boolean portalParticles, boolean teleportMobs, int portalDelay, int debugLevel, int searchRadius, int spotSearchRadius) {
		this.debugLevel = debugLevel;
		this.maxRadius = maxRadius;
		this.defaultWorld = defaultWorld;
		this.enableParticles = portalParticles;
		this.enableMobs = teleportMobs;
		this.teleportDelay = portalDelay;
		this.searchRadius = searchRadius;
		this.spotSearchRadius = spotSearchRadius;
	}
	
	public Main getPlugin() {
		return pl;
	}
	
	public void setPortalLocations(PortalLocations portalLocations, PortalListeners portalListeners) {
	  	debug("Loading locations",2);
	  	int locs = 0;
		for (CustomPortal portal : portalLocations.getLocations().keySet()) {
        	for (World world : portalLocations.getLocations().get(portal).keySet()) {
        		Iterator<Location> locIterator = portalLocations.getLocations().get(portal).get(world).iterator();
			    while (locIterator.hasNext()) {
			    	Location location = locIterator.next();
					if (portal.isPortal(location, true, true) != null) {
						try {
							portal.setFrameBlock(location, portal.isZAxis(location), true);
			        		locs++;
						} catch (NullPointerException e) { }
					} else {
						locIterator.remove();
						portalLocations.removeLocation(portal, location);
					}
			    }
        	}
        }
		debug("Loaded "+locs+"/"+portalLocations.getLocations().size()+" locations",1);

		this.portalLocations = portalLocations;
		this.portalListeners = portalListeners;
	}

	PacketListener packetListener;
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
				if (portal.getFrame()==Material.NETHER_PORTAL && portal.getWorld().getName().contentEquals("world_nether")) {
					allowNetherPortal = false;
				}
			}
			if (!frames.containsKey(portal)) {
				frames.put(portal, new ArrayList<PortalFrame>());
			}
		}

		if (packetListener!=null) ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
		
		packetListener = new PacketAdapter(pl, ListenerPriority.NORMAL, new ArrayList<PacketType>(){{add(PacketType.Play.Server.MAP_CHUNK); add(PacketType.Play.Server.UNLOAD_CHUNK);}}) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					int x = event.getPacket().getIntegers().read(0);
					int z = event.getPacket().getIntegers().read(1);
					for (PortalFrame frame : getFrames()) {
						frame.summon(event.getPlayer(),x,z);
					}
				}
				
				if (event.getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
					int x = event.getPacket().getIntegers().read(0);
					int z = event.getPacket().getIntegers().read(1);
					for (PortalFrame frame : getFrames()) {
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
	
	public ArrayList<Location> getPortalsInWorld(CustomPortal portal, World world) {
		return portalLocations.getLocations(portal, world);
	}
	
	//Check if any of the saved portals can be lit in this location
	public boolean lightPortal(Location loc, IgniteCause cause, LivingEntity entity, ItemStack lighter) {
		
		if (isPortalAtLocation(loc)) return false;
		
		debug("Attempting to light a portal at "+loc,2);
		if ((entity instanceof Player) && pl.getWorldGuardFlags()!=null && !pl.getWorldGuardFlags().testState((Player) entity, loc,WorldGuardFlags.IgniteCustomPortal)) {
			debug("Player does not have permission to light a portal at current location",2);
			return false;
		}

		for (CustomPortal portal : portals) {
			if (!portal.isEnabled() || portal.getDisabledWorlds().contains(loc.getWorld()) || (lighter!=null && portal.getLighter()!=lighter.getType())) continue; 
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

	public CustomPortal getPortalAtLocation(Location loc) {
		if (loc==null) return null;
		return portalLocations.getPortal(loc.getBlock().getLocation());
	}
	
	public PortalFrame getFrameAtLocation(Location loc) {
		for (CustomPortal portal : frames.keySet()) {
			for (PortalFrame frame : frames.get(portal)) {
				if (frame.getLocation().equals(loc.getBlock().getLocation())) {
					debug("Check for frame at "+loc+" | Result = true",3);
					return frame;
				}
			}
		}
		debug("Check for frame at "+loc+" | Result = false",3);
		return null;
	}
	
	public ArrayList<PortalFrame> getFrames(CustomPortal portal) {
		return frames.get(portal);
	}
	
	public boolean addFrame(CustomPortal portal, PortalFrame frame) {
		if (frame==null) return false;
		debug("Added new frame",3);
		if (frames.get(portal)==null) frames.put(portal, new ArrayList<PortalFrame>());
		frames.get(portal).add(frame);
		return true;
	}
	
	public ArrayList<Location> getPortalLocations() {
		return portalLocations.getAllLocations();
	}
	
	public void addLocation(CustomPortal portal, Location loc) {
		portalLocations.addLocation(portal, loc);
	}
	
	public void removeFrame(CustomPortal portal, PortalFrame frame, boolean remove) {
		if (remove) portalLocations.removeLocation(portal, frame.getLocation().getBlock().getLocation());
		frames.get(portal).remove(frame);
	}
	
	public void removeLocation(CustomPortal portal, Location loc) {
		portalLocations.removeLocation(portal, loc.getBlock().getLocation());
	}
	
	public int getMaxRadius() {
		return maxRadius;
	}
	
	public World getDefaultWorld() {
		return defaultWorld;
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
		ArrayList<Location> portals = portalLocations.getLocations(portal,loc.getWorld());
		if (portals==null) return null;
		
		Location closestLocation = null;
		double closestDistance = (searchRadius/2+1)+searchRadius*portal.getRatio()*0.5;
		for(Location location : portals) {
			if (location.getBlock().getRelative(BlockFace.DOWN).getType()!=portal.material) continue;
			double dist = location.distance(loc);
			if (closestDistance>dist) {
	    		closestLocation = location;
	    		closestDistance = dist;
	    	}
		}
		
		return closestLocation;
	}
	
	//Exit system because entering a portal must return you to your previous 
	public World getReturnWorld(LivingEntity p, CustomPortal portal, World from) {
		if (!(p instanceof Player)) return getDefaultWorld();
		
		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);
		
		List<String> lastUsedPortal = lastPortalConfig.getStringList("LastUsedPortal");
		List<String> lastUsedWorld = lastPortalConfig.getStringList("LastUsedWorld");
          
		while (lastUsedWorld.size()<lastUsedPortal.size()) lastUsedWorld.remove(lastUsedWorld.size()-1);
		while (lastUsedPortal.size()<lastUsedWorld.size()) lastUsedPortal.remove(lastUsedPortal.size()-1);
		
		World pWorld = from==null?p.getWorld():from;
		
		for (int i=lastUsedPortal.size()-1;i>=0;i--) {
			if (lastUsedPortal.get(i).contentEquals(portal.getName())) {
				World world = Bukkit.getWorld(lastUsedWorld.get(i));
				if (!pWorld.equals(world)) {
					lastUsedPortal.remove(i);
					lastUsedWorld.remove(i);
					
					lastPortalConfig.set("LastUsedPortal", lastUsedPortal);
					lastPortalConfig.set("LastUsedWorld", lastUsedWorld);
					
					try {
						lastPortalConfig.save(lastPortalFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				} else {
					world = portal.getWorld();
				}
				return world;
			}
		}
		
		return getDefaultWorld();
	}

	public void addToUsedPortals(LivingEntity p, CustomPortal portal) {
		if (!(p instanceof Player)) return;
		
		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);

		List<String> lastUsedPortal = lastPortalConfig.getStringList("LastUsedPortal");
		List<String> lastUsedWorld = lastPortalConfig.getStringList("LastUsedWorld");
		

		if (!p.getWorld().equals(portal.getWorld())) {
			lastUsedPortal.add(portal.getName());
			lastUsedWorld.add(p.getLocation().getWorld().getName());
		}
		
		lastPortalConfig.set("LastUsedPortal", lastUsedPortal);
		lastPortalConfig.set("LastUsedWorld", lastUsedWorld);
		
		try {
			lastPortalConfig.save(lastPortalFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isOnHold(LivingEntity p) {
		return hold.contains(p);
	}
	
	public void addToHold(LivingEntity p) {
		if (!hold.contains(p)) {
			hold.add(p);
		}
	}
	
	public void removeFromHold(LivingEntity p) {
		hold.remove(p);
	}
	
	public boolean isNetherPortalEnabled() {
		return allowNetherPortal;
	}
	
	public boolean enableMobsTeleportation() {
		return enableMobs;
	}
	
	public void findBestPathAndUse(Player p, World from, World to) {
		
		if (from.equals(to)) return;

		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);

		List<String> lastUsedWorld = lastPortalConfig.getStringList("LastUsedWorld");
		
		if (lastUsedWorld.contains(to.getName())) {
			List<String> lastUsedPortal = lastPortalConfig.getStringList("LastUsedPortal");
			for (int i=lastUsedWorld.size()-1;i>=0;i--) {
				if (lastUsedWorld.get(i).contentEquals(to.getName())) {
					getReturnWorld(p, getPortalFromName(lastUsedPortal.get(i)), from);
					return;
				}
			}
		} else {
			for (CustomPortal portal : portals) {
				if (portal.getWorld().equals(to)) {
					portal.usePortal(p, true, false);
					return;
				}
			}
		}
	}
	
	public ArrayList<PortalFrame> getFrames() {
		ArrayList<PortalFrame> result = new ArrayList<PortalFrame>();
		
		for (ArrayList<PortalFrame> list : frames.values()) {
			result.addAll(list);
		}
		
		return result;
	}

	public int getTeleportDelay() {
		return teleportDelay;
	}
	
	public void debug(String msg, int lvl) {
		if (debugLevel>=lvl)
		System.out.println(msg);
	}

	public int getSpotSearchRadius() {
		return spotSearchRadius;
	}
	
	public ArrayList<PortalFrame> getNearbyPortalFrames(Location loc, int radius) {
		ArrayList<PortalFrame> result = new ArrayList<PortalFrame>();
		
		for (PortalFrame frame : getFrames()) {
			if (!frame.getLocation().getWorld().equals(loc.getWorld())) continue;
			if (frame.getLocation().distance(loc)<radius) result.add(frame);
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
}
