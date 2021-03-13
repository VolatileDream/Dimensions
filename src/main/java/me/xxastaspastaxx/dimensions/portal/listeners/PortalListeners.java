package me.xxastaspastaxx.dimensions.portal.listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.block.BlockPhysicsEvent;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.xxastaspastaxx.dimensions.events.DestroyCause;
import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.utils.DimensionsSettings;
import me.xxastaspastaxx.dimensions.utils.DimensionsUtils;

public class PortalListeners implements Listener {
	
	PortalClass portalClass;
	
	public PortalListeners(Plugin pl, PortalClass portalClass) {
		  
		this.portalClass = portalClass;
		
		Bukkit.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	HashMap<Player,Long> clicked = new HashMap<Player,Long>();
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPortalInteract(PlayerInteractEvent e) {
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			try {
				int rad = (int) ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK)?Math.min(Math.ceil(e.getClickedBlock().getLocation().distance(e.getPlayer().getEyeLocation())),5):5);
				List<Block> los = e.getPlayer().getLineOfSight(null, rad);
				for (Block block : los) {
					if (portalClass.isPortalAtLocation(block.getLocation())) {
						e.setCancelled(true);
						break;
					}
				}
			} catch (IllegalStateException ex) {}
		}
		
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	//if (e.getItem() == null) return;
        	Block block = e.getClickedBlock().getRelative(e.getBlockFace());
        	if (!portalClass.isPortalAtLocation(block.getLocation())) {
        		if (portalClass.lightPortal(block.getLocation(), IgniteCause.FLINT_AND_STEEL, e.getPlayer(), e.getItem(), false)) {
					e.setCancelled(true);
					if (DimensionsSettings.consumeItems() && e.getPlayer().getGameMode()!=GameMode.CREATIVE) {
						ItemStack item = e.getItem();
						if (item.getType().toString().contains("BUCKET") && item.getType()!=Material.BUCKET) {
							item.setType(Material.BUCKET);
						} else if (item.getItemMeta() instanceof Damageable) {
							Damageable dmg = (Damageable) item.getItemMeta();
							dmg.setDamage(dmg.getDamage()+1);
							item.setItemMeta((ItemMeta) dmg);
							if (dmg.getDamage()>=item.getType().getMaxDurability()) {
								item.setAmount(item.getAmount()-1);
							}
						} else {
							item.setAmount(item.getAmount()-1);
						}
							
					}
            		clicked.put(e.getPlayer(), System.currentTimeMillis());
        		}
        	} else {
        		e.setCancelled(true);
        	}
        }
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerClick(PlayerAnimationEvent e) {
		Player p = e.getPlayer();

		if (clicked.containsKey(p)) {
			if (System.currentTimeMillis()-clicked.get(p)<500) {
				return;
			} else {
				clicked.remove(p);
			}
		}
		if (e.getAnimationType()==PlayerAnimationType.ARM_SWING) {
			try {
				List<Block> los = p.getLineOfSight(null, 5);
				for (Block block : los) {
					if (!DimensionsUtils.isAir(block.getType()) && !portalClass.getFrameMaterials().contains(block.getType())) break;
					CompletePortal portal = portalClass.getPortalAtLocation(block.getLocation());
					if (portal!=null) {
						portal.getPortal().destroy(portal, false, DestroyCause.PLAYER_FRAME, p);
					}
				}
			} catch (IllegalStateException ex) {}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		e.setCancelled(bucketEvent(e.getPlayer(), e.getBlockClicked().getRelative(e.getBlockFace())));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucketFill(PlayerBucketFillEvent e) {
		e.setCancelled(bucketEvent(e.getPlayer(), e.getBlockClicked().getRelative(e.getBlockFace())));
	}
	
	
	public boolean bucketEvent(Player p, Block eventBlock) {
		try {
			int rad = (int) Math.ceil(eventBlock.getLocation().distance(p.getEyeLocation()));
			List<Block> los = p.getLineOfSight(null, rad);
			for (Block block : los) {
				if (portalClass.isPortalAtLocation(block.getLocation())) {
					return true;
				}
			}
		} catch (IllegalStateException ex) {}
		return false;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onExplode(ExplosionPrimeEvent e) {
		Entity exploder = e.getEntity();
		for (Location loc : portalClass.getPortalLocations()) {
			if (exploder.getWorld()!=loc.getWorld() || exploder.getLocation().distance(loc)>e.getRadius()+2) continue;
			CompletePortal portal = portalClass.getPortalAtLocation(loc);
			if (portal!=null) {
				if (!portal.getPortal().destroy(portal, false, DestroyCause.ENTITY, (exploder instanceof LivingEntity)?(LivingEntity) exploder:null)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onLiquidFlow(BlockFromToEvent e) {
		if (portalClass.isPortalAtLocation(e.getBlock().getLocation()) || portalClass.isPortalAtLocation(e.getToBlock().getLocation())) {
			e.setCancelled(true);
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
			onBlockChange(block,null,DestroyCause.PISTON);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockRedstoneEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(CauldronLevelChangeEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(FluidLevelChangeEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(FurnaceBurnEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(LeavesDecayEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(MoistureChangeEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(SpongeAbsorbEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockBreakEvent e) {
		onBlockChange(e.getBlock(),e.getPlayer(),DestroyCause.PLAYER);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockChange(BlockPhysicsEvent e) {
		onBlockChange(e.getBlock(),null,DestroyCause.BLOCK_PHYSICS);
	}
	
	//BLOCK CHANGE EVENT ^^^^^
	
	public void onBlockChange(Block block, Entity ent, DestroyCause cause) {
		
		if (!portalClass.getBlocks().contains(block.getType())) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(portalClass.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
		        for (BlockFace face : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP})
		        {
		        	Block relative = block.getRelative(face);
		        	CompletePortal portal = portalClass.getPortalAtLocation(relative.getLocation());
		        	if (portal!=null && portal.getPortal().isPortal(portal.getLocation(), true, true)==null) {
		            	portal.destroy(true);
		        	}
		        }
			}
		},1);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e) {
		DamageCause cause = e.getCause();
		if (!((e.getEntity() instanceof LivingEntity) && (cause.equals(DamageCause.SUFFOCATION) || cause.equals(DamageCause.LAVA) || cause.equals(DamageCause.DROWNING) || cause.equals(DamageCause.HOT_FLOOR)))) return;
		if (portalClass.isPortalAtLocation(e.getEntity().getLocation())) e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		
		if (!portalClass.isOnHold(e.getPlayer()))
			portalClass.findBestPathAndUse(e.getPlayer(),e.getFrom(),e.getPlayer().getWorld());
	}
	
}
