package me.xxastaspastaxx.dimensions.portal.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.Plugin;

import me.xxastaspastaxx.dimensions.events.DestroyCause;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;

public class PortalListeners implements Listener {
	
	PortalClass portalClass;

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
	
 
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerClick(PlayerAnimationEvent e) {
		if (e.getAnimationType()==PlayerAnimationType.ARM_SWING) {
			int rad = 5;
			try {
				List<Block> los = e.getPlayer().getLineOfSight(null, rad);
				for (Block block : los) {
					if (block.getType()!=Material.AIR && !frameMaterials.contains(block.getType())) break;
					CustomPortal portal = portalClass.getPortalAtLocation(block.getLocation());
					if (portal!=null) {
						portal.destroy(block.getLocation(), DestroyCause.PLAYER, e.getPlayer());
						break;
					}
				}
			} catch (IllegalStateException ex) {
				
			}
		}
	}
	  
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPortalInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {

		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			try {
				int rad = (int) ((e.getAction() == Action.RIGHT_CLICK_BLOCK)?Math.min(Math.ceil(e.getClickedBlock().getLocation().distance(e.getPlayer().getLocation())),5):5);
				List<Block> los = e.getPlayer().getLineOfSight(null, rad);
				for (Block block : los) {
					CustomPortal portal = portalClass.getPortalAtLocation(block.getLocation());
					if (portal!=null) {
						e.setCancelled(true);
						break;
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
	
	@EventHandler(ignoreCancelled = true)
	public void onExplode(ExplosionPrimeEvent e) {
		Entity exploder = e.getEntity();
		for (Location loc : portalClass.getPortalLocations()) {
			if (exploder.getWorld()!=loc.getWorld() || exploder.getLocation().distance(loc)>e.getRadius()+2) continue;
			CustomPortal portal = portalClass.getPortalAtLocation(loc);
			if (portal!=null) {
				if (!portal.destroy(loc,DestroyCause.ENTITY, (LivingEntity) exploder)) {
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
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockGrowEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockBurnEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockPistonExtendEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.PISTON);
		for (Block block : e.getBlocks()) {
			onBlockChange(block,null,DestroyCause.PISTON);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockPistonRetractEvent e) {
		for (Block block : e.getBlocks()) {
			e.setCancelled(onBlockChange(block,null,DestroyCause.PISTON));
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockRedstoneEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(CauldronLevelChangeEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(FluidLevelChangeEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(FurnaceBurnEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(LeavesDecayEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(MoistureChangeEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(SpongeAbsorbEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockBreakEvent e) {
		e.setCancelled(onBlockChange(e.getBlock(),e.getPlayer(),DestroyCause.PLAYER));
	}
	
	//BLOCK CHANGE EVENT ^^^^^
	
	public boolean onBlockChange(Block block, Entity ent, DestroyCause cause) {
		
		if (!blocks.contains(block.getType())) return false;
		
		boolean destroyed = false;
        for (BlockFace face : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP})
        {
        	Block relative = block.getRelative(face);
        	CustomPortal portal = portalClass.getPortalAtLocation(relative.getLocation());
        	if (portal!=null) {
        		destroyed = !portal.destroy(relative.getLocation(),cause, (LivingEntity) ent);
        	}
        }
        
        return destroyed;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e) {
		DamageCause cause = e.getCause();
		if (!((e.getEntity() instanceof LivingEntity) && (cause.equals(DamageCause.SUFFOCATION) || cause.equals(DamageCause.LAVA) || cause.equals(DamageCause.DROWNING) || cause.equals(DamageCause.HOT_FLOOR)))) return;
		Location loc = e.getEntity().getLocation().clone();
		loc = new Location(loc.getWorld(),loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if (portalClass.isPortalAtLocation(loc)) e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {
		
		if ((portalClass.getPortalAtLocation(e.getFrom())==null || portalClass.getPortalAtLocation(e.getTo())==null) /*&& !portalClass.isOnHold(e.getPlayer())*/)
			portalClass.findBestPathAndUse(e.getPlayer(),e.getFrom().getWorld(),e.getTo().getWorld());
	}

	@EventHandler(ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent e) {
		portalClass.findBestPathAndUse(e.getPlayer(),e.getPlayer().getWorld(),e.getRespawnLocation().getWorld());
	}
}
