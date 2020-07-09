package de.jeff_media.AngelChest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandList implements CommandExecutor {
	
	Main plugin;
	
	public CommandList(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if(!command.getName().equalsIgnoreCase("aclist")) return false;
		
		if(!sender.hasPermission("angelchest.use")) {
			sender.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
			return true;
		}

		if(!(sender instanceof Player)) {
			sender.sendMessage(plugin.messages.MSG_PLAYERSONLY);
			return true;
		}
		
		Player p = (Player) sender;
		
		p.sendMessage(plugin.messages.MSG_ANGELCHEST_LOCATION);
		AngelChestCommandUtils.sendListOfAngelChests(plugin, p);

		return true;
	}
}