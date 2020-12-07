package me.xxastaspastaxx.dimensions.Utils;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.xxastaspastaxx.dimensions.Main;
import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;

public class DimensionsExpansion extends PlaceholderExpansion {

    private Main plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public DimensionsExpansion(Main plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "dimensions";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player p, String identifier){
    	if (!(p instanceof Player)) return null;
    	
    	
    	String subIdentifier = "message_";
    	if (identifier.startsWith(subIdentifier) && Messages.messages.containsKey(identifier.substring(subIdentifier.length()))) {
    		return Messages.get(identifier.substring(subIdentifier.length()));
    	}
    	
    	subIdentifier = "settings_";
    	if (identifier.startsWith(subIdentifier)) {
    		return (String) DimensionsSettings.getConf().get(identifier.substring(subIdentifier.length()), "null");
    	}
       
    	subIdentifier = "portal_";
    	if (identifier.startsWith(subIdentifier)) {
    		subIdentifier = identifier.substring(subIdentifier.length());    				
    		for (CustomPortal portal : Dimensions.getPortals()) {
    			if (!subIdentifier.startsWith(portal.getName())) continue;
    			return (String) Dimensions.getPortalConfiguration(portal).get(subIdentifier.substring(portal.getName().length()), "null");
    		}
    	}
    	
    	subIdentifier = "player_";
    	if (p!=null && identifier.startsWith(subIdentifier)) {
    		subIdentifier = identifier.substring(subIdentifier.length());    				
    		String[] args = subIdentifier.split("_");
    		
    		if (args[0].equalsIgnoreCase("isInPortal")) {
    			if (args.length>1) {
    				CustomPortal portal = Dimensions.getPortalFromName(args[1]);
    				if (portal!=null) {
    					for (CompletePortal complete : Dimensions.getPortalsVisibleFromPlayer(p)) {
    						if (!complete.getPortal().equals(portal)) continue;
    						if (complete.isOnHold(p)) return "true";
    					}
    				}
					return "false";
    			} else {
    				return Dimensions.isOnHold(p)+"";
    			}
    		} else if (args[0].equalsIgnoreCase("getPortal")) {
    			for (CompletePortal complete : Dimensions.getPortalsVisibleFromPlayer(p)) {
					if (complete.isOnHold(p)) return complete.getPortal().getDisplayName();
				}
    		} else if (args[0].equalsIgnoreCase("isPortalZAxis")) {
    			for (CompletePortal complete : Dimensions.getPortalsVisibleFromPlayer(p)) {
					if (complete.isOnHold(p)) return complete.isZAxis()+"";
				}
    			return "false";
    		} else if (args[0].equalsIgnoreCase("getReturnWorld")) {
    			if (args.length>1) {
    				CustomPortal portal = Dimensions.getPortalFromName(args[1]);
    				if (portal!=null) {
    					return portal.getReturnWorld(p, p.getWorld(), false, true).getName();
    				}
					return "null";
    			}
    		}
    	}
 
        return null;
    }
}