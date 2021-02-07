package me.xxastaspastaxx.dimensions.portal;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerEntityTeleport;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import me.xxastaspastaxx.dimensions.utils.DimensionsSettings;

public class PortalFrame implements Listener {
	
	PortalClass pc;
	
	/*********************************************************/
	
	WrapperPlayServerSpawnEntity spawnPacket;
	WrapperPlayServerEntityTeleport teleportPacket;
	WrapperPlayServerEntityMetadata metaPacket;
	WrappedDataWatcher dataWatcher;
	WrapperPlayServerEntityDestroy destroyPacket;
	
	/*********************************************************/
	
	private int fallingBlockId;
	
	CompletePortal complete;
	Location loc;
	Location fixedLoc;
	int[] chunkPos = new int[2];
	
	int task;

	boolean destroyed = false;
	boolean enabled = false;
	
	int task2;
	
	long startLife;
	
	public PortalFrame(CompletePortal complete, Location location) {
		this.complete = complete;
		this.loc = location;
		this.fixedLoc = loc.clone().add(0.5,0.5,0.5);
		this.chunkPos[0] = loc.getChunk().getX();
		this.chunkPos[1] = loc.getChunk().getZ();
		this.pc = complete.getPortal().portalClass;
		
		startLife = System.currentTimeMillis();

		fallingBlockId =  (int) (Math.random() * Integer.MAX_VALUE);
		
		startTask();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, pc.pl);
	}
	
	public void reload(int combinedId) {
		
		if (!complete.isEntity) return;
		
		spawnPacket = new WrapperPlayServerSpawnEntity();

		spawnPacket.setEntityID(fallingBlockId);
		try {
			spawnPacket.setType(EntityType.FALLING_BLOCK);
			spawnPacket.setObjectData(combinedId);
		} catch (FieldAccessException e) {
			spawnPacket.getHandle().getIntegers().write(6, 70);
			spawnPacket.getHandle().getIntegers().write(7, combinedId);
		}
		
		spawnPacket.setX(loc.getX());
		spawnPacket.setY(loc.getY());
		spawnPacket.setZ(loc.getZ());
		
		metaPacket = new WrapperPlayServerEntityMetadata();
		metaPacket.setEntityID(fallingBlockId);
		dataWatcher = new WrappedDataWatcher();
		
		WrappedDataWatcher.WrappedDataWatcherObject noGravity = new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));
		WrappedDataWatcher.WrappedDataWatcherObject ticksLived = new WrappedDataWatcher.WrappedDataWatcherObject(1, WrappedDataWatcher.Registry.get(Integer.class));

		dataWatcher.setObject(noGravity, true);
		dataWatcher.setObject(ticksLived, Integer.MAX_VALUE);
		
		metaPacket.setMetadata(dataWatcher.getWatchableObjects());
		
		
		teleportPacket = new WrapperPlayServerEntityTeleport();
		teleportPacket.setEntityID(fallingBlockId);
		teleportPacket.setX(loc.getX()+0.5f);
		teleportPacket.setY(loc.getY());
		teleportPacket.setZ(loc.getZ()+0.5f);
		
		destroyPacket = new WrapperPlayServerEntityDestroy();
		destroyPacket.setEntityIds(new int[] {fallingBlockId});
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public int getFallingBlockID() {
		return fallingBlockId;
	}

	ArrayList<Player> shown = new ArrayList<Player>();
	
	public void startTask() {

		if (!loc.getChunk().isLoaded() || destroyed || Bukkit.getScheduler().isCurrentlyRunning(task) || Bukkit.getScheduler().isCurrentlyRunning(task2) || enabled) return;
		
		pc.debug("Starting task for portal frame at "+loc, 3);
		
		enabled = true;
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(pc.pl, new Runnable() {
			public void run() {
				
				if (shown.size()==0) return;
				

				if (DimensionsSettings.showPortalContent()) {
					loc.getBlock().setType(Material.AIR);
					if (complete.isEntity) {
						for (Player p : shown) {
							p.sendBlockChange(loc, Material.AIR.createBlockData());
						}
					} else {
						for (Player p : shown) {
							p.sendBlockChange(loc,complete.getPortal().getFrameBlockData(complete.isZAxis()));
						}
					}
				}
				
				for (Entity en : loc.getWorld().getNearbyEntities(fixedLoc, 0.5,0.5,0.5)) {
					if (!(en instanceof Entity)) continue;
					if ((!DimensionsSettings.enableMobsTeleportation() && !(en instanceof Player)) || (!DimensionsSettings.enableEntitiesTeleportation() && !(en instanceof LivingEntity))) continue;
					if (DimensionsSettings.showPortalContent() && en instanceof Player && DimensionsSettings.enableNetherPortalEffect()) ((Player) en).sendBlockChange(loc, complete.getNetherBlockData());
					if (complete.getTimer().containsKey(en) || pc.isOnHold(en)) continue;
					if (en.getLocation().getBlock().equals(loc.getBlock())) {
						int extra = 0;
						if (en instanceof Player && (((Player) en).getGameMode()==GameMode.CREATIVE || ((Player) en).getGameMode()==GameMode.SPECTATOR)) extra = DimensionsSettings.getTeleportDelay()*1000;
						complete.getTimer().put(en, System.currentTimeMillis()-extra);
						complete.addToHold(en, true);
					}
				}
				
			    Iterator<Entry<Entity,Long>> timerIterator = complete.getTimer().entrySet().iterator();
			    while (timerIterator.hasNext()) {
			    	Entry<Entity,Long> entry = timerIterator.next();
			    	Entity en = entry.getKey();
			    	Location eloc = en.getLocation();
			    	if ((eloc.getBlockX()!=loc.getBlockX() || eloc.getBlockY()!=loc.getBlockY() || eloc.getBlockZ()!=loc.getBlockZ()) && (!pc.isPortalAtLocation(en.getLocation()) || (pc.isPortalAtLocation(en.getLocation()) && !pc.getPortalAtLocation(en.getLocation()).equals(complete)))) {
			    		timerIterator.remove();
					} else if (!complete.getTimer().containsKey(en)) {
						try {
							timerIterator.remove();
							complete.removeFromHold(en, false);
						} catch (ConcurrentModificationException e) { }
					} else if (((System.currentTimeMillis()-complete.getTimer().get(en))/1000)>=DimensionsSettings.getTeleportDelay()) {
						if (complete.getPortal().usePortal(complete, en, false, en.getWorld(), false)) {
							try {
								complete.removeFromHold(en, false);
							} catch (ConcurrentModificationException e) { }
						}

						try {
							timerIterator.remove();
						} catch (ConcurrentModificationException e) {}
					}
			    }
			    
			    Iterator<Entity> holdIterator = complete.getHold().iterator();
			    while (holdIterator.hasNext()) {
			    	Entity en = holdIterator.next();
					Location eloc = en.getLocation();
					CompletePortal cp = pc.getPortalAtLocation(eloc);
					if (cp==null || !cp.getPortal().equals(complete.getPortal())) {
						holdIterator.remove();
						pc.removeFromHold(en);
					}
			    }
			    
				if (DimensionsSettings.isEnableParticles()) {
					complete.getPortal().spawnParticles(loc);
				}
				
				if (DimensionsSettings.showPortalContent() && complete.isEntity) {
					for (Player p : shown) {
						metaPacket.sendPacket(p);
						teleportPacket.sendPacket(p);
					}
				}
				
			}
		}, 20,20);
		
		if ((!complete.getPortal().isWorldNeeded() || !complete.getPortal().getWorld().equals(loc.getWorld())) && complete.getPortal().canSpawnEntities() && loc.getBlock().getRelative(BlockFace.DOWN).getType()==complete.getPortal().getMaterial()) {
			task2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(pc.pl, new Runnable() {
				public void run() {
					EntityType type = complete.getPortal().getEntitySpawn();
					if (type!=null) {
						complete.addToHold(loc.getWorld().spawnEntity(loc, type), false);
					}
				}
			}, complete.getPortal().getEntityDelay(), complete.getPortal().getEntityDelay());
		}
	}
	
	public void summon(Player p) {
		if (destroyed || spawnPacket==null) return;

		if (p!=null && (shown.contains(p) || !p.getWorld().equals(loc.getWorld()))) return;
		if (p==null) {
			for (Entity player : loc.getWorld().getNearbyEntities(fixedLoc, 16*Bukkit.getViewDistance(), 255, 16*Bukkit.getViewDistance(), (player) -> player instanceof Player)) {
				summon((Player) player);
			}
			return;
		}
		if (DimensionsSettings.showPortalContent() && complete.isEntity) {
			spawnPacket.sendPacket(p);
			teleportPacket.sendPacket(p);
			metaPacket.sendPacket(p);
		} else if (DimensionsSettings.showPortalContent()) {
			p.sendBlockChange(loc, complete.getPortal().getFrameBlockData(complete.isZAxis()));
		}
		shown.add(p);
		complete.addToShown(p);
		
		if (!shown.isEmpty() && (!destroyed && !enabled)) {
			startTask();
		}
	}
	
	public void summon(Player p, int x, int z) {
		if (chunkPos[0]==x && chunkPos[1]==z && p.getWorld().equals(loc.getWorld())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(pc.pl, new Runnable() {
				@Override
				public void run() {
					summon(p);
				}
			},20);
		}
	}
	
	public void remove(Player p) {
		if (destroyPacket==null) return;
		
		if (p==null) {
			Iterator<Player> shownIter = shown.iterator();
			while (shownIter.hasNext()) {
				Player player = shownIter.next();
				shownIter.remove();
				remove(player);
			}
			return;
		}
		if (DimensionsSettings.showPortalContent() && complete.isEntity) {
			destroyPacket.sendPacket(p);
		}
		shown.remove(p);
		complete.removeFromShown(p);
		
		if (shown.isEmpty() && !destroyed && enabled) {
			Bukkit.getScheduler().cancelTask(task);
			Bukkit.getScheduler().cancelTask(task2);
			enabled = false;
		}
		if (DimensionsSettings.showPortalContent()) {
			p.sendBlockChange(loc, Material.AIR.createBlockData());
		}
	}
	
	public void remove(Player p, int x, int z) {
		if (chunkPos[0]==x && chunkPos[1]==z && p.getWorld().equals(loc.getWorld())) {
			remove(p);
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		if (e.getFrom().equals(loc.getWorld())) {
			remove(e.getPlayer());
		}
	}
	
	
	public boolean destroy(boolean remove) {
		
		destroyed = true;
		enabled = false;
		
		HandlerList.unregisterAll(this);

		Bukkit.getScheduler().cancelTask(task);
		Bukkit.getScheduler().cancelTask(task2);
		
		if (remove) {
			loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, fixedLoc, 10, complete.getPortal().getFrameBlockData(complete.isZAxis()));
			loc.getWorld().playSound(fixedLoc, Sound.BLOCK_GLASS_BREAK, 1.0F, 8.0F);
			loc.getBlock().setBlockData(Material.AIR.createBlockData());
		}
		remove(null);
		shown.clear();
        return true;
	}

	public boolean isShown(Player p) {
		return shown.contains(p);
	}
}
