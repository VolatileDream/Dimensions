package me.xxastaspastaxx.dimensions.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.xxastaspastaxx.dimensions.portal.CompletePortal;

public class EntityStartedViewingCustomPortalEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private CompletePortal complete;
    private Entity entity;
    
    public EntityStartedViewingCustomPortalEvent(CompletePortal complete, Entity entity) {
    	this.complete=complete;
    	this.entity = entity;
    }
    
    public CompletePortal getPortal() {
    	return complete;
    }
    
    public Entity getEntity() {
    	return entity;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
