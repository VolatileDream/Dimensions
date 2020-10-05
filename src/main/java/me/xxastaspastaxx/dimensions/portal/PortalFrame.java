package me.xxastaspastaxx.dimensions.portal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
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
	
	BlockData netherBlockData;
	
	/***************************************************************************************************************************/
	
	CustomPortal portal;
	Location loc;
	Location fixedLoc;
	int[] chunkPos = new int[2];
	boolean zAxis;
	
	int task;
	HashMap<Entity,Long> timer = new HashMap<Entity,Long>();
	ArrayList<Entity> hold = new ArrayList<Entity>();

	boolean destroyed = false;
	boolean enabled = false;
	boolean isEntity = false;
	
	int task2;
	
	long startLife;
	
	HashMap<Object, Object> tags = new HashMap<Object, Object>();
	
	public PortalFrame(PortalClass pc, CustomPortal customPortal, Location location, boolean zAxis) {
		this.portal = customPortal;
		this.loc = location;
		this.fixedLoc = loc.clone().add(0.5,0.5,0.5);
		this.chunkPos[0] = loc.getChunk().getX();
		this.chunkPos[1] = loc.getChunk().getZ();
		this.zAxis = zAxis;
		this.pc = pc;
		
		viewDistance = Bukkit.getViewDistance();
		
		startLife = System.currentTimeMillis();
		
		if (portal.getFrame().isSolid() || portal.getFrame()==Material.NETHER_PORTAL) {
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
			
			isEntity = true;
		}
		
		Orientable orientable = (Orientable) Material.NETHER_PORTAL.createBlockData();
		orientable.setAxis(zAxis ? Axis.Z : Axis.X);
		netherBlockData = orientable;
		
		reload();
		
		summon(null);

		startTask();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, pc.pl);
	}
	
	public void reload() {
		
		if (!isEntity) return;
		
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
				
				if (shown.size()==0) return;
				
				if (isEntity) {
					loc.getBlock().setType(Material.AIR);
					for (Player p : shown) {
						p.sendBlockChange(loc, Material.AIR.createBlockData());
					}
				} else {
					loc.getBlock().setBlockData(portal.getFrameBlockData(zAxis));
					for (Player p : shown) {
						p.sendBlockChange(loc,portal.getFrameBlockData(zAxis));
					}
				}
				
				for (Entity en : loc.getWorld().getNearbyEntities(fixedLoc, 0.5,0.5,0.5)) {
					if (!(en instanceof Entity)) continue;
					if ((!pc.enableMobsTeleportation() && !(en instanceof Player)) || (!pc.enableEntitiesTeleportation() && !(en instanceof LivingEntity))) continue;
					if (en instanceof Player && pc.enableNetherPortalEffect()) ((Player) en).sendBlockChange(loc, netherBlockData);
					if (timer.containsKey(en) || pc.isOnHold(en)) continue;
					if (en.getLocation().getBlock().equals(loc.getBlock())) {
						int extra = 0;
						if (en instanceof Player && (((Player) en).getGameMode()==GameMode.CREATIVE || ((Player) en).getGameMode()==GameMode.SPECTATOR)) extra = pc.getTeleportDelay()*1000;
						timer.put(en, System.currentTimeMillis()-extra);
						hold.add(en);
						pc.addToHold(en);
					}
				}
				
			    Iterator<Entry<Entity,Long>> timerIterator = timer.entrySet().iterator();
			    while (timerIterator.hasNext()) {
			    	Entry<Entity,Long> entry = timerIterator.next();
			    	Entity en = entry.getKey();
			    	Location eloc = en.getLocation();
			    	if ((eloc.getBlockX()!=loc.getBlockX() || eloc.getBlockY()!=loc.getBlockY() || eloc.getBlockZ()!=loc.getBlockZ()) && (!pc.isPortalAtLocation(en.getLocation()) ||(pc.isPortalAtLocation(en.getLocation()) && !pc.getPortalAtLocation(en.getLocation()).equals(portal)))) {
			    		timerIterator.remove();
					} else if (!timer.containsKey(en)) {
						try {
							timerIterator.remove();
							hold.remove(en);
						} catch (ConcurrentModificationException e) { }
					} else if (((System.currentTimeMillis()-timer.get(en))/1000)>=pc.getTeleportDelay()) {
						if (portal.usePortal(en, false, en.getWorld(), false)) {
							try {
								timerIterator.remove();
								hold.remove(en);
							} catch (ConcurrentModificationException e) { }
						}
					}
			    }
			    
			    Iterator<Entity> holdIterator = hold.iterator();
			    while (holdIterator.hasNext()) {
			    	Entity en = holdIterator.next();
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
				
				if (isEntity) {
					for (Player p : shown) {
						metaPacket.sendPacket(p);
						teleportPacket.sendPacket(p);
					}
				}
				
			}
		}, 20,20);
		
		if (!portal.getWorld().equals(loc.getWorld()) && portal.canSpawnEntities() && loc.getBlock().getRelative(BlockFace.DOWN).getType()==portal.getMaterial()) {
			task2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(pc.pl, new Runnable() {
				public void run() {
					EntityType type = portal.getEntitySpawn();
					if (type!=null) {
						hold.add(loc.getWorld().spawnEntity(loc, type));
					}
				}
			}, portal.getEntityDelay(), portal.getEntityDelay());
		}
	}
	
	public void addToHold(Entity en) {
		hold.add(en);
		pc.addToHold(en);
	}
	
	public void summon(Player p) {
		if (destroyed) return;

		if (p!=null && (shown.contains(p) || !p.getWorld().equals(loc.getWorld()))) return;
		if (p==null) {
			for (Entity player : loc.getWorld().getNearbyEntities(fixedLoc, 16*viewDistance, 255, 16*viewDistance, (player) -> player instanceof Player)) {
				summon((Player) player);
			}
			return;
		}
		if (isEntity) {
			spawnPacket.sendPacket(p);
			teleportPacket.sendPacket(p);
			metaPacket.sendPacket(p);
		}
		shown.add(p);
		
		if (!shown.isEmpty() && (!destroyed && !enabled)) {
			startTask();
			if (!isEntity) {
				loc.getBlock().setBlockData(portal.getFrameBlockData(zAxis));
			}
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
		if (p==null) {
			Iterator<Player> shownIter = shown.iterator();
			while (shownIter.hasNext()) {
				Player player = shownIter.next();
				shownIter.remove();
				remove(player);
			}
			return;
		}
		if (isEntity) {
			destroyPacket.sendPacket(p);
		}
		p.sendBlockChange(loc, Material.AIR.createBlockData());
		shown.remove(p);
		
		if (shown.isEmpty() && !destroyed && enabled) {
			Bukkit.getScheduler().cancelTask(task);
			Bukkit.getScheduler().cancelTask(task2);
			enabled = false;
			loc.getBlock().setBlockData(Material.AIR.createBlockData());
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
	
	
	public boolean destroy(boolean remove, boolean deleteNear) {
		
		destroyed = true;
		enabled = false;
		
		HandlerList.unregisterAll(this);

		Bukkit.getScheduler().cancelTask(task);
		Bukkit.getScheduler().cancelTask(task2);
		timer.clear();
		for (Entity en : hold) {
			pc.removeFromHold(en);
		}
		hold.clear();
		
		if (remove) {
			loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, fixedLoc, 10, portal.getFrameBlockData(zAxis));
			loc.getWorld().playSound(fixedLoc, Sound.BLOCK_GLASS_BREAK, 1.0F, 8.0F);
			loc.getBlock().setBlockData(Material.AIR.createBlockData());
		}
		remove(null);
		shown.clear();

		pc.removeFrame(portal, this, remove);
		if (deleteNear) {
			
			pc.debug("Destroyed portal frame at "+loc,3);
			
			for (BlockFace face : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP})
	        {
				CustomPortal found = pc.getPortalAtLocation(loc.getBlock().getRelative(face).getLocation());
	        	if (found!=null && found.equals(portal)) {
	        		pc.getFrameAtLocation(loc.getBlock().getRelative(face).getLocation()).destroy(remove, deleteNear);
	        	}
	        }
		}
		
        return true;
	}

	public boolean isShown(Player p) {
		return shown.contains(p);
	}
	
	public PortalFrame getBottomFrame() {

		if (portal.isHorizontal()) return this;
		PortalFrame current = this;
		while (!current.isOnGround()) current = pc.getFrameAtLocation(current.getLocation().clone().add(0,-1,0));
		
		return current;
	}
	
	public PortalFrame getRightFrame() {

		PortalFrame current = this;
		while (pc.getPortalAtLocation(current.getLocation().getBlock().getRelative(BlockFace.EAST).getLocation())!=null) current = pc.getFrameAtLocation(current.getLocation().clone().add(1,0,0));
		while (pc.getPortalAtLocation(current.getLocation().getBlock().getRelative(BlockFace.SOUTH).getLocation())!=null) current = pc.getFrameAtLocation(current.getLocation().clone().add(0,0,1));
		
		return current;
	}
	
	public PortalFrame getBottomRightFrame() {
		return getBottomFrame().getRightFrame();
	}
	
	public boolean isOnGround() {
		if (portal.isHorizontal()) return true;
		return pc.getPortalAtLocation(loc.getBlock().getRelative(BlockFace.DOWN).getLocation())==null;
	}
	
	public Object getTag(Object key) {
		return tags.get(key);
	}
	
	public void setTag(Object key, Object value) {
		tags.put(key, value);
	}
	
	public ArrayList<PortalFrame> getCompletePortal() {
		ArrayList<PortalFrame> result = new ArrayList<PortalFrame>();
		for (BlockFace face : (portal.isHorizontal()?new BlockFace[]{BlockFace.NORTH}:new BlockFace[]{BlockFace.WEST, BlockFace.NORTH})) {
			PortalFrame bottomCurrent = getBottomRightFrame();
			while (bottomCurrent!=null) {
				PortalFrame current = bottomCurrent;
				while (current!=null) {
					if (!result.contains(current)) 
						result.add(current);
					current = pc.getFrameAtLocation(current.getLocation().getBlock().getRelative(portal.isHorizontal()?BlockFace.WEST:BlockFace.UP).getLocation());
				}
				bottomCurrent = pc.getFrameAtLocation(bottomCurrent.getLocation().getBlock().getRelative(face).getLocation());
			}
		}
		
		return result;
	}
}
