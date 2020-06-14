package de.jeffclan.AngelChest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListener implements Listener {

	AngelChestPlugin plugin;

	PlayerListener(AngelChestPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		plugin.registerPlayer(event.getPlayer());

		if (event.getPlayer().isOp()) {
			plugin.updateChecker.sendUpdateMessage(event.getPlayer());
		}
		
		if(event.getPlayer().getName().equalsIgnoreCase("mfnalex")) {
			plugin.debug=true;
		}

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		plugin.unregisterPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		if (!p.hasPermission("angelchest.use")) {
			return;
		}

		if (event.getKeepInventory()) {
			return;
		}
		
		if(!Utils.isWorldEnabled(p.getLocation().getWorld(), plugin)) {
			return;
		}

		// Don't do anything if player's inventory is empty anyway
		if (event.getDrops() == null || event.getDrops().size() == 0) {
			Utils.sendDelayedMessage(p, plugin.messages.MSG_INVENTORY_WAS_EMPTY, 1, plugin);
			return;
		}

		// Enable keep inventory to prevent drops (this is not preventing the drops at the moment due to spigot)
		event.setKeepInventory(true);

		Block tmp;
		
		if(p.getLocation().getBlockY() < 1) {
			Location ltmp = p.getLocation();
			ltmp.setY(1);
			tmp = ltmp.getBlock();
		} else {
			tmp = p.getLocation().getBlock();
		}
		
		Block angelChestBlock = tmp;
		
		Block fixedAngelChestBlock = angelChestBlock;
		
		//System.out.println(plugin.getConfig().getInt("max-radius"));
		

		if (!angelChestBlock.getType().equals(Material.AIR)) {
			List<Block> blocksNearby = Utils.getPossibleChestLocations(angelChestBlock.getLocation(),
					plugin.getConfig().getInt("max-radius"));


			if (blocksNearby.size() > 0) {
				Collections.sort(blocksNearby, new Comparator<Block>() {
					public int compare(Block b1, Block b2) {
						double dist1 = b1.getLocation().distance(angelChestBlock.getLocation());
						double dist2 = b2.getLocation().distance(angelChestBlock.getLocation());
						if (dist1 > dist2)
							return 1;
						if (dist2 > dist1)
							return -1;
						return 0;
					}
				});
				
				fixedAngelChestBlock = blocksNearby.get(0);

			}

		}

		plugin.angelChests.put(fixedAngelChestBlock,
				new AngelChest(p.getUniqueId(), fixedAngelChestBlock, p.getInventory(), plugin));

		// Delete players inventory
		p.getInventory().clear();
		
		// Clear the drops
		event.getDrops().clear();

		// send message after one twentieth second
		Utils.sendDelayedMessage(p, plugin.messages.MSG_ANGELCHEST_CREATED, 1, plugin);

		if(plugin.getConfig().getBoolean("show-location")) {
			//Utils.sendDelayedMessage(p, String.format(plugin.messages.MSG_ANGELCHEST_LOCATION , Utils.locationToString(fixedAngelChestBlock) ), 2, plugin);
			/*final int x = fixedAngelChestBlock.getX();
			final int y = fixedAngelChestBlock.getY();
			final int z = fixedAngelChestBlock.getZ();
			final String world = fixedAngelChestBlock.getWorld().getName();
			String locString = Utils.locationToString(fixedAngelChestBlock);*/
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					//TpLinkUtil.sendLink(p, String.format(plugin.messages.MSG_ANGELCHEST_LOCATION , locString )+" ", "/acinfo tp "+x+" "+y+" "+z+" "+world);
					plugin.commandListExecutor.sendListOfAngelChests(p);
				}},2);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		System.out.println("PlayerInteractEvent");
		Player p = event.getPlayer();
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;
		if (event.getClickedBlock() == null)
			return;
		Block block = event.getClickedBlock();
		if (!plugin.isAngelChest(block))
			return;
		AngelChest angelChest = plugin.angelChests.get(block);
		// event.getPlayer().sendMessage("This is " + angelChest.owner.getName()+"'s
		// AngelChest.");
		// Test here if player is allowed to open THIS angelchest
		if (angelChest.isProtected && !event.getPlayer().getUniqueId().equals(angelChest.owner)
				&& !event.getPlayer().hasPermission("angelchest.protect.ignore")) {
			event.getPlayer().sendMessage(plugin.messages.MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS);
			event.setCancelled(true);
			return;
		}
		// p.openInventory(angelChest.inv);
		boolean succesfullyStoredEverything = Utils.tryToMergeInventories(angelChest, p.getInventory());
		if (succesfullyStoredEverything) {
			event.getPlayer().sendMessage(plugin.messages.MSG_YOU_GOT_YOUR_INVENTORY_BACK);
			Utils.destroyAngelChest(block, angelChest, plugin);
		} else {
			event.getPlayer().sendMessage(plugin.messages.MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK);
			event.getPlayer().openInventory(angelChest.overflowInv);
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {

		for (AngelChest angelChest : plugin.angelChests.values()) {
			if (angelChest.overflowInv.equals(event.getInventory())) {
				// This is an AngelChest!

				Inventory inv = event.getInventory();
				if (Utils.isEmpty(inv)) {
					// plugin.angelChests.remove(Utils.getKeyByValue(plugin.angelChests,
					// angelChest));
					Utils.destroyAngelChest(Utils.getKeyByValue(plugin.angelChests, angelChest), angelChest, plugin);
					// event.getPlayer().sendMessage("You have emptied an AngelChest. It is now
					// gone.");
				}

				return;
			}
		}
	}

}
