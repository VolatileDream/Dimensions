package me.xxastaspastaxx.dimensions.portal;

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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import me.xxastaspastaxx.dimensions.Dimensions;

public class PortalFrame implements Listener {
	
	PortalClass pc;
	CustomPortal portal;
	Location loc;
	FallingBlock fallingBlock;
	boolean zAxis;

	int task;
	HashMap<LivingEntity,Long> timer = new HashMap<LivingEntity,Long>();
	ArrayList<LivingEntity> hold = new ArrayList<LivingEntity>();

	boolean destroyed = false;
	boolean enabled = false;
	
	int task2;
	
	public PortalFrame(PortalClass pc, CustomPortal customPortal, Location location, boolean zAxis) {
		this.portal = customPortal;
		this.loc = location;
		this.zAxis = zAxis;
		this.pc = pc;
		
		Block block = loc.getBlock();
		block.setBlockData(customPortal.getFrameBlockData(block, zAxis));
		if (loc.getChunk().isLoaded() && (block.getType().isSolid() || block.getType()==Material.NETHER_PORTAL)) {
			fallingBlock = block.getLocation().getWorld().spawnFallingBlock(block.getLocation().add(0.5f,0,0.5f), block.getBlockData());
			fallingBlock.setGravity(false);
			fallingBlock.setDropItem(false);
			fallingBlock.setHurtEntities(false);
			fallingBlock.setTicksLived(Integer.MAX_VALUE);
			block.setType(Material.AIR);
		}

		startTask();
		
		
		Bukkit.getServer().getPluginManager().registerEvents(this, pc.pl);
	}
	
	public CustomPortal getPortal() {
		return portal;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public FallingBlock getFallingBlock() {
		return fallingBlock;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e) {
		if (destroyed || enabled) return;
		
		if (loc.getChunk().equals(e.getChunk())) {
			startTask();
			summon();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onChuckUnload(ChunkUnloadEvent e) {
		if (destroyed || !enabled) return;
		
		if (loc.getChunk().equals(e.getChunk())) {
			Bukkit.getScheduler().cancelTask(task);
			Bukkit.getScheduler().cancelTask(task2);
			remove();
		}
	}
	
	public void startTask() {

		if (!loc.getChunk().isLoaded() || destroyed || Bukkit.getScheduler().isCurrentlyRunning(task) || Bukkit.getScheduler().isCurrentlyRunning(task2)) return;
		
		enabled = true;
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(pc.pl, new Runnable() {
			
			public void run() {
				
				if (fallingBlock!=null && !Dimensions.isAir(loc.getBlock().getType())) {
					loc.getBlock().setType(Material.AIR);
				}
				
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
					//en.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, pc.getTeleportDelay()*25, Integer.MAX_VALUE,false,false,false));
			    	if ((eloc.getBlockX()!=loc.getBlockX() || eloc.getBlockY()!=loc.getBlockY() || eloc.getBlockZ()!=loc.getBlockZ()) && (!pc.isPortalAtLocation(en.getLocation()) ||(pc.isPortalAtLocation(en.getLocation()) && !pc.getPortalAtLocation(en.getLocation()).equals(portal)))) {
			    		timerIterator.remove();
					} else if (((System.currentTimeMillis()-timer.get(en))/1000)>=pc.getTeleportDelay()) {
						timerIterator.remove();
						hold.remove(en);
						pc.removeFromHold(en);
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
				
				if (portal.getFrame().isSolid() || portal.getFrame()==Material.NETHER_PORTAL) {
					if (fallingBlock==null || !isSameLocation()) {
						destroy();
					}
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
	
	public boolean isSameLocation() {
		Location entityLocation = fallingBlock.getLocation().clone().add(-0.5,0,-0.5);
		entityLocation.setY(Math.floor(entityLocation.getY()));
		if (entityLocation.equals(loc)) return true;
		
		return false;
	}
	
	public void addToHold(LivingEntity en) {
		hold.add(en);
		pc.addToHold(en);
	}
	
	public void remove() {
		loc.getBlock().setType(Material.AIR);
		if (fallingBlock!=null) fallingBlock.remove();
		fallingBlock = null;
		enabled = false;
	}
	
	public void summon() {
		
		Block block = loc.getBlock();
		block.setBlockData(portal.getFrameBlockData(block, zAxis));
		if (block.getType().isSolid() || block.getType()==Material.NETHER_PORTAL) {
			fallingBlock = block.getLocation().getWorld().spawnFallingBlock(block.getLocation().add(0.5f,0,0.5f), block.getBlockData());
			fallingBlock.setGravity(false);
			fallingBlock.setDropItem(false);
			fallingBlock.setHurtEntities(false);
			fallingBlock.setTicksLived(Integer.MAX_VALUE);
			block.setType(Material.AIR);
		}
	}
	
	public boolean destroy() {
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
		
		Block block = loc.getBlock();
		loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 10, portal.getFrameBlockData(block, false));
		loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 8.0F);
		remove();
		pc.removeFrame(portal, this);
		
	        for (BlockFace face : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP})
	        {
	        	if (pc.isPortalAtLocation(loc.getBlock().getRelative(face).getLocation()) && pc.getPortalAtLocation(loc.getBlock().getRelative(face).getLocation()).equals(portal)) {
	        		pc.getFrameAtLocation(loc.getBlock().getRelative(face).getLocation()).destroy();
	        	}
	        }
        
        return true;
	}
}
