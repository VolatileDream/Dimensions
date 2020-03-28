package me.xxastaspastaxx.dimensions.portal.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.Plugin;

import me.xxastaspastaxx.dimensions.events.DestroyCause;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class PortalListeners implements Listener {
	
	PortalClass portalClass;
	
	ArrayList<Player> hold = new ArrayList<Player>();

	ArrayList<Material> lighters = new ArrayList<Material>();
	ArrayList<Material> frameMaterials = new ArrayList<Material>();
	ArrayList<Material> blocks = new ArrayList<Material>();
	
	  public PortalListeners(Plugin pl, PortalClass portalClass, ArrayList<Material> lighters, ArrayList<Material> frameMaterials, ArrayList<Material> blocks) {
		  
		  this.portalClass = portalClass;
		  this.lighters = lighters;
		  this.frameMaterials = frameMaterials;
		  this.blocks = blocks;
		  
		  Bukkit.getServer().getPluginManager().registerEvents(this, pl);
	  }
	
 
	  
	@EventHandler
	public void onPortalInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
			int rad = 5;
			if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				rad = (int) e.getClickedBlock().getLocation().distance(e.getPlayer().getLocation());
			}
			try {
				List<Block> los = e.getPlayer().getLineOfSight(null, rad);
				for (Block block : los) {
					if (block.getType()==Material.AIR)  {
						for (Entity en : block.getWorld().getNearbyEntities(block.getLocation(), 1,1,1)) {
							if (!(en instanceof FallingBlock)) break;
							CustomPortal portal = portalClass.getPortalAtLocation(en.getLocation().getBlock().getLocation());
							if (portal!=null) {
								portal.destroy(en.getLocation(), DestroyCause.PLAYER, e.getPlayer());
								return;
							}
						}
					} else if (block.getType()==Material.WATER || block.getType()==Material.LAVA) {
						if (block.getWorld().getNearbyEntities(block.getLocation(), 1,1,1).size()!=0) break;
						CustomPortal portal = portalClass.getPortalAtLocation(block.getLocation());
						if (portal!=null) {
							portal.destroy(block.getLocation(), DestroyCause.PLAYER, e.getPlayer());
						}
						break;
					} else {
						break;
					}
				}
			} catch (IllegalStateException ex) {
				
			}
		}
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			try {
				List<Block> los = e.getPlayer().getLineOfSight(null, 5);
				if (los!=null) {
					for (Block block : los) {
						if (portalClass.getPortalAtLocation(block.getLocation())!=null) {
							e.setCancelled(true);
							break;
						}
					}
				}
			} catch (IllegalStateException ex) {}
		}
		
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	if (e.getItem() == null || !lighters.contains(e.getItem().getType())) return;
        	Block block = e.getClickedBlock().getRelative(e.getBlockFace());
        	if (!portalClass.isPortalAtLocation(block.getLocation())) {
        		portalClass.lightPortal(block.getLocation(), IgniteCause.FLINT_AND_STEEL, e.getPlayer(), e.getItem().getType());
        	} else {
        		e.setCancelled(true);
        	}
        }
	}
	
	@EventHandler
	public void onExplode(ExplosionPrimeEvent e) {
		Entity exploder = e.getEntity();
		for (Location loc : portalClass.getPortalLocations()) {
			if (exploder.getWorld()!=loc.getWorld() || exploder.getLocation().distance(loc)>e.getRadius()+2) continue;
			CustomPortal portal = portalClass.getPortalAtLocation(loc);
			if (portal!=null) {
				if (!portal.destroy(loc, DestroyCause.ENTITY, exploder)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onLiquidFlow(BlockFromToEvent e) {
		CustomPortal portal = portalClass.getPortalAtLocation(e.getBlock().getLocation());
		if (portal!=null) {
			if (portal.getFrame()==e.getBlock().getType()) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent e) {
		if (!portalClass.isNetherPortalEnabled()) {
			e.setCancelled(true);
		}
	}
	
	//BLOCK CHANGE EVENT VVVV
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockFadeEvent e) {
		onBlockChange(e.getBlock(),null);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockGrowEvent e) {
		onBlockChange(e.getBlock(),null);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockBurnEvent e) {
		onBlockChange(e.getBlock(),null);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockPistonExtendEvent e) {
		onBlockChange(e.getBlock(),null);
		for (Block block : e.getBlocks()) {
			onBlockChange(block,null);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockPistonRetractEvent e) {
		for (Block block : e.getBlocks()) {
			e.setCancelled(onBlockChange(block,null));
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockRedstoneEvent e) {
		onBlockChange(e.getBlock(),null);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(CauldronLevelChangeEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(FluidLevelChangeEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(FurnaceBurnEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(LeavesDecayEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(MoistureChangeEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(SpongeAbsorbEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockBreakEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),e.getPlayer()));
	}
	
	//BLOCK CHANGE EVENT ^^^^^
	
	public boolean onBlockChange(Block block, Entity ent) {
		
		if (!blocks.contains(block.getType())) return false;
		
        for (BlockFace face : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP})
        {
        	Block relative = block.getRelative(face);
        	CustomPortal portal = portalClass.getPortalAtLocation(relative.getLocation());
        	if (portal!=null) {
        		if (!portal.destroy(relative.getLocation(), (ent==null)? DestroyCause.UNKNOWN:(ent instanceof Player)?DestroyCause.PLAYER:DestroyCause.ENTITY, ent)) {
        			return true;
        		}
        	}
        }
        
        return false;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e) {
		DamageCause cause = e.getCause();
		if (!((e.getEntity() instanceof Player) && (cause.equals(DamageCause.SUFFOCATION) || cause.equals(DamageCause.LAVA) || cause.equals(DamageCause.DROWNING) || cause.equals(DamageCause.HOT_FLOOR))))	return;
		Location loc = e.getEntity().getLocation().clone();
		loc = new Location(loc.getWorld(),loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if (portalClass.isPortalAtLocation(loc)) e.setCancelled(true);
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		
		if ((portalClass.getPortalAtLocation(e.getFrom())==null || portalClass.getPortalAtLocation(e.getTo())==null) && !hold.contains(e.getPlayer()))
			portalClass.findBestPathAndUse(e.getPlayer(),e.getFrom().getWorld(),e.getTo().getWorld());
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		portalClass.findBestPathAndUse(e.getPlayer(),e.getPlayer().getWorld(),e.getRespawnLocation().getWorld());
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent e) {
		
		//Check if the player is in a portal so it can be used
		//or if they are not in the portal anymore so they can be removed from the list to prevent teleporting them back and forth
        if (isDifferentBlockLocation(e.getFrom(), e.getTo())) {
        	Player p = e.getPlayer();
        	
    		Location loc = e.getTo();
        	CustomPortal portal = portalClass.getPortalAtLocation(new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()));
        	if (portal==null) {
        		if (portalClass.isOnTimer(p)) portalClass.removeFromTimer(p);
        		if (hold.contains(p)) hold.remove(p);
        		return;
        	}
        	if (portal.getFrame()==Material.WATER || portal.getFrame()==Material.LAVA) {
            	if (loc.getBlock().getType()==portal.getFrame()) {
    				if (!hold.contains(p)) {
    	                hold.add(p);
    					portalClass.addToTimer(p, portal);
    				}
            	} else {
            		if (hold.contains(p)) hold.remove(p);
            	}
        	} else {
            	for (Entity en : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
            		if (!(en instanceof FallingBlock)) continue;
            		FallingBlock fallingBlock = (FallingBlock) en;
            		
                	if (fallingBlock.getBlockData().getMaterial()==portal.getFrame()) {
        				if (!hold.contains(p)) {
        	                hold.add(p);
        					portalClass.addToTimer(p, portal);
        				}
                	} else {
                		if (hold.contains(p)) hold.remove(p);
                	}
            	}
        	}
        }
		
	}
	
	public boolean isDifferentBlockLocation(Location location1, Location location2) {
		if (location1.getBlockX()!=location2.getBlockX() || location1.getBlockY()!=location2.getBlockY() || location1.getBlockZ()!=location2.getBlockZ()) {
			if (location2.getBlock().getType()==Material.AIR || frameMaterials.contains(location2.getBlock().getType())) {
				return true;
			}
		}
		return false;
	}

}
