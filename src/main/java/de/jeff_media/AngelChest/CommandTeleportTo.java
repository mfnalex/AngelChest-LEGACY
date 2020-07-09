package de.jeff_media.AngelChest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTeleportTo implements CommandExecutor {

	Main plugin;

	public CommandTeleportTo(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!command.getName().equalsIgnoreCase("actp")) return false;		

		if(!(sender instanceof Player)) {
			sender.sendMessage(plugin.messages.MSG_PLAYERSONLY);
			return true;
		}

		Player p = (Player) sender;

		if(!sender.hasPermission("angelchest.tp")) {
			sender.sendMessage(plugin.getCommand("actp").getPermissionMessage());
			return true;
		}

        AngelChest ac = AngelChestCommandUtils.argIdx2AngelChest(plugin, p, args);
        if(ac == null) {
            return true;
        }

        AngelChestCommandUtils.teleportPlayerToChest(plugin, p, ac);

		return true;
	}

}
