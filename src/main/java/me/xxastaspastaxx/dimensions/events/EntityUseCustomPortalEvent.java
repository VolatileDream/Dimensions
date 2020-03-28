package me.xxastaspastaxx.dimensions.events;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class EntityUseCustomPortalEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    LivingEntity entity;
    Location portalLocation;
    CustomPortal portal;
    
    boolean buildExitPortal;
    
    boolean forceTP;
    
    Location buildLocation;
    boolean zAxis;
    
    public EntityUseCustomPortalEvent(LivingEntity entity, Location portalLocation, CustomPortal portal, boolean buildExitPortal,boolean forceTP) {
    	this.entity=entity;
    	this.portalLocation=portalLocation;
    	this.portal=portal;
    	this.buildExitPortal = buildExitPortal;
    	this.forceTP = forceTP;
    }

    public LivingEntity getEntity() {
    	return entity;
    }
    
    public Location getLocation() {
    	return portalLocation.clone();
    }
    
    public void setLocation(Location loc) {
    	portalLocation = loc.clone();
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
    
    public CustomPortal getCustomPortal() {
    	return portal;
    }
    
    public boolean isForcedTeleport() {
    	return forceTP;
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
