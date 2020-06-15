package de.jeff_media.AngelChest;

import java.util.ArrayList;

import org.bukkit.block.Block;
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
		
		if(args.length==5 && args[0].equals("tp")) {
			AngelChestCommandUtils.teleportPlayerToChest(plugin,(Player)sender, args);
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
		
		sendListOfAngelChests(p);

		return true;
	}

	void sendListOfAngelChests(Player p) {
		// Get all AngelChests by this player
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p, plugin);
		
		if(angelChestsFromThisPlayer.size()==0) {
			p.sendMessage(plugin.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
			return;
		}
		
		p.sendMessage(plugin.messages.MSG_ANGELCHEST_LOCATION);
		
		int chestIndex = 1;
		Block b;

		for(AngelChest angelChest : angelChestsFromThisPlayer) {
			int remaining = angelChest.secondsLeft;
			int sec = remaining % 60;
			int min = (remaining / 60) % 60;
			int hour = (remaining / 60) / 60;

			b = angelChest.block;
			String tpCommand=null;
			String unlockCommand=null;
			if(p.hasPermission("angelchest.tp")) {
				tpCommand="/acinfo tp "+b.getX()+" "+b.getY()+" "+b.getZ()+" "+b.getWorld().getName();
			}
			if(angelChest.isProtected) {
				unlockCommand="/acunlock "+b.getX()+" "+b.getY()+" "+b.getZ()+" "+b.getWorld().getName();
			}
			
			String text = String.format("[%d] %02d:%02d:%02d §aX:§f %d §aY:§f %d §aZ:§f %d | %s ",
				chestIndex, hour, min, sec, b.getX(), b.getY(), b.getZ(), b.getWorld().getName()
			);
			p.spigot().sendMessage(LinkUtils.getLinks(p, text, tpCommand,unlockCommand,plugin));
			chestIndex++;
		}
	}

}