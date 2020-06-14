package de.jeffclan.AngelChest;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AngelChestTeleporter {
	protected static void teleportPlayerToChest(AngelChestPlugin plugin, Player p, String[] args) {
		if(!p.hasPermission("angelchest.tp")) return;
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int z = Integer.parseInt(args[3]);
		String world = args[4];
		p.teleport(new Location(plugin.getServer().getWorld(world), x, y, z), TeleportCause.PLUGIN);
	}
}
