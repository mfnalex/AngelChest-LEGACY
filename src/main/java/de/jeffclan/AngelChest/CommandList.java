package de.jeffclan.AngelChest;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandList implements CommandExecutor {
	
	AngelChestPlugin plugin;
	
	public CommandList(AngelChestPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!command.getName().equalsIgnoreCase("aclist")) return false;
		
		if(args.length==5 && args[1].equals("tp")) {
			AngelChestTeleporter.teleportPlayerToChest(plugin,(Player)sender, args);
			return true;
		}
		
		if(!sender.hasPermission("angelchest.use")) {
			sender.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
			return true;
		}

		if(!(sender instanceof Player)) {
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
		
		int chestIndex = 1;
		Block b;

		for(AngelChest angelChest : angelChestsFromThisPlayer) {
			long remaining = angelChest.secondsRemaining();
			long sec = remaining % 60;
			long min = (remaining / 60) % 60;
			long hour = (remaining / 60) / 60;
			
			String tpCommand="/tp 0 100 0";

			b = angelChest.block;
			String text = String.format("[%d] %02d:%02d:%02d §aX:§f %d §aY:§f %d §aZ:§f %d | %s ",
				chestIndex, hour, min, sec, b.getX(), b.getY(), b.getZ(), b.getWorld().getName()
			);
			TpLinkUtil.sendLink(p, text, tpCommand);
			chestIndex++;
		}

		return true;
	}

}