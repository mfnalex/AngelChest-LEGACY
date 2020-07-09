package de.jeff_media.AngelChest;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUnlock implements CommandExecutor {
	
	Main plugin;
	
	public CommandUnlock(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!command.getName().equalsIgnoreCase("unlock")) return false;
		
		if(!sender.hasPermission("angelchest.protect")) {
			sender.sendMessage(plugin.getCommand("unlock").getPermissionMessage());
			return true;
		}

		if(! ( sender instanceof Player)) {
			sender.sendMessage(plugin.messages.MSG_PLAYERSONLY);
			return true;
		}
		
		Player p = (Player) sender;
		
		if(args.length > 0) {
			if(args[0].equals("all")) {
				AngelChestCommandUtils.unlockAllChests(plugin, p);
				return true;
			}
		}
		
		AngelChest ac = AngelChestCommandUtils.argIdx2AngelChest(plugin, p, args);
		if(ac == null) {
			return true;
		}

		AngelChestCommandUtils.unlockSingleChest(plugin, p, ac);
		return true;
	}
}