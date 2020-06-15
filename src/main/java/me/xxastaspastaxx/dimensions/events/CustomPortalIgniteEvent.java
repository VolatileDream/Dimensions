package me.xxastaspastaxx.dimensions.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;

import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class CustomPortalIgniteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    Location portalLocation;
    CustomPortal portal;
    List<Block> blocks;
    IgniteCause cause;
    LivingEntity entity;
    boolean load;
    
    ItemStack lighter;
    
    public CustomPortalIgniteEvent(Location portalLocation, CustomPortal portal, List<Block> blocks, IgniteCause cause, LivingEntity entity, boolean load, ItemStack lighter) {
    	this.portalLocation=portalLocation;
    	this.portal=portal;
    	this.blocks = blocks;
    	this.cause = cause;
    	this.entity = entity;
    	this.load = load;
    	this.lighter = lighter;
    }
    
    public Location getLocation() {
    	return portalLocation;
    }
    
    public CustomPortal getCustomPortal() {
    	return portal;
    }

    public List<Block> getBlocks() {
    	return blocks;
    }
    
    public IgniteCause getCause() {
    	return cause;
    }
    
    public LivingEntity getEntity() {
    	return entity;
    }
    
    public boolean isLoading() {
    	return load;
    }
    
    public ItemStack getLighter() {
    	return lighter;
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
