package me.xxastaspastaxx.dimensions.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import me.xxastaspastaxx.dimensions.Utils.Dimensions;
import me.xxastaspastaxx.dimensions.Utils.DimensionsSettings;
import me.xxastaspastaxx.dimensions.Utils.Messages;
import me.xxastaspastaxx.dimensions.events.CustomPortalDestroyEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.DestroyCause;
import me.xxastaspastaxx.dimensions.events.EntityTeleportCustomPortalEvent;
import me.xxastaspastaxx.dimensions.events.EntityUseCustomPortalEvent;

public class CustomPortal {
	
	PortalClass portalClass;
	Plugin plugin;
	
	HashMap<UUID, ArrayList<World>> useHistory = new HashMap<UUID, ArrayList<World>>();
	
	String name;
	
	public int maxRadius;
	
	public boolean enabled;
	public String displayName;
	
	public boolean horizontal;
	public Material material;
	public String face;
	public Material frame;
	public Material lighter;

	public World world;
	public int worldHeight;
	public String ratio;
	
	public int minPortalWidth;
	public int minPortalHeight;
	
	public HashMap<EntityType,EntityType> entityTransformation;
	
	public int[] spawningDelay;
	public HashMap<EntityType,Integer> entitySpawning;
	
	public boolean buildExitPortal;
	public boolean spawnOnAir;
	
	public ArrayList<World> disabledWorlds;
	
	public String particlesColor;
	
	public CustomPortal(PortalClass portalClass, String name, boolean enabled, String displayName, boolean horizontal, Material material, String face,
			Material frame, Material lighter, World world, int worldHeight, String ratio, int minPortalWidth, int minPortalHeight,
			HashMap<EntityType,EntityType> entityTransformation, int[] spawningDelay, HashMap<EntityType,Integer> entitySpawning,
			boolean buildExitPortal, boolean spawnOnAir, ArrayList<World> disabledWorlds, String particlesColor, Plugin plugin) {
		
		this.plugin = plugin;
		
		this.portalClass = portalClass;
		this.maxRadius = DimensionsSettings.getMaxRadius();
		
		this.name = name;
		this.enabled = enabled;
		this.displayName = displayName;
		this.horizontal = horizontal;
		this.material = material;
		this.face = face;
		this.frame = frame;
		this.lighter = lighter;
		this.world = world;
		this.worldHeight = worldHeight;
		this.ratio = ratio;
		this.minPortalWidth = minPortalWidth;
		this.minPortalHeight = minPortalHeight;
		this.entityTransformation = entityTransformation;
		this.spawningDelay = spawningDelay;
		this.entitySpawning = entitySpawning;
		this.buildExitPortal = buildExitPortal;
		this.spawnOnAir = spawnOnAir;
		this.disabledWorlds = disabledWorlds;
		this.particlesColor = particlesColor;
	}
	
	public CustomPortal(CustomPortal portal) {
		this.plugin = portal.plugin;
		
		this.portalClass = portal.portalClass;
		this.maxRadius = DimensionsSettings.getMaxRadius();
		
		this.name = portal.name;
		this.enabled = portal.enabled;
		this.displayName = portal.displayName;
		this.horizontal = portal.horizontal;
		this.material = portal.material;
		this.face = portal.face;
		this.frame = portal.frame;
		this.lighter = portal.lighter;
		this.world = portal.world;
		this.worldHeight = portal.worldHeight;
		this.ratio = portal.ratio;
		this.minPortalWidth = portal.minPortalWidth;
		this.minPortalHeight = portal.minPortalHeight;
		this.entityTransformation = portal.entityTransformation;
		this.spawningDelay = portal.spawningDelay;
		this.entitySpawning = portal.entitySpawning;
		this.buildExitPortal = portal.buildExitPortal;
		this.spawnOnAir = portal.spawnOnAir;
		this.disabledWorlds = portal.disabledWorlds;
		this.particlesColor = portal.particlesColor;
	}
	
	public void update(String name, boolean enabled, String displayName, boolean horizontal, Material material, String face,
			Material frame, Material lighter, World world, int worldHeight, String ratio, int minPortalWidth, int minPortalHeight,
			HashMap<EntityType,EntityType> entityTransformation, int[] spawningDelay, HashMap<EntityType,Integer> entitySpawning,
			boolean buildExitPortal, boolean spawnOnAir, ArrayList<World> disabledWorlds, String particlesColor) {
		
		ArrayList<CompletePortal> comps = portalClass.getCompletePortals(this);
		if (comps==null) return;
		for (CompletePortal port : comps) {
			port.remove();
		}
		
		this.name = name;
		this.enabled = enabled;
		this.displayName = displayName;
		this.horizontal = horizontal;
		this.material = material;
		this.face = face;
		this.frame = frame;
		this.lighter = lighter;
		this.world = world;
		this.worldHeight = worldHeight;
		this.ratio = ratio;
		this.minPortalWidth = minPortalWidth;
		this.minPortalHeight = minPortalHeight;
		this.entityTransformation = entityTransformation;
		this.spawningDelay = spawningDelay;
		this.entitySpawning = entitySpawning;
		this.buildExitPortal = buildExitPortal;
		this.spawnOnAir = spawnOnAir;
		this.disabledWorlds = disabledWorlds;
		this.particlesColor = particlesColor;
		
		Iterator<CompletePortal> compsIter = comps.iterator();
		while (compsIter.hasNext()) {
			CompletePortal frame1 = compsIter.next();
			frame1.reload();
			List<Object> portal = isPortal(frame1.getLocation(), true, true);
			if (portal==null) {
				compsIter.remove();
				frame1.destroy(true);
			}
		}
	}
	
	public void disable() {
		enabled = false;
		Iterator<PortalFrame> framesIter = portalClass.getFrames(this).iterator();
		while (framesIter.hasNext()) {
			PortalFrame frame1 = framesIter.next();
			framesIter.remove();
			frame1.destroy(true);
		}
		portalClass.removePortals(this);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public String getDisplayName() {
		return displayName.replace("&", "§");
	}
	
	public boolean isHorizontal() {
		return horizontal;
	}
	
	public Material getMaterial() {
		return this.material;
	}
	
	public BlockFace getFace() {
		try {
			return BlockFace.valueOf(face.toUpperCase());
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public Material getFrame() {
		return this.frame;
	}
	
	
	public Material getLighter() {
		return this.lighter;
	}
	
	public Axis getAxis() {
		try {
			return Axis.valueOf(face.toUpperCase());
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public String getFaceString() {
		return face;
	}
	
	public World getWorld() {
		return world;
	}
	
	public int getWorldHeight() {
		return worldHeight;
	}
	
	
	public int getRatio() {
		int[] ratio = new int[2];
		
		ratio[0] = Integer.parseInt(this.ratio.split(":")[0]);
		ratio[1] = Integer.parseInt(this.ratio.split(":")[1]);
		
		if (ratio[0]>ratio[1]) return ratio[0]/ratio[1];
		else return ratio[1]/ratio[0];
	}
		
	public int getMinPortalWidth() {
		return minPortalWidth;
	}
	
	public int getMinPortalHeight() {
		return minPortalHeight;
	}
	
	public EntityType getEntityTransformation(EntityType type) {
		return entityTransformation.get(type);
	}
	
	public long getEntityDelay() {
		return Dimensions.getRandom(spawningDelay[0],spawningDelay[1]);
	}
	
	public EntityType getEntitySpawn() {
		for (EntityType type : entitySpawning.keySet()) {
			if (Dimensions.getRandom(1,100)<=entitySpawning.get(type)) {
				return type;
			}
		}
		
		return null;
	}
	
	public boolean canSpawnEntities() {
		return entitySpawning.size()>0;
	}
	
	public boolean getBuildExitPortal() {
		return buildExitPortal;
	}
	
	public boolean getSpawnOnAir() {
		return spawnOnAir;
	}
	
	public ArrayList<World> getDisabledWorlds() {
		return disabledWorlds;
	}
	
	public Color getParticlesColor() {
		Color color;
		
		String[] spl = particlesColor.split(";");
		color = Color.fromRGB(Integer.parseInt(spl[0]),Integer.parseInt(spl[1]),Integer.parseInt(spl[2]));
		
		return color;
	}
	
	public CustomPortal clone() {
		return new CustomPortal(this);
	}
	
	public List<Object> isPortal(Location loc, boolean checkEmpty, boolean load) {

		try {
			loc.getBlock();
		} catch (Exception e) {
			return null;
		}

		//if (isHorizontal()) return isHorizontalPortal(loc, checkEmpty, load);
		Location[] results = new Location[4];
		
		//Check if there are blocks matching the portal block at up, down , east and west or south and north
		int up = 0;
		int down = 0;
		
		int west = 0;
		int east = 0;

		int north = 0;
		int south = 0;
		

	    Location location = null;
		if (!isHorizontal()) {
			location = loc.clone();
			for (int blocks = 1; blocks <= maxRadius; blocks++) {
		        location.add(0, -1, 0);
		        if (isPortalBlock(location.getBlock())) {
		        	down = (int) (location.getY()-loc.getY());
		            break;
		        }
		        if(blocks==maxRadius) return null;
			}
			
			location = loc.clone();
			for (int blocks = 1; blocks <= maxRadius-(down-1); blocks++) {
		        location.add(0, 1, 0);
		        if (isPortalBlock(location.getBlock())) {
		        	up = (int) (location.getY()-loc.getY());
		            break;
		        }
		        if(blocks==maxRadius-(down-1)) return null;
			}
		}
		
		location = loc.clone();
		for (int blocks = 1; blocks <= maxRadius; blocks++) {
	        location.add(1, 0, 0);
	        if (isPortalBlock(location.getBlock())) {
	        	east = (int) (location.getX()-loc.getX());
	            break;
	        }
		}
		
		location = loc.clone();
		for (int blocks = 1; blocks <= maxRadius-(east-1); blocks++) {
	        location.add(-1, 0, 0);
	        if (isPortalBlock(location.getBlock())) {
	        	west = (int) (location.getX()-loc.getX());
	            break;
	        }
		}
		
		location = loc.clone();
		for (int blocks = 1; blocks <= maxRadius; blocks++) {
	        location.add(0, 0, 1);
	        if (isPortalBlock(location.getBlock())) {
	        	south = (int) (location.getZ()-loc.getZ());
	            break;
	        }
		}
		
		location = loc.clone();
		for (int blocks = 1; blocks <= maxRadius-(south-1); blocks++) {
	        location.add(0, 0, -1);
	        if (isPortalBlock(location.getBlock())) {
	        	north = (int) (location.getZ()-loc.getZ());
	            break;
	        }
		}

		if ((!isHorizontal() && (up==0 || down==0)) || (isHorizontal() && (west==0 || east==0 || south==0 || north==0))) return null;
		
		//if there are blocks at top,bottom,east/west or north/south then continue checking if the portal sides are complete
		Location min = null;
		Location max = null;
		if (!isHorizontal()) {
			min = new Location(loc.getWorld(), loc.getX(),loc.getY()+down,loc.getZ());
			max = new Location(loc.getWorld(), loc.getX(),loc.getY()+up,loc.getZ());
		} else {
			min = new Location(loc.getWorld(), loc.getX()+west,loc.getY(),loc.getZ()+north);
			max = new Location(loc.getWorld(), loc.getX()+east,loc.getY(),loc.getZ()+south);
		}
		
		Block[][] portal = null;
		boolean accepted = false;
		
		Location extraMin = new Location(loc.getWorld(),0,1,0);
		Location extraMax = new Location(loc.getWorld(),0,-1,0);
		if (!isHorizontal()) {
			extraMin = new Location(loc.getWorld(),0,1,0);
			extraMax = new Location(loc.getWorld(),0,-1,0);
		} else {
			extraMin = new Location(loc.getWorld(),1,0,1);
			extraMax = new Location(loc.getWorld(),-1,0,-1);
		}
		
		if (!isHorizontal()) {
			if (east!=0 && west!=0) {
				min.setX(min.getX()+west);
				extraMin.add(1,0,0);
				max.setX(max.getX()+east);
				extraMax.add(-1,0,0);
				portal = new Block[Math.abs(up)+Math.abs(down)+1][Math.abs(east)+Math.abs(west)+1];
				int layoutSide = 0;
				int layoutY = 0;
				for(int y = (int) Math.max(max.getBlockY(), min.getBlockY()); y >= (int) Math.min(min.getBlockY(), max.getBlockY()); y--) {
					for(int x = (int) Math.max(max.getBlockX(), min.getBlockX()); x >= (int) Math.min(min.getBlockX(), max.getBlockX()); x--) {
						Location blockLocation = new Location(loc.getWorld(),x,y,loc.getZ());
						portal[layoutY][layoutSide] = blockLocation.getBlock();
						layoutSide+=1;
				    }
					layoutSide=0;
					layoutY+=1;
				}
			}
			
			//after a table looking like a portal is created the plugin checks if the width and height are acceptable and also if the blocks are corrent and there is nothing inside
			if (portal!=null) {
				if (isAcceptedPortal(portal, checkEmpty, load)) {
					accepted = true; 
				} else {
					min = new Location(loc.getWorld(), loc.getX(),loc.getY()+down,loc.getZ());
					max = new Location(loc.getWorld(), loc.getX(),loc.getY()+up,loc.getZ());
					portal = null;
					extraMin = new Location(loc.getWorld(),0,1,0);
					extraMax = new Location(loc.getWorld(),0,-1,0);
				}
			}
			
			if (!accepted && north!=0 && south!=0) {
				min.setZ(min.getZ()+north);
				extraMin.add(0,0,1);
				max.setZ(max.getZ()+south);
				extraMax.add(0,0,-1);
				portal = new Block[Math.abs(up)+Math.abs(down)+1][Math.abs(north)+Math.abs(south)+1];
				int layoutSide = 0;
				int layoutY = 0;
				for(int y = (int) Math.max(max.getBlockY(), min.getBlockY()); y >= (int) Math.min(min.getBlockY(), max.getBlockY()); y--) {
					for(int z = (int) Math.max(max.getBlockZ(), min.getBlockZ()); z >= (int) Math.min(min.getBlockZ(), max.getBlockZ()); z--) {
						Location blockLocation = new Location(loc.getWorld(),loc.getX(),y,z);
						portal[layoutY][layoutSide] = blockLocation.getBlock();
						layoutSide+=1;
					}
					layoutSide=0;
					layoutY+=1;
				}
			}
		} else {
			portal = new Block[Math.abs(east)+Math.abs(west)+1][Math.abs(south)+Math.abs(north)+1];
			int layoutZ = 0;
			int layoutX = 0;
			for(int z = (int) Math.max(max.getBlockZ(), min.getBlockZ()); z >= (int) Math.min(min.getBlockZ(), max.getBlockZ()); z--) {
				for(int x = (int) Math.max(max.getBlockX(), min.getBlockX()); x >= (int) Math.min(min.getBlockX(), max.getBlockX()); x--) {
					Location blockLocation = new Location(loc.getWorld(),x,loc.getY(),z);
					try {
						portal[layoutX][layoutZ] = blockLocation.getBlock();
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
					layoutZ+=1;
			    }
				layoutZ=0;
				layoutX+=1;
			}
			
			if (portal!=null) {
				if (isAcceptedPortal(portal, checkEmpty, load)) {
					accepted = true; 
				} else {
					min = new Location(loc.getWorld(), loc.getX()+west,loc.getY(),loc.getZ()+north);
					max = new Location(loc.getWorld(), loc.getX()+east,loc.getY(),loc.getZ()+south);
					portal = null;
					extraMin = new Location(loc.getWorld(),1,0,1);
					extraMax = new Location(loc.getWorld(),-1,0,-1);
				}
			}
		}
		
		//If there was the portal block at every direction portal wouldnt light evenn if it was complete so had to do this twice
		if (portal==null)
			return null;
		
		int maxRadius = DimensionsSettings.getMaxRadius();
		if (portal[0].length<getMinPortalWidth() || portal[0].length>maxRadius || portal.length<getMinPortalHeight() || portal.length>maxRadius) return null;
		
		if (!accepted)
			if (!isAcceptedPortal(portal, checkEmpty, load))
				return null;
		
		

		
		/* acceptable portal layout (default portal)
		S = Stone, A = Air, F = Fire
		
		S S S S
		S A A S
		S A A S
		S A F S
		S S S S */
		
		results[0] = min;
		results[1] = max;
		results[2] = extraMin;
		results[3] = extraMax;
		return Arrays.asList(results, getBlocks(portal), portal);
	}
	
	public List<Block> getBlocks(Block[][] portal) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		for (int y = 0;y<portal.length;y++) {
			for (int side = 0;side<portal[0].length;side++) {
				if ((y==0 && side==0) || (y==portal.length-1 && side==0) || (y==0 && side==portal[0].length-1) || (y==portal.length-1 && side==portal[0].length-1)) continue;
				
				blocks.add(portal[y][side]);
			}
		}
		
		return blocks;
	}
	
	public boolean isAcceptedPortal(Block[][] portal, boolean checkEmpty, boolean load) {
		int maxRadius = DimensionsSettings.getMaxRadius();
		if (portal[0].length<getMinPortalWidth() || portal[0].length>maxRadius || portal.length<getMinPortalHeight() || portal.length>maxRadius) return false;
		
		for (int y = 0;y<portal.length;y++) {
			for (int side = 0;side<portal[0].length;side++) {
				
				//is round the correct material? (skip corners)	
				if ((y==0 && side==0) || (y==portal.length-1 && side==0) || (y==0 && side==portal[0].length-1) || (y==portal.length-1 && side==portal[0].length-1)) continue;
				if ((y==0 || y==portal.length-1) && !isPortalBlock(portal[y][side])) return false;
				if ((side==0 || side==portal[0].length-1) && !isPortalBlock(portal[y][side])) return false;
				
				//is it empty inside?
				if (checkEmpty && (y>0 && y<portal.length-1 && side>0 && side<portal[0].length-1) && (!load && (!Dimensions.isAir(portal[y][side].getType()) || portalClass.getPortalAtLocation(portal[y][side].getLocation())!=null))) return false;
			}
		}
		
		return true;
	}
	
	//Used to check if the block is a block accepted (by config settings)
	public boolean isPortalBlock(Block block) {
		if (block.getType()!=getMaterial()) return false;

		BlockData blockData = block.getBlockData();
		if (blockData instanceof Orientable) {
			Orientable orientable = (Orientable) blockData;
			if (getAxis()!=null) {
				if (!orientable.getAxis().equals(getAxis())) {
					return false;
				}
			}
		} else if (blockData instanceof Directional) {
			Directional directional = (Directional) blockData;
			if (getFace()!=null) {
				if (!directional.getFacing().equals(getFace()) && directional.getFaces().contains(getFace())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	//Fill the center with he frame blocks
	public boolean lightPortal(Location loc, IgniteCause cause, Entity igniter, boolean load, ItemStack lighter) {
		List<Object> portal = isPortal(loc, true, load);
		if (portal==null) return false;
			
		Location[] portalLocations = (Location[]) portal.get(0);
		
		Location min = portalLocations[0].clone().add(portalLocations[2]);
		Location max = portalLocations[1].clone().add(portalLocations[3]);
		
		if (getDisabledWorlds().contains(loc.getWorld())) {
			if (igniter!=null && (igniter instanceof Player)) {
				String message = Messages.get("disabledWorldMesasge");
				if (!message.equalsIgnoreCase("")) {
					igniter.sendMessage(message.replace("%player%", igniter.getName()).replace("%world%", igniter.getWorld().getName()).replace("%portal%", getDisplayName()));
				}
			}
			return false;
		}
		
		if (min.getWorld().equals(getWorld()) && (min.getY()>getWorldHeight() || max.getY()>getWorldHeight())) {
			if (igniter!=null && (igniter instanceof Player)) {
				String message = Messages.get("maxHeightExceededDenyMessage");
				if (!message.equalsIgnoreCase("")) {
					igniter.sendMessage(message.replace("%player%", igniter.getName()).replace("%world%", igniter.getWorld().getName()).replace("%portal%", getDisplayName()).replace("%maxWorldHeight%", ""+getWorldHeight()));
				}
			}
			return false;
		}
		
		ArrayList<PortalFrame> completeFrames = new ArrayList<PortalFrame>();

		CompletePortal complete = new CompletePortal(this, portal,portalLocations[0].getZ()!=portalLocations[1].getZ());
		for(int y = (int) Math.min(max.getBlockY(), min.getBlockY()); y <= (int) Math.max(min.getBlockY(), max.getBlockY()); y++) {
			for(int x = (int) Math.max(max.getBlockX(), min.getBlockX()); x >= (int) Math.min(min.getBlockX(), max.getBlockX()); x--) {
				for(int z = (int) Math.max(max.getBlockZ(), min.getBlockZ()); z >= (int) Math.min(min.getBlockZ(), max.getBlockZ()); z--) {
					Location blockLocation = new Location(loc.getWorld(),x,y,z);
					completeFrames.add(new PortalFrame(complete, blockLocation,complete.isZAxis()));
				}
			}
		}
		complete.setFrames(completeFrames);
			
		CustomPortalIgniteEvent event = new CustomPortalIgniteEvent(complete, cause, igniter, load, lighter);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			
			for (PortalFrame frame : complete.getFrames()) {
				frame.summon(null);
			}

			portalClass.addPortal(complete);
			return true;
		} else {
			complete.destroy(true);
		}

		return false;
	}
	
	public void setBlock(Block block) {
		
		block.setType(getMaterial());
		
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Orientable) {
			Orientable orientable = (Orientable) blockData;
			if (getAxis()!=null) {
				orientable.setAxis(getAxis());
			}
			block.setBlockData(orientable);
		} else if (blockData instanceof Directional) {
			Directional directional = (Directional) blockData;
			if (getFace()!=null) {
				directional.setFacing(getFace());
			}
			block.setBlockData(directional);
		}
		
	}
	
	public BlockData getFrameBlockData(boolean zAxis) {
		BlockData blockData = getFrame().createBlockData();
		if (zAxis) {
			if (blockData instanceof Orientable) {
				Orientable orientable = (Orientable) blockData;
				orientable.setAxis(Axis.Z);
				blockData = orientable;
			} else if (blockData instanceof Directional) {
				Directional directional = (Directional) blockData;
				directional.setFacing(BlockFace.NORTH);
				blockData = directional;
			} else if (blockData instanceof MultipleFacing) {
				MultipleFacing face = (MultipleFacing) blockData;
				face.setFace(BlockFace.NORTH, true);
				face.setFace(BlockFace.SOUTH, true);
				blockData = face;
			}
		} else {
			if (blockData instanceof MultipleFacing) {
				MultipleFacing face = (MultipleFacing) blockData;
				face.setFace(BlockFace.EAST, true);
				face.setFace(BlockFace.WEST, true);
				blockData = face;
			}
		}
		
		return blockData;
	}
	
	public boolean isZAxis(Location loc) {
		List<Object> portal = isPortal(loc, false, false);
		if (portal==null) return false;
		Location[] portalLocations = (Location[]) portal.get(0);
		
		Location min = portalLocations[0];
		Location max = portalLocations[1];
		
		if (min.getZ()!=max.getZ()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean destroy(CompletePortal complete, DestroyCause cuase, Entity entity) {
		if ((entity instanceof Player) && portalClass.getPlugin().getWorldGuardFlags()!=null && !portalClass.getPlugin().getWorldGuardFlags().testState((Player) entity, complete.getLocation(),WorldGuardFlags.DestroyCustomPortal)) {
			entity.sendMessage(Messages.get("worldGuardDenyMessage"));
			return false;
		}
		
		CustomPortalDestroyEvent event = new CustomPortalDestroyEvent(complete, cuase, entity);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			return complete.destroy(true);
		}
		
		return false;
	}
	
	
	//Find the location that the player must go when entering a portal
	public Location calculateTeleportLocation(Entity p, EntityUseCustomPortalEvent event) {
		
		Location loc = event.getLocation();
		Location teleportLocation;

		if (getWorldHeight()>0) loc.setY(loc.getY()/(256/getWorldHeight()));
		if (loc.getWorld().equals(getWorld()) && loc.getY()+4>getWorldHeight()) loc.setY(getWorldHeight()-4);
		
		
		//If player is returning plugin will find the world he got into from. If not he will be teleported to the default world
		if (!loc.getWorld().equals(getWorld())) {
			teleportLocation = new Location(getWorld(), Math.floor(loc.getX())/getRatio(), loc.getY(), Math.floor(loc.getZ())/getRatio());
		} else {
			teleportLocation = new Location(getReturnWorld(p, p.getWorld(), false, true), Math.floor(loc.getX())*getRatio(), loc.getY(), Math.floor(loc.getZ())*getRatio());
		}
		
		if ((p instanceof Player) && portalClass.pl.getWorldGuardFlags()!=null && !portalClass.pl.getWorldGuardFlags().testState((Player) p, loc,WorldGuardFlags.UseCustomPortal)) {
			p.sendMessage(Messages.get("worldGuardDenyMessage"));
			return null;
		}

		
		if (event.isForcedTeleport()) return teleportLocation;
		if (teleportLocation.getX()==Double.POSITIVE_INFINITY || teleportLocation.getX()==Double.NEGATIVE_INFINITY || teleportLocation.getZ()==Double.POSITIVE_INFINITY || teleportLocation.getZ()==Double.NEGATIVE_INFINITY) {
			if (p instanceof Player) p.sendMessage("§4ERROR: §cPortal \""+getDisplayName()+"\"§c maybe has world ratio wrongly set. Change "+ratio.split(":")[0]+":"+ratio.split(":")[1]+" to "+ratio.split(":")[1]+":"+ratio.split(":")[0]+"?. Report this to an admin. X = "+teleportLocation.getX()+", Z = "+teleportLocation.getZ());
			return null;
		}

		//Find exit portal, if there is none create one
		Location nearestLocation = portalClass.getNearestPortalLocation(this,teleportLocation);
		if (nearestLocation!=null) {
			nearestLocation = nearestLocation.clone();
			nearestLocation.setYaw(loc.getYaw());
			nearestLocation.setPitch(loc.getPitch());
			if (nearestLocation.getBlock().getRelative(BlockFace.WEST).getType()!=frame) nearestLocation.add(0.5,0,0);
			if (nearestLocation.getBlock().getRelative(BlockFace.NORTH).getType()!=frame) nearestLocation.add(0,0,0.5);
			event.setZaxis(isZAxis(loc));
			return nearestLocation;
		}
		
		boolean zAxis = isZAxis(loc);
		event.setZaxis(zAxis);
		
		boolean foundLocation = false;

		Location tempLoc = spiralSearch(teleportLocation, zAxis,true);
		if (tempLoc==null) spiralSearch(teleportLocation, zAxis,false);
		if (tempLoc!=null) {
			teleportLocation =  tempLoc;
			foundLocation = true;
		} else {
			while (!foundLocation && teleportLocation.getY()<teleportLocation.getWorld().getHighestBlockYAt(teleportLocation)) {
				while (!Dimensions.isAir(teleportLocation.getBlock().getRelative(BlockFace.UP).getType()) && teleportLocation.getY()<teleportLocation.getWorld().getHighestBlockYAt(teleportLocation)) teleportLocation.add(0,1,0);
				tempLoc = spiralSearch(teleportLocation, zAxis, true);
				if (tempLoc==null) spiralSearch(teleportLocation, zAxis,false);
				if (tempLoc!=null) {
					teleportLocation =  tempLoc;
					foundLocation = true;
				} else {
					teleportLocation.add(0,10,0);
				}
			}
		}
		
		if (!foundLocation) {
			while (Dimensions.isAir(teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()) && teleportLocation.getY()>=1) teleportLocation.add(0,-1,0);
			tempLoc = spiralSearch(teleportLocation, zAxis,true);
			if (tempLoc==null) spiralSearch(teleportLocation, zAxis,false);
			if (tempLoc!=null) {
				teleportLocation =  tempLoc;
				foundLocation = true;
			}
		}
		
		nearestLocation = portalClass.getNearestPortalLocation(this,teleportLocation);
		if (nearestLocation!=null) {
			nearestLocation = nearestLocation.clone();
			nearestLocation.setYaw(loc.getYaw());
			nearestLocation.setPitch(loc.getPitch());
			if (nearestLocation.getBlock().getRelative(BlockFace.WEST).getType()!=frame) nearestLocation.add(0.5,0,0);
			if (nearestLocation.getBlock().getRelative(BlockFace.NORTH).getType()!=frame) nearestLocation.add(0,0,0.5);
			return nearestLocation;
		}

		event.setBuildLocation(teleportLocation);
		
		teleportLocation.add(zAxis ? 0.5f : 0,0,!zAxis ? 0.5f : 1.0f);
		teleportLocation.setYaw(loc.getYaw());
		teleportLocation.setPitch(loc.getPitch());
		return teleportLocation;
	}

	public Location spiralSearch(Location teleportLocation, boolean zAxis,boolean checkPlatform) {
	    int size=DimensionsSettings.getSpotSearchRadius();
	    for (int siz = 4;siz<=size;siz++) {
			for (int y=-siz+3;y<=siz-3;y++) {
				for (int sz=1;sz<=siz;sz++) {
	            	if (teleportLocation.getY()+y<=0 || teleportLocation.getY()+y>=getWorldHeight()) continue;
					int x = 0;
				    int z = 0;
				    int d = 0;
				    int s = 1;
				    
				    for (int k=1; k<=(sz*2-1); k++)
				    {
				        for (int j=0; j<(k<(sz*2-1)?2:3); j++)
				        {
				            for (int i=0; i<s; i++)
				            {
				            	Location blockLocation = new Location(teleportLocation.getWorld(),teleportLocation.getX()+x,teleportLocation.getY()+y,teleportLocation.getZ()+z);

								if (canBuildPortal(blockLocation, zAxis,checkPlatform)) {
									return blockLocation;
								}

				                switch (d)
				                {
				                    case 0: z++; break;
				                    case 1: x++; break;
				                    case 2: z--; break;
				                    case 3: x--; break;
				                }
				            }
				            d = (d+1)%4;
				        }
				        s = s + 1;
				  }
				}
		    }
	    }

	    return null;
	}
	
	public boolean canBuildPortal(Location loc, boolean zAxis, boolean checkPlatform) {
		if (loc.getWorld().equals(getWorld()) && loc.getY()+4>getWorldHeight()) return false;

		if (getBuildExitPortal()) {
			if (!getSpawnOnAir()) {
				for (int i=-1;i<3;i++) {
					if (!loc.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i).getType().isSolid())
						return false;
				}
			}

			if (!isHorizontal()) {
				if (checkPlatform) {
					for (int i=-1;i<2;i+=2) {
						if (!loc.getBlock().getRelative(BlockFace.DOWN).getRelative(zAxis ? BlockFace.WEST : BlockFace.SOUTH,i).getType().isSolid() ||
							!loc.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH).getRelative(zAxis ? BlockFace.WEST : BlockFace.SOUTH,i).getType().isSolid())
							return false;
					}
				}
				
				for (int i=0;i<4;i++) {
					if ((isBadBlock(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH).getType())) ||
						(isBadBlock(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,2).getType())) ||
						(isBadBlock(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH).getType())) ||
						(isBadBlock(loc.getBlock().getRelative(BlockFace.UP,i).getType())))
							return false;
				}
			} else {
				if (checkPlatform) {
					for (int i=-3;i<4;i++) {
						for (int i2=-3;i2<4;i2++) {
							if (!loc.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST, i).getRelative(BlockFace.SOUTH, i2).getType().isSolid())
								return false;
						}
					}
				}
				
				for (int i=-2;i<3;i++) {
					for (int i2=-2;i2<3;i2++) {
						if ((isBadBlock(loc.getBlock().getRelative(BlockFace.EAST, i).getRelative(BlockFace.SOUTH, i2).getType())))
							return false;
					}
				}
			}
		} else {
			if (!Dimensions.isAir(loc.getBlock().getType()) ||
				!Dimensions.isAir(loc.getBlock().getRelative(BlockFace.UP).getType()))
					return false;
		}
		return true;
	}
	
	private boolean isBadBlock(Material mat) {
		return mat.isSolid() || mat==Material.LAVA || mat==Material.WATER;
	}

	public void spawnParticles(Location loc) {
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX()+0.5f,loc.getY()+0.5f,loc.getZ()+0.5f, 3, 0.5,0.5,0.5,new Particle.DustOptions(getParticlesColor(),2));

	}
	
	public boolean usePortal(CompletePortal complete, Entity p, boolean forceTP, World fromWorld, boolean bungee) {
		if (!forceTP && ((p instanceof Player) && portalClass.getPlugin().getWorldGuardFlags()!=null && !portalClass.getPlugin().getWorldGuardFlags().testState((Player) p, p.getLocation(),WorldGuardFlags.UseCustomPortal))) {
			p.sendMessage(Messages.get("worldGuardDenyMessage"));
			return false;
		}
		
		//Disable teleportation if player is in the disabled worlds
		if (getDisabledWorlds().contains(fromWorld)) return false;
		//Call event for custom plugins that want to extend the possibilities of the plugin
		EntityUseCustomPortalEvent event = new EntityUseCustomPortalEvent(p, p.getLocation(), complete, getBuildExitPortal(),forceTP, bungee);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			//Calculate teleport location and teleport the player there
			Location startLocation = event.getLocation();
			if (startLocation.getWorld().equals(getWorld()) && startLocation.getY()>getWorldHeight()) startLocation.setY(getWorldHeight()-5);
			event.setLocation(startLocation);
			Location teleportLocation = null;
			if (complete!=null && !event.isForceCalculate()) teleportLocation = complete.getLinkedLocation();
			if (teleportLocation==null) teleportLocation = calculateTeleportLocation(p, event);
			teleportLocation.setDirection(p.getLocation().getDirection());
			EntityTeleportCustomPortalEvent tpEvent = new EntityTeleportCustomPortalEvent(event,teleportLocation,p.getLocation());
			tpEvent.setCancelled(teleportLocation==null || event.isForcedTeleport() || getDisabledWorlds().contains(teleportLocation.getWorld()));
			Bukkit.getServer().getPluginManager().callEvent(tpEvent);
			if (!tpEvent.isCancelled()) {
				if (event.getBuildLocation()!=null) buildPortal(event);
				teleportLocation = teleportLocation.add(0,(event.getBuildLocation()!=null ? 1:0),(event.getBuildLocation()!=null && event.getZaxis()?0.5:0));
				
				CompletePortal compl = portalClass.getPortalAtLocation(teleportLocation);
				if (compl!=null) {
					compl.addToHold(p);
				} else {
					portalClass.removeFromHold(p);
				}
				if (!(p instanceof Player)) {
					EntityType type = getEntityTransformation(p.getType());
					if (type!=null) {
						if (p instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) teleportLocation.getWorld().spawnEntity(teleportLocation,type);
							
							for (PotionEffect effect : ((LivingEntity) p).getActivePotionEffects()) { entity.addPotionEffect(effect); }
							entity.setCanPickupItems(((LivingEntity) p).getCanPickupItems());
							entity.setCustomName(p.getCustomName());
							entity.getEquipment().setArmorContents(((LivingEntity) p).getEquipment().getArmorContents());
							entity.getEquipment().setItemInMainHand(((LivingEntity) p).getEquipment().getItemInMainHand());
							entity.getEquipment().setItemInOffHand(((LivingEntity) p).getEquipment().getItemInOffHand());
							entity.setFireTicks(p.getFireTicks());
							entity.setHealth(entity.getHealth());
							entity.setTicksLived(p.getTicksLived());
							
							compl.addToHold(entity);
							
							p.remove();
						} else {
							Entity entity = teleportLocation.getWorld().spawnEntity(teleportLocation,type);
							entity.setCustomName(p.getCustomName());
							entity.setFireTicks(p.getFireTicks());
							entity.setTicksLived(p.getTicksLived());
							
							compl.addToHold(entity);
							
							p.remove();
							
						}
						return true;
					}
				}
				
				getReturnWorld(p, fromWorld, true, true);
				addToUsedPortals(p, p.getWorld());
				
				if (p.teleport(teleportLocation)) {
					return true;
				}
			}
		}
		return false;
	}

	public World getReturnWorld(Entity p, World from, boolean update, boolean useDefaultWorld) {
		
		ArrayList<World> history = useHistory.get(p.getUniqueId());
		
		World world = useDefaultWorld ? DimensionsSettings.getDefaultWorld() : getWorld();
		if (from.equals(getWorld())) {
			if (history != null && !history.isEmpty()) {
				world = history.get(0);
				if (update) {
					history.remove(0);
					useHistory.put(p.getUniqueId(), history);
				}
			}
		}
		
		return world;
	}
	
	public void addToUsedPortals(Entity p, World world) {
		if (!useHistory.containsKey(p.getUniqueId())) useHistory.put(p.getUniqueId(), new ArrayList<World>());
		if (!world.equals(getWorld())) {
			useHistory.get(p.getUniqueId()).add(0, world);
		}
	}
	
	public void buildPortal(EntityUseCustomPortalEvent event) {
		if (event.getBuildLocation().getY()>getWorldHeight()) return;
		
		Location teleportLocation = event.getBuildLocation();
		
		if (event.getBuildExitPortal()) {

			boolean zAxis = event.getZaxis();
			
			ArrayList<PortalFrame> completeFrames = new ArrayList<PortalFrame>();
			CompletePortal complete = new CompletePortal(this, isPortal(teleportLocation, true, false),zAxis);
			
			if (!isHorizontal()) {
				
				//build a portal
				boolean added = false;
				if (isPortalBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN)) && isPortalBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH))) {
					added = true;
					teleportLocation.add(0,-1,0);
				}
				if (!teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
					//teleportLocation.add(0,-1,0);
					for (int side = -1;side<2;side++) {
						setBlock(teleportLocation.getBlock().getRelative(!zAxis ? BlockFace.SOUTH : BlockFace.WEST,side));
						setBlock(teleportLocation.getBlock().getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,1).getRelative(!zAxis ? BlockFace.SOUTH : BlockFace.WEST,side));
					}
				}
				
				for (int i=-1;i<3;i++) {
					setBlock(teleportLocation.getBlock().getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i));
					setBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,4).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i));
				}
				
				for (int i=1;i<4;i++) {
					setBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH));
					setBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,2));
		
					completeFrames.add(new PortalFrame(complete, teleportLocation.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH).getLocation(),zAxis));
					completeFrames.add(new PortalFrame(complete, teleportLocation.getBlock().getRelative(BlockFace.UP,i).getLocation(),zAxis));
				}
				

				
				if (added) teleportLocation.add(0,1,0);
			} else {
				for (int i=-2;i<3;i++) {
					for (int i2=-2;i2<3;i2++) {
						if (i==-2 || i==2 || i2==-2 || i2==2)
							setBlock(teleportLocation.getBlock().getRelative(BlockFace.EAST, i).getRelative(BlockFace.SOUTH, i2));
						else
							completeFrames.add(new PortalFrame(complete, teleportLocation.getBlock().getRelative(BlockFace.EAST, i).getRelative(BlockFace.SOUTH, i2).getLocation(),false));
					}
				}
				teleportLocation.add(0,-1,0);
			}

			for (PortalFrame frame : completeFrames) {
				frame.summon(null);
			}
			complete.setFrames(completeFrames);
			portalClass.addPortal(complete);
		} else {
			
			if (teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()==Material.WATER || teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()==Material.LAVA || Dimensions.isAir(teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType())) 
				setBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN));
		}

	}

	public boolean isReturnWorld(Player p, World to) {

		ArrayList<World> history = useHistory.get(p.getUniqueId());
		
		World world = DimensionsSettings.getDefaultWorld();
		if (history != null && !history.isEmpty()) {
			world = history.get(0);
		}

		return world.equals(to);
	}

	public void setHistories(HashMap<UUID, ArrayList<World>> useHistory) {
		this.useHistory = useHistory;
	}
	
	public HashMap<UUID, ArrayList<World>> getHistory() {
		return useHistory;
	}
	
	public boolean equals(CustomPortal other) {
		if (other==null) return false;
		return getName().contentEquals(other.getName());
	}
}
