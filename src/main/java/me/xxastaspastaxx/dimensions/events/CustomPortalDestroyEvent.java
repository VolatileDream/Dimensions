package me.xxastaspastaxx.dimensions.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class CustomPortalDestroyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    Location portalLocation;
    CustomPortal portal;
    DestroyCause cause;
    Entity entity;
    
    public CustomPortalDestroyEvent(Location portalLocation, CustomPortal portal, DestroyCause cause, Entity entity) {
    	this.portalLocation=portalLocation;
    	this.portal=portal;
    	this.cause = cause;
    	this.entity = entity;
    }
    
    public Location getLocation() {
    	return portalLocation;
    }
    
    public CustomPortal getCustomPortal() {
    	return portal;
    }
    
    public DestroyCause getCause() {
    	return cause;
    }
    
    public Entity getEntity() {
    	return entity;
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
}
