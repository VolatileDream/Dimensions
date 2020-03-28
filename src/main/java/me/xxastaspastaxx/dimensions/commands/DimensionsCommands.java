package me.xxastaspastaxx.dimensions.commands;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DimensionsCommands implements CommandExecutor {

	HashMap<String, Entry<Entry<String, String>, Entry<String, Boolean>>> commands = new HashMap<String, Entry<Entry<String, String>, Entry<String, Boolean>>>();
	//command, description, permission, admin command
	
	float cmdPerPage = 5;
	
	String pluginCommand = "dim";
	String pluginName = "Dimensions";
	
    public DimensionsCommands(Plugin pl) {
    	//Setup command list
    	addCommand("help", "[page/command]","Display commands list","none",false);
    	addCommand("adminhelp", "[page/command]","Display admin commands list",true);
    	addCommand("perms", "[page/command]","Display permissions for commands",true);
    	addCommand("adminperms", "[page/command]","Display permissions for admin commands",true);
    	
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

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (!(sender instanceof Player)) return false;
    	
		Player p = (Player) sender;
			
		if (args.length==0) {
			if (getPermission("help").equalsIgnoreCase("none") || p.hasPermission(getPermission("help"))) {
				p.sendMessage(getHelp(1, false));
				return true;
			}
		} else {
			if (args[0].equalsIgnoreCase("help")) {
				if (getPermission(args[0].toLowerCase()).equalsIgnoreCase("none") || p.hasPermission(getPermission(args[0].toLowerCase()))) {
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
			}
			if (args[0].equalsIgnoreCase("adminhelp")) {
				if (getPermission(args[0].toLowerCase()).equalsIgnoreCase("none") || p.hasPermission(getPermission(args[0].toLowerCase()))) {
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
			}
			
			if (args[0].equalsIgnoreCase("perms")) {
				if (getPermission(args[0].toLowerCase()).equalsIgnoreCase("none") || p.hasPermission(getPermission(args[0].toLowerCase()))) {
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
			}
			if (args[0].equalsIgnoreCase("adminperms")) {
				if (getPermission(args[0].toLowerCase()).equalsIgnoreCase("none") || p.hasPermission(getPermission(args[0].toLowerCase()))) {
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
			}
			//Commands
			
		}
		
		String suggestion = getSuggestionInHelp(p, args[0]);
		if (suggestion!=null) {
			p.sendMessage("§7Did you mean §c/"+ pluginCommand +" "+suggestion+"§7?");
		} else {
			p.sendMessage("§7Unknown command");
		}
		return true;
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
    
    
    public String getSuggestionInHelp(Player p, String arg) {
    	
    	for (String cmd : commands.keySet()) {
    		if (cmd.toLowerCase().startsWith(arg.toLowerCase())) {
    			if (getPermission(cmd).equalsIgnoreCase("none") || p.hasPermission(getPermission(cmd))) return cmd+" "+getArgs(cmd);
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
}