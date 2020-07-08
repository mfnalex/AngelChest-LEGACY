package de.jeff_media.AngelChest;

import java.util.List;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AngelChestCommandUtils {

	static boolean hasEnoughMoney(Player p, double money, Main main) {

		if(money <= 0) {
			return true;
		}

		Plugin v = main.getServer().getPluginManager().getPlugin("Vault");

		if(v == null) {
			return true;
		}

		RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) return true;

		if(rsp.getProvider()==null) return true;

		Economy econ = rsp.getProvider();

		if(econ.getBalance(p)>=money) {
			econ.withdrawPlayer(p,"AngelChest Teleport",money);
			return true;
		} else {
			p.sendMessage(main.messages.MSG_NOT_ENOUGH_MONEY);
			return false;
		}

	}

	protected static void teleportPlayerToChest(Main plugin, Player p, String[] args) {
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
		
		double price = plugin.getConfig().getDouble("price-teleport");
		if(price>0 && !hasEnoughMoney(p,price,plugin)) {
			return;
		}

		List<Block> possibleSpawnPoints = Utils.getPossibleChestLocations(loc, plugin.getConfig().getInt("max-radius"), plugin);
		Utils.sortBlocksByDistance(loc.getBlock(), possibleSpawnPoints);
		
		Location teleportLocation = loc;
		
		if(possibleSpawnPoints.size()>0) {
			teleportLocation = possibleSpawnPoints.get(0).getLocation();
		}
		
		teleportLocation.setDirection(loc.toVector().subtract(teleportLocation.toVector()));
		teleportLocation.add(0.5,0,0.5);
		
		p.teleport(teleportLocation, TeleportCause.PLUGIN);
	}
	
	protected static void unlockSingleChest(Main plugin, Player p, String[] args) {
//		if(!p.hasPermission("angelchest.tp")) {
//			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
//			return;
//		}
		int x = Integer.parseInt(args[0]);
		int y = Integer.parseInt(args[1]);
		int z = Integer.parseInt(args[2]);
		String world = args[3];
		
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
		if(!ac.isProtected) {
			p.sendMessage(ChatColor.RED+"This AngelChest is already unlocked.");
			return;
		}
		
		ac.unlock();
		p.sendMessage(plugin.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
	}
}
