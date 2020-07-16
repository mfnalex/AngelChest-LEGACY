package de.jeff_media.AngelChest;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

		if(!(sender instanceof Player)) {
			sender.sendMessage(plugin.messages.MSG_PLAYERSONLY);
			return true;
		}

		Player p = (Player) sender;

		if(!sender.hasPermission("angelchest.fetch")) {
			sender.sendMessage(plugin.getCommand("acfetch").getPermissionMessage());
			return true;
		}

        AngelChest ac = AngelChestCommandUtils.argIdx2AngelChest(plugin, p, args);
        if(ac == null) {
            return true;
        }

        // Only charge the player if they select a valid chest
		double price = plugin.getConfig().getDouble("price-fetch");
		if(price>0 && !AngelChestCommandUtils.hasEnoughMoney(p,price,plugin)) {
			return true;
		}

		Location newLoc = p.getLocation();
		String dir = Utils.getCardinalDirection(p);
		BlockFace facing;
	
		// Set the relative direction of the block and offset the new chest location
		switch(dir){
			case "N":
				newLoc.add(0,0,-2);
				facing = BlockFace.SOUTH;
				break;
			case "NE":
				newLoc.add(2,0,-2);
				facing = BlockFace.SOUTH;
				break;
			case "E":
				newLoc.add(2,0,0);
				facing = BlockFace.WEST;
				break;
			case "SE":
				newLoc.add(2,0,2);
				facing = BlockFace.WEST;
				break;
			case "S":
				newLoc.add(0,0,2);
				facing = BlockFace.NORTH;
				break;
			case "SW":
				newLoc.add(-2,0,2);
				facing = BlockFace.NORTH;
				break;
			case "W":
				newLoc.add(-2,0,0);
				facing = BlockFace.EAST;
				break;
			case "NW":
				newLoc.add(-2,0,-2);
				facing = BlockFace.EAST;
				break;
			default:
				plugin.getLogger().info("Unable to get block facing direction");
				facing = BlockFace.NORTH;
		}

		Block newBlock = Utils.findSafeBlock(newLoc.getBlock(), plugin);
		Block oldBlock = ac.block;

		// Move the block in game
		ac.destroyChest(oldBlock);
		ac.createChest(newBlock, p.getUniqueId());

		// Make the chest face the player
		try {
			AngelChestBlockDataUtils.setBlockDirection(newBlock, facing);
		} catch (Throwable throwable) {
			// NoClassDefFoundError in <1.13
		}

		// Swap the block in code
		plugin.angelChests.put(newBlock, plugin.angelChests.remove(oldBlock));
		plugin.angelChests.get(newBlock).block = newBlock;

		p.sendMessage("AngelChest Retrieved!");

		return true;
	}

}
