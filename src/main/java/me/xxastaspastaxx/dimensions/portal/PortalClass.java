
package me.xxastaspastaxx.dimensions.portal;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.fileHandling.PortalLocations;


public class PortalClass {
	
	int maxRadius;
	World defaultWorld;
	boolean enableParticles;
	int teleportDelay;
	
	PortalLocations portalLocations;
	
	boolean allowNetherPortal = true;
	
	ArrayList<CustomPortal> portals = new ArrayList<CustomPortal>();
	
	Plugin pl;
	
	HashMap<Player,Entry<CustomPortal,Long>> timer = new HashMap<Player,Entry<CustomPortal,Long>>();
	
	public PortalClass(Plugin pl, int maxRadius, World defaultWorld, boolean portalParticles, int portalDelay) {
		this.pl = pl;
		this.maxRadius = maxRadius;
		this.defaultWorld = defaultWorld;
		this.enableParticles = portalParticles;
		this.teleportDelay = portalDelay;
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
			
			public void run() {
				for (int i=0;i<timer.size();i++) {
					Player p = (Player) timer.keySet().toArray()[i];
					
					if (isPortalAtLocation(p.getLocation().getBlock().getLocation())) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, Integer.MAX_VALUE,false,false,false));
						CustomPortal portal = getPortalAtLocation(p.getLocation().getBlock().getLocation());
						if (timer.get(p).getKey()==portal && ((System.currentTimeMillis()-timer.get(p).getValue())/1000)>=teleportDelay) {
							portal.usePortal(p,false);
							timer.remove(p);
							i--;
							continue;
						}
					}
				}
				if (enableParticles) {
					for (CustomPortal portal : portals) {
						if (portalLocations.getLocations(portal)==null) continue;
						for (World world : portalLocations.getLocations(portal).keySet()) {
							for (Location loc : portalLocations.getLocations(portal, world)) {
								portal.spawnParticles(loc);
							}
						}
					}
				}
			}
		}, 20,20);
		
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
			
			public void run() {
				HashMap<CustomPortal,ArrayList<Location>> toRemove = new HashMap<CustomPortal,ArrayList<Location>>();
				for (CustomPortal portal : portalLocations.getLocations().keySet()) {
					toRemove.put(portal, new ArrayList<Location>());
		        	for (World world : portalLocations.getLocations().get(portal).keySet()) {
		        		for (Location location : portalLocations.getLocations().get(portal).get(world)) {
		        			if (location==null) continue;
			        		portal.lightPortal(location, IgniteCause.LIGHTNING, null, true);
		        		}
		        	}
		        }
			}
		}, 20);
		
		Dimensions.portalClass = this;
	}
	
	public void setPortalLocations(PortalLocations portalLocations) {
		this.portalLocations = portalLocations;
	}

	public void setPortals(ArrayList<CustomPortal> portals) {
		this.portals = portals;
		
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled()) continue;
			if (portal.getMaterial()==Material.OBSIDIAN) {
				if (portal.getFrame()!=Material.NETHER_PORTAL || !portal.getWorld().getName().contentEquals("world_nether")) {
					allowNetherPortal = false;
				}
			}
		}
	}
	
	public ArrayList<CustomPortal> getPortals() {
		return portals;
	}
	
	public ArrayList<Location> getPortalsInWorld(CustomPortal portal, World world) {
		return portalLocations.getLocations(portal, world);
	}
	
	//Check if any of the saved portals can be lit in this location
	public boolean lightPortal(Location loc, IgniteCause cause, LivingEntity entity, Material lighter) {
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled() || portal.getDisabledWorlds().contains(loc.getWorld()) || portal.getLighter()!=lighter) continue; 
			if (portal.lightPortal(loc, cause, entity, false)) return true;
		}
		return false;
	}
	
	//check if there is a portal
	public boolean isPortal(Location loc, boolean load) {
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled() || portal.getDisabledWorlds().contains(loc.getWorld())) continue; 
			if (portal.isPortal(loc, true, load)!=null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPortalAtLocation(Location loc) {
		if (getPortalAtLocation(loc)!=null) return true;
		return false;
	}
	
	//get the portal
	public CustomPortal getPortal(Location loc, boolean load) {
		CustomPortal result = null;
		for (CustomPortal portal : portals) {
			if (!portal.isEnabled() || portal.getDisabledWorlds().contains(loc.getWorld())) continue; 
			if (portal.isPortal(loc, true, load)!=null) {
				result = portal;
				break;
			}
		}
		
		return result;
	}

	public CustomPortal getPortalAtLocation(Location loc) {
		return portalLocations.getPortal(loc);
	}
	
	public ArrayList<Location> getPortalLocations() {
		return portalLocations.getAllLocations();
	}
	
	public void addLocation(CustomPortal portal, Location loc) {
		portalLocations.addLocation(portal, loc);
	}
	
	public void removeLocation(CustomPortal portal, Location loc) {
		portalLocations.removeLocation(portal, loc);
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
		double closestDistance = 129;
		for(Location location : portals) {
			if (location.getBlock().getRelative(BlockFace.DOWN).getType()!=portal.material) continue;
			boolean hasFrame = false;
			if ((portal.getFrame()==Material.WATER || portal.getFrame()==Material.LAVA) && location.getBlock().getType()==portal.getFrame()) {
				hasFrame = true;
			} else {
				for (Entity en : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
					if (!(en instanceof FallingBlock)) continue;
	        		FallingBlock fallingBlock = (FallingBlock) en;
	        		
	            	if (fallingBlock.getBlockData().getMaterial()==portal.getFrame()) {
	            		hasFrame=true;
	            		break;
	            	}
				}
			}
			if (hasFrame && closestDistance>location.distance(loc)) {
	    		closestLocation = location;
	    		closestDistance = location.distance(loc);
	    		
	    	}
		}
		
		return closestLocation;
	}
	
	//Exit system because entering a portal must return you to your previous 
	public World getReturnWorld(Player p, CustomPortal portal) {
		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);
		
		List<String> lastUsedPortal = lastPortalConfig.getStringList("LastUsedPortal");
		List<String> lastUsedWorld = lastPortalConfig.getStringList("LastUsedWorld");
          
		while (lastUsedWorld.size()<lastUsedPortal.size()) lastUsedWorld.remove(lastUsedWorld.size()-1);
		while (lastUsedPortal.size()<lastUsedWorld.size()) lastUsedPortal.remove(lastUsedPortal.size()-1);
		
		for (int i=lastUsedPortal.size()-1;i>=0;i--) {
			if (lastUsedPortal.get(i).contentEquals(portal.getName())) {
				World world = Bukkit.getWorld(lastUsedWorld.get(i));
				if (p.getWorld()==portal.getWorld()) {
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

	public void addToUsedPortals(Player p, CustomPortal portal) {
		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);

		List<String> lastUsedPortal = lastPortalConfig.getStringList("LastUsedPortal");
		List<String> lastUsedWorld = lastPortalConfig.getStringList("LastUsedWorld");
		

		if (p.getWorld()!=portal.getWorld()) {
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
	
	public void addToTimer(Player p, CustomPortal portal) {
		if (p.getGameMode()==GameMode.CREATIVE || p.getGameMode()==GameMode.SPECTATOR) {
			portal.usePortal(p,false);
		} else {
			SimpleEntry<CustomPortal,Long> entry = new SimpleEntry<CustomPortal, Long>(portal, System.currentTimeMillis());
			timer.put(p, entry);
		}
	}
	
	public void removeFromTimer(Player p) {
		timer.remove(p);
        if (p.hasPotionEffect(PotionEffectType.CONFUSION)) {
            p.removePotionEffect(PotionEffectType.CONFUSION);
        }
	}
	
	public boolean isOnTimer(Player p) {
		return timer.containsKey(p);
	}
	
	public boolean isNetherPortalEnabled() {
		return allowNetherPortal;
	}
	
	public void findBestPathAndUse(Player p, World from, World to) {
		
		if (from==to) return;

		File lastPortalFile = new File("plugins/Dimensions/PlayerData/"+p.getName()+"/LastPortal.yml");
		YamlConfiguration lastPortalConfig = YamlConfiguration.loadConfiguration(lastPortalFile);

		List<String> lastUsedWorld = lastPortalConfig.getStringList("LastUsedWorld");
		
		if (lastUsedWorld.contains(to.getName())) {
			List<String> lastUsedPortal = lastPortalConfig.getStringList("LastUsedPortal");
			for (int i=lastUsedWorld.size()-1;i>=0;i--) {
				if (lastUsedWorld.get(i).contentEquals(to.getName())) {
					getPortalFromName(lastUsedPortal.get(i)).usePortal(p, true);
					return;
				}
			}
		} else {
			for (CustomPortal portal : portals) {
				if (portal.getWorld()==to) {
					portal.usePortal(p, true);
					return;
				}
			}
		}
	}
}
