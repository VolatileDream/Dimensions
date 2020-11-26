package me.xxastaspastaxx.dimensions.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;

public class CustomPortalDestroyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    CompletePortal complete;
    DestroyCause cause;
    Entity entity;
    
    public CustomPortalDestroyEvent(CompletePortal complete, DestroyCause cause, Entity entity) {
    	this.complete=complete;
    	this.cause = cause;
    	this.entity = entity;
    }
    
    public CompletePortal getPortal() {
    	return complete;
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
