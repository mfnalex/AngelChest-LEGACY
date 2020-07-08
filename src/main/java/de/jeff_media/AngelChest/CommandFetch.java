package de.jeff_media.AngelChest;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFetch implements CommandExecutor {

	Main plugin;

	public CommandFetch(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!command.getName().equalsIgnoreCase("acfetch")) return false;		

		if(!sender.hasPermission("angelchest.fetch")) {
			sender.sendMessage(plugin.getCommand("acfetch").getPermissionMessage());
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

		if(angelChestsFromThisPlayer.size() > 1 && args.length == 0) {
			p.sendMessage("Please specify which AngelChest you would like to retrieve");
			return true;
		}

		int chestIdx = 0;

		if(args.length > 0) {
			chestIdx = Integer.parseInt(args[0]) - 1;
		}

		if(chestIdx >= angelChestsFromThisPlayer.size() || chestIdx < 0) {
			p.sendMessage("Invalid AngelChest!");
			return true;
		}

		Location pLoc = p.getLocation();
		String dir = Utils.getCardinalDirection(p);
		if (dir == "N") {
			pLoc.add(-2,0,0);
		} else if (dir == "NE") {
			pLoc.add(0,2,-2);
		} else if (dir == "E") {
			pLoc.add(0,2,0);
		} else if (dir == "SE") {
			pLoc.add(0,2,2);
		} else if (dir == "S") {
			pLoc.add(0,0,2);
		} else if (dir == "SW") {
			pLoc.add(0,-2,2);
		} else if (dir == "W") {
			pLoc.add(0,-2,0);
		} else if (dir == "NW") {
			pLoc.add(0,-2,-2);
		}

		AngelChest ac = angelChestsFromThisPlayer.get(chestIdx);
		Block newBlock = Utils.findSafeBlock(pLoc.getBlock(), plugin);
		Block oldBlock = ac.block;

		// Move the block in game
		oldBlock.setType(Material.AIR);
		newBlock.setType(Material.CHEST);
		ac.destroyHologram(plugin);
		ac.createHologram(newBlock, plugin);

		// Move the block in code
		plugin.angelChests.put(newBlock, plugin.angelChests.remove(oldBlock));
		plugin.angelChests.get(newBlock).block = newBlock;

		p.sendMessage("AngelChest Retrieved!");

		return true;
	}

}
