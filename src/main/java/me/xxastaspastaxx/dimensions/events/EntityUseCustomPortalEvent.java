package me.xxastaspastaxx.dimensions.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;

public class EntityUseCustomPortalEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    Entity entity;
    CompletePortal complete;
    
    boolean buildExitPortal;
    
    boolean forceTP;
    boolean bungee;

    Location tpLoc;
    Location buildLocation;
    boolean zAxis;
    
    boolean forceCalculate = false;
    
    public EntityUseCustomPortalEvent(Entity entity, Location tpLoc, CompletePortal complete, boolean buildExitPortal,boolean forceTP, boolean bungee) {
    	this.entity=entity;
    	this.tpLoc = tpLoc;
    	this.complete=complete;
    	this.buildExitPortal = buildExitPortal;
    	this.forceTP = forceTP;
    	this.bungee = bungee;
    }

    public boolean isForceCalculate() {
		return forceCalculate;
	}

	public void setForceCalculate(boolean forceCalculate) {
		this.forceCalculate = forceCalculate;
	}

	public Entity getEntity() {
    	return entity;
    }
    
    public CompletePortal getPortal() {
    	return complete;
    }
    
    public Location getLocation() {
    	return tpLoc;
    }
    
    public void setLocation(Location tpLoc) {
    	this.tpLoc = tpLoc;
    }
    
    public boolean getBuildExitPortal() {
    	return buildExitPortal;
    }
    
    public Location getBuildLocation() {
    	return buildLocation;
    }
    
    public void setBuildLocation(Location buildLocation) {
    	this.buildLocation = buildLocation;
    }
    
    public void setBuildExitPortal(boolean buildExitPortal) {
    	this.buildExitPortal = buildExitPortal;
    }
    
    public boolean isForcedTeleport() {
    	return forceTP;
    }
    
    public boolean isBungee() {
    	return bungee;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

	public boolean getZaxis() {
		return zAxis;
	}
	
	public void setZaxis(boolean zAxis) {
		this.zAxis = zAxis;
	}
}
