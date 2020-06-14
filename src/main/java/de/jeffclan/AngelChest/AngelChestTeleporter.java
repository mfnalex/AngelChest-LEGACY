package de.jeffclan.AngelChest;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AngelChestTeleporter {
	protected static void teleportPlayerToChest(AngelChestPlugin plugin, Player p, String[] args) {
		if(!p.hasPermission("angelchest.tp")) {
			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
			return;
		}
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int z = Integer.parseInt(args[3]);
		String world = args[4];
		
		Location loc = new Location(plugin.getServer().getWorld(world), x, y, z);
		Block block = loc.getBlock();
		
		if(!plugin.angelChests.containsKey(block)) {
			p.sendMessage(ChatColor.RED+"The AngelChest could not be found.");
			return;
		}
		AngelChest ac = plugin.getAngelChest(block);
		if(!ac.owner.equals(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED+"You do not own this AngelChest.");
			return;
		}
		
		p.teleport(loc, TeleportCause.PLUGIN);
	}
}
