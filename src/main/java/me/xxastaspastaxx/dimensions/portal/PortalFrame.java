package me.xxastaspastaxx.dimensions.portal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerEntityTeleport;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class PortalFrame implements Listener {
	
	PortalClass pc;
	
	private Class<?> blockClass;
	private Class<?> craftBlockDataClass;
	private Method getCombinedIdMethod;
	private Method getStateMethod;
	
	private int fallingBlockId;
	
	private int viewDistance;

	WrapperPlayServerSpawnEntity spawnPacket;
	WrapperPlayServerEntityTeleport teleportPacket;
	WrapperPlayServerEntityMetadata metaPacket;
	WrappedDataWatcher dataWatcher;
	WrapperPlayServerEntityDestroy destroyPacket;
	
	/***************************************************************************************************************************/
	
	CustomPortal portal;
	Location loc;
	int[] chunkPos = new int[2];
	boolean zAxis;
	
	int task;
	HashMap<LivingEntity,Long> timer = new HashMap<LivingEntity,Long>();
	ArrayList<LivingEntity> hold = new ArrayList<LivingEntity>();

	boolean destroyed = false;
	boolean enabled = false;
	
	int task2;
	
	long startLife;
	
	public PortalFrame(PortalClass pc, CustomPortal customPortal, Location location, boolean zAxis) {
		this.portal = customPortal;
		this.loc = location;
		this.chunkPos[0] = loc.getChunk().getX();
		this.chunkPos[1] = loc.getChunk().getZ();
		this.zAxis = zAxis;
		this.pc = pc;
		
		viewDistance = Bukkit.getViewDistance();
		
		startLife = System.currentTimeMillis();
		
		try {
			blockClass = MinecraftReflection.getBlockClass();
			craftBlockDataClass = MinecraftReflection.getCraftBukkitClass("block.data.CraftBlockData");
			getCombinedIdMethod = blockClass.getMethod("getCombinedId",MinecraftReflection.getIBlockDataClass());
			getStateMethod = craftBlockDataClass.getMethod("getState");
			
		} catch (NoSuchMethodException | IllegalArgumentException e) {
			e.printStackTrace();
			return;
		}
		
		fallingBlockId =  (int) (Math.random() * Integer.MAX_VALUE);
		
		reload();
		
		summon(null);

		startTask();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, pc.pl);
	}
	
	public void reload() {
		

		int combinedId = 0;
		try {
			Object nmsBlockData = getStateMethod.invoke(portal.getFrameBlockData(zAxis));
			combinedId = (int) getCombinedIdMethod.invoke(blockClass,nmsBlockData);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			e1.printStackTrace();
		}
		
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
	
	public CustomPortal getPortal() {
		return portal;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public int getFallingBlockID() {
		return fallingBlockId;
	}
	
	public boolean isZAxis() {
		return zAxis;
	}
	
	ArrayList<Player> shown = new ArrayList<Player>();
	
	public void startTask() {

		if (!loc.getChunk().isLoaded() || destroyed || Bukkit.getScheduler().isCurrentlyRunning(task) || Bukkit.getScheduler().isCurrentlyRunning(task2) || enabled) return;
		
		pc.debug("Starting task for portal frame at "+loc, 3);
		
		enabled = true;
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(pc.pl, new Runnable() {
			public void run() {
				
				for (Entity en : loc.getWorld().getNearbyEntities(loc, 1,1,1)) {
					if (!(en instanceof LivingEntity)) continue;
					if (!pc.enableMobsTeleportation() && !(en instanceof Player)) continue;
					if (timer.containsKey(en) || pc.isOnHold((LivingEntity) en)) continue;
					if (en.getLocation().getBlock().equals(loc.getBlock())) {
						int extra = 0;
						if (en instanceof Player && (((Player) en).getGameMode()==GameMode.CREATIVE || ((Player) en).getGameMode()==GameMode.SPECTATOR)) extra = pc.getTeleportDelay()*1000;
						timer.put((LivingEntity) en, System.currentTimeMillis()-extra);
						hold.add((LivingEntity) en);
						pc.addToHold((LivingEntity) en);
					}
				}
				
			    Iterator<Entry<LivingEntity,Long>> timerIterator = timer.entrySet().iterator();
			    while (timerIterator.hasNext()) {
			    	Entry<LivingEntity,Long> entry = timerIterator.next();
			    	LivingEntity en = entry.getKey();
			    	Location eloc = en.getLocation();
			    	if ((eloc.getBlockX()!=loc.getBlockX() || eloc.getBlockY()!=loc.getBlockY() || eloc.getBlockZ()!=loc.getBlockZ()) && (!pc.isPortalAtLocation(en.getLocation()) ||(pc.isPortalAtLocation(en.getLocation()) && !pc.getPortalAtLocation(en.getLocation()).equals(portal)))) {
			    		timerIterator.remove();
					} else if (((System.currentTimeMillis()-timer.get(en))/1000)>=pc.getTeleportDelay()) {
						timerIterator.remove();
						hold.remove(en);
						portal.usePortal(en, false, false);
					}
			    }
			    
			    Iterator<LivingEntity> holdIterator = hold.iterator();
			    while (holdIterator.hasNext()) {
			    	LivingEntity en = holdIterator.next();
					Location eloc = en.getLocation();
					CustomPortal cp = pc.getPortalAtLocation(eloc);
					if (cp==null || !cp.equals(portal)) {
						holdIterator.remove();
						pc.removeFromHold(en);
					}
			    }
			    
				if (pc.enableParticles) {
					portal.spawnParticles(loc);
				}
				
				for (Player p : shown) {
					metaPacket.sendPacket(p);
					teleportPacket.sendPacket(p);
				}
				
			}
		}, 20,20);
		
		if (!portal.getWorld().equals(loc.getWorld()) && portal.canSpawnEntities() && loc.getBlock().getRelative(BlockFace.DOWN).getType()==portal.getMaterial()) {
			task2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(pc.pl, new Runnable() {
				public void run() {
					EntityType type = portal.getEntitySpawn();
					if (type!=null) {
						hold.add((LivingEntity) loc.getWorld().spawnEntity(loc, type));
					}
				}
			}, portal.getEntityDelay(), portal.getEntityDelay());
		}
	}
	
	public void addToHold(LivingEntity en) {
		hold.add(en);
		pc.addToHold(en);
	}
	
	public void summon(Player p) {
		
		if (p!=null && (shown.contains(p) || !p.getWorld().equals(loc.getWorld()))) return;
		if (portal.getFrame().isSolid() || portal.getFrame()==Material.NETHER_PORTAL) {
			
			if (p==null) {
				for (Entity player : loc.getWorld().getNearbyEntities(loc, 16*viewDistance, 255, 16*viewDistance, (player) -> player instanceof Player)) {
					summon((Player) player);
				}
			} else {
				spawnPacket.sendPacket(p);
				teleportPacket.sendPacket(p);
				metaPacket.sendPacket(p);
				shown.add(p);
			}
			loc.getBlock().setType(Material.AIR);
		}
		
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
		if (p!=null && !shown.contains(p)) return;
		if (p==null) {
			for (Player player : shown) {
				destroyPacket.sendPacket(player);
			}
			shown.clear();
		} else {
			destroyPacket.sendPacket(p);
			shown.remove(p);
		}
		
		if (shown.isEmpty() && !destroyed && enabled) {
			Bukkit.getScheduler().cancelTask(task);
			Bukkit.getScheduler().cancelTask(task2);
			enabled = false;
		}
	}
	
	public void remove(Player p, int x, int z) {
		if (chunkPos[0]==x && chunkPos[1]==z && p.getWorld().equals(loc.getWorld())) {
			remove(p);
		}
	}
	
	
	public boolean destroy(boolean remove, boolean deleteNear) {
		
		destroyed = true;
		enabled = false;
		
		HandlerList.unregisterAll(this);

		Bukkit.getScheduler().cancelTask(task);
		Bukkit.getScheduler().cancelTask(task2);
		timer.clear();
		for (LivingEntity en : hold) {
			pc.removeFromHold(en);
		}
		hold.clear();
		
		if (remove) {
			Location fixedLocation = loc.clone().add(0.5,0.5,0.5);
			loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, fixedLocation, 10, portal.getFrameBlockData(zAxis));
			loc.getWorld().playSound(fixedLocation, Sound.BLOCK_GLASS_BREAK, 1.0F, 8.0F);
			loc.getBlock().setType(Material.AIR);
		}
		remove(null);
		shown.clear();

		pc.removeFrame(portal, this, remove);
		if (deleteNear) {
			
			pc.debug("Destroyed portal frame at "+loc,3);
			
			for (BlockFace face : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP})
	        {
	        	if (pc.isPortalAtLocation(loc.getBlock().getRelative(face).getLocation()) && pc.getPortalAtLocation(loc.getBlock().getRelative(face).getLocation()).equals(portal)) {
	        		pc.getFrameAtLocation(loc.getBlock().getRelative(face).getLocation()).destroy(remove, deleteNear);
	        	}
	        }
		}
		
        return true;
	}
}
