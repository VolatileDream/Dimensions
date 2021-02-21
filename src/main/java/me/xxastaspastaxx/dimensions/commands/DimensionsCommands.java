package me.xxastaspastaxx.dimensions.commands;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.portal.CompletePortal;
import me.xxastaspastaxx.dimensions.portal.CustomPortal;
import me.xxastaspastaxx.dimensions.portal.PortalClass;
import me.xxastaspastaxx.dimensions.utils.Messages;

public class DimensionsCommands implements CommandExecutor {

	HashMap<String, Entry<Entry<String, String>, Entry<String, Boolean>>> commands = new HashMap<String, Entry<Entry<String, String>, Entry<String, Boolean>>>();
	//command, description, permission, admin command
	
	float cmdPerPage = 5;
	
	String pluginCommand = "dim";
	String pluginName = "Dimensions";
	String prefix = Messages.get("prefix")+" ";
	
	Plugin pl;
	PortalClass pc;
	
	Plugin dimensionsAddons;
	
    public DimensionsCommands(Plugin pl, PortalClass pc) {
    	this.pl = pl;
    	this.pc = pc;
    	
    	//Setup command list
    	addCommand("help", "[page/command]","Display commands list","none",false);
    	addCommand("adminhelp", "[page/command]","Display admin commands list",false);
    	addCommand("perms", "[page/command]","Display permissions for commands",true);
    	addCommand("adminperms", "[page/command]","Display permissions for admin commands",true);
    	

    	//addCommand("portals", "[page/command]","Display portals",false);
    	addCommand("clear", "<all/world/portal>","Delete all saved portals.",true);
    	addCommand("reload", "","Reload settings and load new portals.",true);
    	addCommand("history", "clear","Clear portal player history",true);
    	addCommand("data", "clear","Clear player data",true);
    	addCommand("portal", "[page/portal name]","Show info about portal",false);
    	
    	
    	dimensionsAddons = Bukkit.getPluginManager().getPlugin("DimensionsAddons");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void addCommand(String cmd, String args, String desc, boolean adminCommand) {
    	Entry<String, String> permDesc = new SimpleEntry(desc,pluginName.toLowerCase()+"."+cmd);
    	Entry<String, Boolean> argsAdmin = new SimpleEntry(args, adminCommand);
		Entry<Entry<String, String>, Entry<String, Boolean>> admin = new SimpleEntry(permDesc, argsAdmin);
    	commands.put(cmd, admin);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCommand(String cmd, String args, String desc, String perm, boolean adminCommand) {
    	Entry<String, String> permDesc = new SimpleEntry(desc,perm);
    	Entry<String, Boolean> argsAdmin = new SimpleEntry(args, adminCommand);
		Entry<Entry<String, String>, Entry<String, Boolean>> admin = new SimpleEntry(permDesc, argsAdmin);
    	commands.put(cmd, admin);
    }

    public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
    	//if (!(sender instanceof Player)) return false;
		
		if (args.length==0) {
			if (hasPermission(p, "help")) {
				p.sendMessage(getHelp(1, false));
				return true;
			}
		} else {
			if (p instanceof Player && commands.containsKey(args[0]) && !getPermission(args[0].toLowerCase()).equalsIgnoreCase("none") && !p.hasPermission(getPermission(args[0].toLowerCase()))) {
				p.sendMessage(Messages.get("noPermission"));
				return true;
			}
			
			if (args[0].equalsIgnoreCase("help")) {
				if (args.length==1) {
					p.sendMessage(getHelp(1, false));
					return true;
				} else if (args.length==2) {
					if (isInt(args[1])) {
						p.sendMessage(getHelp(Integer.parseInt(args[1]), false));
						return true;
					} else {
						p.sendMessage(getHelp(args[1], false));
						return true;
					}
				}
			}
			if (args[0].equalsIgnoreCase("adminhelp")) {
				if (args.length==1) {
					p.sendMessage(getHelp(1, true));
					return true;
				} else if (args.length==2) {
					if (isInt(args[1])) {
						p.sendMessage(getHelp(Integer.parseInt(args[1]), true));
						return true;
					} else {
						p.sendMessage(getHelp(args[1], true));
						return true;
					}
				}
			}
			
			if (args[0].equalsIgnoreCase("perms")) {
				if (args.length==1) {
					p.sendMessage(getPermissions(1, false));
					return true;
				} else if (args.length==2) {
					if (isInt(args[1])) {
						p.sendMessage(getPermissions(Integer.parseInt(args[1]), false));
						return true;
					} else {
						p.sendMessage(getPermissions(args[1], false));
						return true;
					}
				}
			}
			if (args[0].equalsIgnoreCase("adminperms")) {
				if (args.length==1) {
					p.sendMessage(getPermissions(1, true));
					return true;
				} else if (args.length==2) {
					if (isInt(args[1])) {
						p.sendMessage(getPermissions(Integer.parseInt(args[1]), true));
						return true;
					} else {
						p.sendMessage(getPermissions(args[1], true));
						return true;
					}
				}
			}
			//Commands
			if (args[0].equalsIgnoreCase("clear") && args.length==2) {
				Iterator<CompletePortal> iterator = pc.getCompletePortals().iterator();
				while (iterator.hasNext()) {
					CompletePortal complete = iterator.next();
					if (args[1].equalsIgnoreCase("all") || (args[1].equalsIgnoreCase(complete.getFrames().get(0).getLocation().getWorld().getName())) || (args[1].equalsIgnoreCase(complete.getPortal().getName()))) {
						iterator.remove();
						complete.destroy(true);
					}
				}
				p.sendMessage(prefix+"§aRemoved §c"+args[1]+"§a portals");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("reload") && args.length==1) {
				try {
					Dimensions.getInstance().files.reload();
					reloadDimensionsAddons();
					p.sendMessage(prefix+"§aSuccesfully reloaded files.");
				} catch (Exception e) {
					p.sendMessage(prefix+"§cCould not reload files. Check console for errors.");
					e.printStackTrace();
				}
				return true;
			}
			
			if (args[0].equalsIgnoreCase("history") && args.length==2 && args[1].equalsIgnoreCase("clear")) {
				pc.clearHistory();
				p.sendMessage(prefix+"§aCleared portal history.");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("data") && args.length==2 && args[1].equalsIgnoreCase("clear")) {
				pc.clearData();
				p.sendMessage(prefix+"§aCleared player data.");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("portal")) {
				if (args.length==2) {
					if (isInt(args[1])) {
						int page = Integer.parseInt(args[1]);
						int pageCount = (int) Math.ceil(pc.getPortals().size()/cmdPerPage);
				    	if (page>pageCount) {
				    		p.sendMessage("§7Page §c"+page+"§7 was found.");
				    		return true;
				    	}
				    	
				    	String result = "§7§m---§7[§c"+page+"/"+pageCount+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+pageCount+"§7]§m---§r\n";
				    	result += "\n";
				    	
				    	for (int i=(int) ((page-1)*cmdPerPage);i<cmdPerPage*page;i++) {
				    		if (i>=pc.getPortals().size()) break;
				    		CustomPortal portal = pc.getPortals().get(i);
				    		result+= (portal.isEnabled()?"§a[+]":"§c[x]")+portal.getName()+"§7 | "+portal.getWorld().getName()+" | "+portal.getMaterial().name().toLowerCase().replace("_", " ")+" | "+portal.getLighter().name().toLowerCase().replace("_", " ")+"\n";
				    	}
				    	
				    	result += "\n";
				    	result += "§7§m---§7[§c"+page+"/"+pageCount+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+pageCount+"§7]§m---\n";
				    	
				    	p.sendMessage(result);
					} else {
						CustomPortal portal = pc.getPortalByName(args[1]);
						if (portal==null) {
				    		p.sendMessage("§7Portal §c"+args[1]+"§7 was found.");
				    		return true;
				    	}
						
						String result = "§7Info about "+(portal.isEnabled()?"§a[+]":"§c[x]")+portal.getName()+"§7:";
						result+="\n§7Horizontal:§a "+portal.isHorizontal();
						result+="\n§7Material:§a "+portal.getMaterial().name().toLowerCase().replace("_", " ")+" | "+portal.getFaceString();
						result+="\n§7Frame:§a "+portal.getFrame().name().toLowerCase().replace("_", " ");
						result+="\n§7Lighter:§a "+portal.getLighter().name().toLowerCase().replace("_", " ");
						result+="\n§7Minimum width/height:§a "+portal.getMinPortalWidth()+"/"+portal.getMinPortalHeight();
						result+="\n§7World Name:§a "+portal.getWorld().getName()+(portal.isWorldNeeded()?"":" §7(Not required)");
						result+="\n§7World Ratio:§a "+portal.getRatio();
						result+="\n§7Build exit portal:§a "+portal.getBuildExitPortal();
						result+="\n§7SpawnOnAir:§a "+portal.canSpawnEntities();
						String disabledWorlds = "";
						for (World world : portal.getDisabledWorlds()) {
							disabledWorlds +=", "+world.getName();
						}
						result+="\n§7DisabledWorlds:§a "+(portal.getDisabledWorlds().size()==0?"§cnone":disabledWorlds.replaceFirst(", ", ""));
						p.sendMessage(result);
					}
				} else {
					int page = 1;
					int pageCount = (int) Math.ceil(pc.getPortals().size()/cmdPerPage);
			    	//if (page>getPageCount(admin)) return "§7Page §c"+page+"§7 was found.";
			    	
			    	String result = "§7§m---§7[§c"+page+"/"+pageCount+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+pageCount+"§7]§m---§r\n";
			    	result += "\n";
			    	
			    	for (int i=(int) ((page-1)*cmdPerPage);i<cmdPerPage*page;i++) {
			    		if (i>=pc.getPortals().size()) break;
			    		CustomPortal portal = pc.getPortals().get(i);
			    		result+= (portal.isEnabled()?"§a[+]":"§c[x]")+portal.getName()+"§7 | "+portal.getWorld().getName()+" | "+portal.getMaterial().name().toLowerCase().replace("_", " ")+" | "+portal.getLighter().name().toLowerCase().replace("_", " ")+"\n";
			    	}
			    	
			    	result += "\n";
			    	result += "§7§m---§7[§c"+page+"/"+pageCount+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+pageCount+"§7]§m---\n";
			    	
			    	p.sendMessage(result);
				}
				
				return true;
			}
		}
		
		String suggestion = getSuggestionInHelp(p, args[0]);
		if (suggestion!=null) {
			p.sendMessage(prefix+"§7Did you mean §c/"+ pluginCommand +" "+suggestion+"§7?");
		} else {
			p.sendMessage(prefix+"§7Unknown command");
		}
		return true;
	}
    
	private boolean hasPermission(CommandSender p, String command) {
		if (!(p instanceof Player)) return true;
		return getPermission(command).equalsIgnoreCase("none") || p.hasPermission(getPermission(command));
	}

	private void reloadDimensionsAddons() {
		if (dimensionsAddons==null) return;

		Bukkit.getPluginManager().disablePlugin(dimensionsAddons);
		Bukkit.getPluginManager().enablePlugin(dimensionsAddons);
	}

	public String getCommand(int index) {
    	return commands.keySet().toArray()[index].toString();
    }
    public String getDescription(String cmd) {
    	return commands.get(cmd).getKey().getKey();
    }
    
    public String getPermission(String cmd) {
    	return commands.get(cmd).getKey().getValue();
    }
    
    public String getArgs(String cmd) {
    	return commands.get(cmd).getValue().getKey();
    }
    
    public boolean isAdminCommand(String cmd) {
    	return commands.get(cmd).getValue().getValue();
    }
    
    public int getPageCount(boolean admin) {

		int items = 0;
		for (String command : commands.keySet()) {
    		boolean isAdmin = isAdminCommand(command);
    		if (!admin && isAdmin) {
    			items+=1;
    		} else if (admin && !isAdmin) {
    			items+=1;
    		}
    	}
    	return (int) Math.ceil((commands.size()-items)/cmdPerPage);
    }
    
    public int getTotalCommands(boolean admin) {
    	int items = 0;
    	
		for (String command : commands.keySet()) {
    		boolean isAdmin = isAdminCommand(command);
    		if (admin && isAdmin) {
    			items+=1;
    		} else if (!admin && !isAdmin) {
    			items+=1;
    		}
    	}
    	
    	return items;
    }
    
    public String getHelp(int page, boolean admin) {
    	if (page>getPageCount(admin)) return "§7Page §c"+page+"§7 was found.";
    	
    	String result = "§7§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---§r\n";
    	result += "\n";
    	
    	int offset = 0;
    	for (int i=(int) ((page-1)*cmdPerPage);i<cmdPerPage*page;i++) {
    		if (i>=getTotalCommands(admin)) break;
    		String cmd = getCommand(i+offset);
    		String args = getArgs(cmd);
    		String desc = getDescription(cmd);
    		//String perm = getPermission(cmd);
    		boolean isAdmin = isAdminCommand(cmd);
    		if ((admin && isAdmin) || (!admin && !isAdmin)) {
    			result+= "§7/"+ pluginCommand +" "+cmd+" "+args+" §c-§7 "+desc+"\n";
    		} else {
    			offset+=1;
    			i-=1;
    		}
    	}
    	
    	result += "\n";
    	result += "§7§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---\n";
    	return result;
    }
    
    public String getHelp(String cmd, boolean admin) {
    	
    	String result = "§7§m---§7[§c"+cmd+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+cmd+"§7]§m---\n";
    	result += "\n";
    	
    	for (String command : commands.keySet()) {
    		if (command.toLowerCase().startsWith(cmd.toLowerCase())) {
        		String args = getArgs(command);
        		String desc = getDescription(command);
        		//String perm = getPermission(command);
        		boolean isAdmin = isAdminCommand(command);
        		if ((admin && isAdmin) || (!admin && !isAdmin)) {
        			result+= "§7/"+ pluginCommand +" "+command+" "+args+" §c-§7 "+desc+"\n";
        		}
    		}
    	}
    	
    	result += "\n";
    	result += "§7§m---§7[§c"+cmd+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+cmd+"§7]§m---\n";
    	
    	
    	
    	return result;
    }
    
    public String getPermissions(int page, boolean admin) {
    	if (page>getPageCount(admin)) return "§7Page §c"+page+"§7 was found.";
    	
    	String result = "§7§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---§r\n";
    	result += "\n";
    	
    	int offset = 0;
    	for (int i=(int) ((page-1)*cmdPerPage);i<cmdPerPage*page;i++) {
    		if (i>=getTotalCommands(admin)) break;
    		String cmd = getCommand(i+offset);
    		String args = getArgs(cmd);
    		//String desc = getDescription(cmd);
    		String perm = getPermission(cmd);
    		boolean isAdmin = isAdminCommand(cmd);
    		if ((admin && isAdmin) || (!admin && !isAdmin)) {
    			result+= "§7/"+ pluginCommand +" "+cmd+" "+args+" §c-§7 "+perm+"\n";
    		} else {
    			offset+=1;
    			i-=1;
    		}
    	}
    	
    	result += "\n";
    	result += "§7§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+page+"/"+getPageCount(admin)+"§7]§m---\n";
    	return result;
    }
    
    public String getPermissions(String cmd, boolean admin) {
    	
    	String result = "§7§m---§7[§c"+cmd+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+cmd+"§7]§m---\n";
    	result += "\n";
    	
    	for (String command : commands.keySet()) {
    		if (command.toLowerCase().startsWith(cmd.toLowerCase())) {
        		String args = getArgs(command);
        		//String desc = getDescription(command);
        		String perm = getPermission(command);
        		boolean isAdmin = isAdminCommand(command);
        		if ((admin && isAdmin) || (!admin && !isAdmin)) {
        			result+= "§7/"+ pluginCommand +" "+command+" "+args+" §c-§7 "+perm+"\n";
        		}
    		}
    	}
    	
    	result += "\n";
    	result += "§7§m---§7[§c"+cmd+"§7]§m---§7[§c"+pluginName+"§7]§m---§7[§c"+cmd+"§7]§m---\n";
    	
    	
    	
    	return result;
    }
    
    
    public String getSuggestionInHelp(CommandSender p, String arg) {
    	
    	for (String cmd : commands.keySet()) {
    		if (cmd.toLowerCase().startsWith(arg.toLowerCase())) {
    			if (hasPermission(p, cmd)) return cmd+" "+getArgs(cmd);
    		}
    	}
    	
    	return null;
    }

    
	  public boolean isInt(String s)
	  {
	    try {
	      Integer.parseInt(s);
	    } catch (NumberFormatException nfe) {
	      return false;
	    }
	    return true;
	  }
	  
	  //Î™Î©Î‘Î�Î�Î‘ Î£Î• Î‘Î“Î‘Î Î†Î©!
}