package me.xxastaspastaxx.dimensions.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.plugin.Plugin;

import me.xxastaspastaxx.dimensions.events.CustomPortalDestroyEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.DestroyCause;
import me.xxastaspastaxx.dimensions.events.EntityTeleportCustomPortalEvent;
import me.xxastaspastaxx.dimensions.events.EntityUseCustomPortalEvent;

public class CustomPortal {
	
	PortalClass portalClass;
	Plugin plugin;
	
	String name;
	
	int maxRadius;
	
	boolean enabled;
	String displayName;
	
	Material material;
	String face;
	Material frame;
	Material lighter;
	World world;
	String ratio;
	
	int minPortalWidth;
	int minPortalHeight;
	
	boolean buildExitPortal;
	boolean spawnOnAir;
	
	ArrayList<World> disabledWorlds;
	
	String particlesColor;
	
	public CustomPortal(PortalClass portalClass, String name, boolean enabled, String displayName, Material material, String face,
			Material frame, Material lighter, World world, String ratio, int minPortalWidth, int minPortalHeight,
			boolean buildExitPortal, boolean spawnOnAir, ArrayList<World> disabledWorlds, String particlesColor, Plugin plugin) {
		
		this.plugin = plugin;
		
		this.portalClass = portalClass;
		this.maxRadius = portalClass.getMaxRadius();
		
		this.name = name;
		this.enabled = enabled;
		this.displayName = displayName;
		this.material = material;
		this.face = face;
		this.frame = frame;
		this.lighter = lighter;
		this.world = world;
		this.ratio = ratio;
		this.minPortalWidth = minPortalWidth;
		this.minPortalHeight = minPortalHeight;
		this.buildExitPortal = buildExitPortal;
		this.spawnOnAir = spawnOnAir;
		this.disabledWorlds = disabledWorlds;
		this.particlesColor = particlesColor;
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
	
	public World getWorld() {
		return world;
	}
	
	public int getRatio() {
		int[] ratio = new int[2];
		
		ratio[0] = Integer.parseInt(this.ratio.split(":")[0]);
		ratio[1] = Integer.parseInt(this.ratio.split(":")[1]);
		
		return ratio[1]/ratio[0];
	}
		
	public int getMinPortalWidth() {
		return minPortalWidth;
	}
	
	public int getMinPortalHeight() {
		return minPortalHeight;
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
	
	public List<Object> isPortal(Location loc, boolean checkEmpty, boolean load) {
		try {
			loc.getBlock();
		} catch (Exception e) {
			return null;
		}
		
		
		Location[] results = new Location[4];
		
		//Check if there are blocks matching the portal block at up, down , east and west or south and north
		int up = 0;
		int down = 0;
		
		int west = 0;
		int east = 0;

		int north = 0;
		int south = 0;
		

	    Location location = loc.clone();
		for (int blocks = 1; blocks <= maxRadius; blocks++) {
	        location.add(0, -1, 0);
	        if (isPortalBlock(location.getBlock())) {
	        	down = (int) (location.getY()-loc.getY());
	            break;
	        }
		}
		
		location = loc.clone();
		for (int blocks = 1; blocks <= maxRadius; blocks++) {
	        location.add(0, 1, 0);
	        if (isPortalBlock(location.getBlock())) {
	        	up = (int) (location.getY()-loc.getY());
	            break;
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
		for (int blocks = 1; blocks <= maxRadius; blocks++) {
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
		for (int blocks = 1; blocks <= maxRadius; blocks++) {
	        location.add(0, 0, -1);
	        if (isPortalBlock(location.getBlock())) {
	        	north = (int) (location.getZ()-loc.getZ());
	            break;
	        }
		}

		if (up==0 || down==0) return null;
		
		//if there are blocks at top,bottom,east/west or north/south then continue checking if the portal sides are complete
		Location min = new Location(loc.getWorld(), loc.getX(),loc.getY()+down,loc.getZ());
		Location max = new Location(loc.getWorld(), loc.getX(),loc.getY()+up,loc.getZ());
		
		Block[][] portal = null;
		boolean accepted = false;
		
		Location extraMin = new Location(loc.getWorld(),0,1,0);
		Location extraMax = new Location(loc.getWorld(),0,-1,0);

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
		
		if (north!=0 && south!=0 && !accepted) {
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
		
		//If there was the portal block at every direction portal wouldnt light evenn if it was complete so had to do this twice
		if (portal==null)
			return null;
		
		if (portal[0].length<getMinPortalWidth() || portal[0].length>portalClass.maxRadius || portal.length<getMinPortalHeight() || portal.length>portalClass.maxRadius) return null;
		
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
		return Arrays.asList(results, getBlocks(portal));
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
		if (portal[0].length<getMinPortalWidth() || portal[0].length>portalClass.maxRadius || portal.length<getMinPortalHeight() || portal.length>portalClass.maxRadius) return false;
		
		for (int y = 0;y<portal.length;y++) {
			for (int side = 0;side<portal[0].length;side++) {
				
				//is round the correct material? (skip corners)
				if ((y==0 && side==0) || (y==portal.length-1 && side==0) || (y==0 && side==portal[0].length-1) || (y==portal.length-1 && side==portal[0].length-1)) continue;
				if ((y==0 || y==portal.length-1) && !isPortalBlock(portal[y][side])) return false;
				if ((side==0 || side==portal[0].length-1) && !isPortalBlock(portal[y][side])) return false;
				
				//is it empty inside?
				if (checkEmpty && (y>0 && y<portal.length-1 && side>0 && side<portal[0].length-1) && (!load && (portal[y][side].getType()!=Material.AIR || portalClass.getPortalAtLocation(portal[y][side].getLocation())!=null))) return false;
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
	public boolean lightPortal(Location loc, IgniteCause cause, LivingEntity igniter, boolean load) {

		List<Object> portal = isPortal(loc, true, load);
		if (portal==null) return false;
		if (getFrame()==Material.WATER || getFrame()==Material.LAVA) {
			if (loc.getBlock().getType()!=Material.AIR) return false;
		} else {
			for (Entity en : loc.getWorld().getNearbyEntities(loc, 1,1,1)) {
				if (en instanceof FallingBlock) return false;
			}
		}
		
		@SuppressWarnings("unchecked")
		List<Block> blocks = (List<Block>) portal.get(1);
		
		CustomPortalIgniteEvent event = new CustomPortalIgniteEvent(loc, this, blocks, cause, igniter,load);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			
			Location[] portalLocations = (Location[]) portal.get(0);
			
			Location min = portalLocations[0].add(portalLocations[2]);
			Location max = portalLocations[1].add(portalLocations[3]);

			for(int y = (int) Math.min(max.getBlockY(), min.getBlockY()); y <= (int) Math.max(min.getBlockY(), max.getBlockY()); y++) {
				for(int x = (int) Math.max(max.getBlockX(), min.getBlockX()); x >= (int) Math.min(min.getBlockX(), max.getBlockX()); x--) {
					for(int z = (int) Math.max(max.getBlockZ(), min.getBlockZ()); z >= (int) Math.min(min.getBlockZ(), max.getBlockZ()); z--) {
						Location blockLocation = new Location(loc.getWorld(),x,y,z);
						Block block = blockLocation.getBlock();
						if (min.getZ()!=max.getZ()) {
							setFrameBlock(block, true, load);
						} else {
							setFrameBlock(block, false, load);
						}
					}
				}
			}
			
			return true;
		}

		return false;
	}
	
	public boolean destroy(Location loc, DestroyCause cause, Entity destroyer) {
		List<Object> portal = isPortal(loc, false, false);
		if (portal==null) return false;

		CustomPortalDestroyEvent event = new CustomPortalDestroyEvent(loc, this, cause, destroyer);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			
			Location[] portalLocations = (Location[]) portal.get(0);
			
			Location min = portalLocations[0].add(portalLocations[2]);
			Location max = portalLocations[1].add(portalLocations[3]);
	
			for(int y = (int) Math.min(max.getBlockY(), min.getBlockY()); y <= (int) Math.max(min.getBlockY(), max.getBlockY()); y++) {
				for(int x = (int) Math.max(max.getBlockX(), min.getBlockX()); x >= (int) Math.min(min.getBlockX(), max.getBlockX()); x--) {
					for(int z = (int) Math.max(max.getBlockZ(), min.getBlockZ()); z >= (int) Math.min(min.getBlockZ(), max.getBlockZ()); z--) {
						Location blockLocation = new Location(loc.getWorld(),x,y,z);
						Block block = blockLocation.getBlock();
						block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 10, getFrameBlockData(block, false));
						block.getWorld().playSound(blockLocation, Sound.BLOCK_GLASS_BREAK, 1.0F, 8.0F);
						block.setType(Material.AIR);
						for (Entity en : blockLocation.getWorld().getNearbyEntities(blockLocation, 1,1,1)) {
							if (en instanceof FallingBlock) {
								en.remove();
							}
						}
						
						portalClass.removeLocation(this, blockLocation);
					}
				}
			}
			
			return true;
		} else {
			return false;
		}
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
	
	public BlockData getFrameBlockData(Block block, boolean zAxis) {
		block.setType(getFrame());
		BlockData blockData = block.getBlockData();
		if (zAxis) {
			if (blockData instanceof Orientable) {
				Orientable orientable = (Orientable) blockData;
				orientable.setAxis(Axis.Z);
				block.setBlockData(orientable);
			} else if (blockData instanceof Directional) {
				Directional directional = (Directional) blockData;
				directional.setFacing(BlockFace.NORTH);
				block.setBlockData(directional);
			} else if (blockData instanceof MultipleFacing) {
				MultipleFacing face = (MultipleFacing) blockData;
				face.setFace(BlockFace.NORTH, true);
				face.setFace(BlockFace.SOUTH, true);
				block.setBlockData(face);
			}
		} else {
			if (blockData instanceof MultipleFacing) {
				MultipleFacing face = (MultipleFacing) blockData;
				face.setFace(BlockFace.EAST, true);
				face.setFace(BlockFace.WEST, true);
				block.setBlockData(face);
			}
		}
		
		return block.getBlockData();
	}
	
	public boolean isZAxis(Location loc) {
		List<Object> portal = isPortal(loc, false, false);
		if (portal==null) return false;
		Location[] portalLocations = (Location[]) portal.get(0);
		
		Location min = portalLocations[0].add(portalLocations[2]);
		Location max = portalLocations[1].add(portalLocations[3]);
		
		if (min.getZ()!=max.getZ()) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setFrameBlock(Block block, boolean zAxis, boolean load) {
		block.setBlockData(getFrameBlockData(block, zAxis));
		if (block.getType()!=Material.LAVA && block.getType()!=Material.WATER) {
			FallingBlock fallingBlock = block.getLocation().getWorld().spawnFallingBlock(block.getLocation().add(0.5f,0,0.5f), block.getBlockData());
			fallingBlock.setGravity(false);
			fallingBlock.setDropItem(false);
			fallingBlock.setHurtEntities(false);
			fallingBlock.setTicksLived(Integer.MAX_VALUE);
			block.setType(Material.AIR);
		}
		if (!load) portalClass.addLocation(this,block.getLocation());
	}
	
	//Find the location that the player must go when entering a portal
	public Location calculateTeleportLocation(Player p, EntityUseCustomPortalEvent event) {
		
		Location loc = event.getLocation();
		Location teleportLocation;
		
		//If player is returning plugin will find the world he got into from. If not he will be teleported to the default world
		if (!loc.getWorld().getName().contentEquals(getWorld().getName())) {
			teleportLocation = new Location(getWorld(), Math.floor(loc.getX())/getRatio(), loc.getY(), Math.floor(loc.getZ())/getRatio());
		} else {
			teleportLocation = new Location(portalClass.getReturnWorld(p, this), Math.floor(loc.getX())*getRatio(), loc.getY(), Math.floor(loc.getZ())*getRatio());
		}
		
		if (event.isForcedTeleport()) return teleportLocation;
		if (teleportLocation.getX()==Double.POSITIVE_INFINITY || teleportLocation.getX()==Double.NEGATIVE_INFINITY || teleportLocation.getZ()==Double.POSITIVE_INFINITY || teleportLocation.getZ()==Double.NEGATIVE_INFINITY) {
			p.sendMessage("§4ERROR: §cPortal \""+getDisplayName()+"\"§c uses bad math for world ratio maybe. "+ratio.split(":")[0]+">"+ratio.split(":")[1]+"?. Report this to an admin. X = "+teleportLocation.getX()+", Z = "+teleportLocation.getZ());
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
			return nearestLocation;
		}
		
		boolean zAxis = isZAxis(loc);
		event.setZaxis(zAxis);
		
		if (!getSpawnOnAir()) {
			boolean foundLocation = false;

			Location tempLoc = spiralSearch(teleportLocation, zAxis);
			if (tempLoc!=null) {
				teleportLocation =  tempLoc;
				foundLocation = true;
			} else {
				while (!foundLocation && teleportLocation.getY()<teleportLocation.getWorld().getHighestBlockYAt(teleportLocation)) {
					while (teleportLocation.getBlock().getRelative(BlockFace.UP).getType()!=Material.AIR && teleportLocation.getY()<teleportLocation.getWorld().getHighestBlockYAt(teleportLocation)) teleportLocation.add(0,1,0);
					tempLoc = spiralSearch(teleportLocation, zAxis);
					if (tempLoc!=null) {
						teleportLocation =  tempLoc;
						foundLocation = true;
					} else {
						teleportLocation.add(0,1,0);
					}
				}
			}
			
			if (!foundLocation) {
				while (teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()==Material.AIR && teleportLocation.getY()>=10) {
					teleportLocation.add(0,-1,0);
				}
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
	
	public Location spiralSearch(Location teleportLocation, boolean zAxis) {


	    int size=16;
	    for (int sz=1;sz<=size;sz++) {
			for (int y=sz;y>=-sz;y--) {
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
			            	if (teleportLocation.getY()+y<=0) continue;
			            	Location blockLocation = new Location(teleportLocation.getWorld(),teleportLocation.getX()+x,teleportLocation.getY()+y,teleportLocation.getZ()+z);
							if (canBuildPortal(blockLocation, zAxis)) {
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

	    return null;
	}
	
	public boolean canBuildPortal(Location loc, boolean zAxis) {
		
		if (getBuildExitPortal()) {
			for (int i=-1;i<3;i++) {
				if (!loc.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i).getType().isSolid() ||
					(loc.getBlock().getRelative(BlockFace.UP,3).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i).getType()!=Material.AIR && !isPortalBlock(loc.getBlock().getRelative(BlockFace.UP,3).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i))))
						return false; 
			}
			
			
			for (int i=0;i<4;i++) {
				if ((loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH).getType()!=Material.AIR && !isPortalBlock(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH))) ||
					(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,2).getType()!=Material.AIR && !isPortalBlock(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,2))) ||
					(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH).getType()!=Material.AIR && !isPortalBlock(loc.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH))) ||
					(loc.getBlock().getRelative(BlockFace.UP,i).getType()!=Material.AIR && !isPortalBlock(loc.getBlock().getRelative(BlockFace.UP,i))))
						return false;
			}
		} else {
			if (loc.getBlock().getType()!=Material.AIR ||
				loc.getBlock().getRelative(BlockFace.UP).getType()!=Material.AIR)
					return false;
		}
		return true;
	}
	
	public void spawnParticles(Location loc) {
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX()+0.5f,loc.getY()+0.5f,loc.getZ()+0.5f, 3, 0.5,0.5,0.5,new Particle.DustOptions(getParticlesColor(),2));

	}
	
	public void usePortal(Player p,boolean forceTP) {
		
		//Disable teleportation if player is in the disabled worlds
		if (getDisabledWorlds().contains(p.getLocation().getWorld())) return;
		//Call event for custom plugins that want to extend the possibilities of the plugin
		EntityUseCustomPortalEvent event = new EntityUseCustomPortalEvent(p, p.getLocation(), this, getBuildExitPortal(),forceTP);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			//Calculate teleport location and teleport the player there
			Location startLocation = event.getLocation();
			Location teleportLocation = calculateTeleportLocation(p, event);
			EntityTeleportCustomPortalEvent tpEvent = new EntityTeleportCustomPortalEvent(event,teleportLocation,startLocation);
			Bukkit.getServer().getPluginManager().callEvent(tpEvent);
			if (!tpEvent.isCancelled()) {
				if (teleportLocation!=null && !event.isForcedTeleport()) {
					if (event.getBuildLocation()!=null) buildPortal(event);
					portalClass.addToUsedPortals(p,this);
					p.teleport(teleportLocation);
				}
			}
		}
	}
	
	public void buildPortal(EntityUseCustomPortalEvent event) {

		boolean zAxis = event.getZaxis();
		Location teleportLocation = event.getBuildLocation();
		
		if (event.getBuildExitPortal()) {
			
			//build a portal
			boolean added = false;
			if (isPortalBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN)) && isPortalBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH))) {
				added = true;
				teleportLocation.add(0,-1,0);
			}
			if (!teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
				for (int side = -1;side<2;side++) {
					setBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.SOUTH : BlockFace.WEST,side));
					setBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,1).getRelative(!zAxis ? BlockFace.SOUTH : BlockFace.WEST,side));
				}
			}
			
			for (int i=-1;i<3;i++) {
				setBlock(teleportLocation.getBlock().getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i));
				setBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,4).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,i));
			}
			
			for (int i=1;i<4;i++) {
				setBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.EAST : BlockFace.NORTH));
				setBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH,2));
	
				setFrameBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,i).getRelative(!zAxis ? BlockFace.WEST : BlockFace.SOUTH),zAxis,false);
				setFrameBlock(teleportLocation.getBlock().getRelative(BlockFace.UP,i),zAxis,false);
			}
			if (added) teleportLocation.add(0,1,0);
		} else {
			
			if (teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()==Material.WATER || teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()==Material.LAVA || teleportLocation.getBlock().getRelative(BlockFace.DOWN).getType()==Material.AIR) 
				setBlock(teleportLocation.getBlock().getRelative(BlockFace.DOWN));
		}
		
		
		for (int i = 5;i>=0;i--) {
			if (teleportLocation.getBlock().getRelative(BlockFace.DOWN,i).getType()==frame) {
				teleportLocation.add(0,-i,0);
			}
		}
	}
}
