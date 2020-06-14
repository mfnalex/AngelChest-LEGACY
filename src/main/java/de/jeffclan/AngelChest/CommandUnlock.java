package de.jeffclan.AngelChest;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUnlock implements CommandExecutor {
	
	AngelChestPlugin plugin;
	
	public CommandUnlock(AngelChestPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!command.getName().equalsIgnoreCase("unlock")) return false;
		
		if(args.length==4) {
			AngelChestCommandUtils.unlockSingleChest(plugin, (Player) sender, args);
			plugin.commandListExecutor.sendListOfAngelChests((Player) sender);
			return true;
		}
		
		if(!sender.hasPermission("angelchest.protect")) {
			sender.sendMessage(plugin.getCommand("unlock").getPermissionMessage());
			return true;
		}
		if(! ( sender instanceof Player)) {
			sender.sendMessage(plugin.messages.MSG_PLAYERSONLY);
			return true;
		}
		
		Player p = (Player) sender;
		
		// Get all AngelChests by this player
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p, plugin);
		
		if(angelChestsFromThisPlayer.size()==0) {
			p.sendMessage(plugin.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
			return true;
		}
		
		int chestsUnlocked = 0;
		
		for(AngelChest angelChest : angelChestsFromThisPlayer) {
			if(angelChest.isProtected) {
				angelChest.unlock();
				chestsUnlocked++;
			}
		}
		
		if(chestsUnlocked == 0) {
			p.sendMessage(plugin.messages.MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED);
			return true;
		}
		
		else if(chestsUnlocked == 1) {
			p.sendMessage(plugin.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
			return true;
		}
		
		else {
			p.sendMessage(String.format(plugin.messages.MSG_UNLOCKED_MORE_ANGELCHESTS,chestsUnlocked));
			return true;
		}
	}

}