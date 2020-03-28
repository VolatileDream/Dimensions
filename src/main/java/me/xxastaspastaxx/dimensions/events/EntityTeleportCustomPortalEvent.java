package me.xxastaspastaxx.dimensions.events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityTeleportCustomPortalEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    EntityUseCustomPortalEvent useEvent;
    Location finalLocation;
    Location firstLocation;
    
    public EntityTeleportCustomPortalEvent(EntityUseCustomPortalEvent useEvent, Location finalLocation, Location firstLocation) {
    	this.useEvent = useEvent;
    	this.finalLocation = finalLocation;
    	this.firstLocation = firstLocation;
    }

    public EntityUseCustomPortalEvent getUseEvent() {
    	return useEvent;
    }
    
    public Location getFinalLocation() {
    	return finalLocation.clone();
    }
    
    public Location getFirstLocation() {
    	return firstLocation.clone();
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
