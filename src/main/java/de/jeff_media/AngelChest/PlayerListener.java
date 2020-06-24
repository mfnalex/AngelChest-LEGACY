package de.jeff_media.AngelChest;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListener implements Listener {

	Main plugin;

	PlayerListener(Main plugin) {
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
	public void spawnAngelChest(PlayerDeathEvent event) {
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
		

		//if (!angelChestBlock.getType().equals(Material.AIR)) {
			List<Block> blocksNearby = Utils.getPossibleChestLocations(angelChestBlock.getLocation(),
					plugin.getConfig().getInt("max-radius"),plugin);

			if (blocksNearby.size() > 0) {
				Utils.sortBlocksByDistance(angelChestBlock, blocksNearby);
				
				fixedAngelChestBlock = blocksNearby.get(0);

			}

		//}

		AngelChest ac =new AngelChest(p.getUniqueId(), fixedAngelChestBlock, p.getInventory(), plugin); 
		plugin.angelChests.put(fixedAngelChestBlock,ac);
		
		if(!event.getKeepLevel() && event.getDroppedExp()!=0 && plugin.getConfig().getBoolean("preserve-xp")) {
			if(plugin.getConfig().getBoolean("full-xp")) {
				ac.experience=ExperienceUtils.getPlayerExp(p);
			} else {
				ac.experience=event.getDroppedExp();
			}
			event.setDroppedExp(0);
		}

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
	public void onAngelChestRightClick(PlayerInteractEvent event) {
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
		openAngelChest(p, block, angelChest);

		event.setCancelled(true);
	}

	void openAngelChest(Player p, Block block, AngelChest angelChest) {
		if(angelChest.experience!=0) {
			p.giveExp(angelChest.experience);
			angelChest.experience=0;
		}
		boolean succesfullyStoredEverything = Utils.tryToMergeInventories(angelChest, p.getInventory());
		if (succesfullyStoredEverything) {
			p.sendMessage(plugin.messages.MSG_YOU_GOT_YOUR_INVENTORY_BACK);
			angelChest.destroy();
		} else {
			p.sendMessage(plugin.messages.MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK);
			p.openInventory(angelChest.overflowInv);
		}
	}

	@EventHandler
	public void onAngelChestClose(InventoryCloseEvent event) {

		for (AngelChest angelChest : plugin.angelChests.values()) {
			if (angelChest.overflowInv.equals(event.getInventory())) {
				// This is an AngelChest!

				Inventory inv = event.getInventory();
				if (Utils.isEmpty(inv)) {
					// plugin.angelChests.remove(Utils.getKeyByValue(plugin.angelChests,
					// angelChest));
					angelChest.destroy();
					// event.getPlayer().sendMessage("You have emptied an AngelChest. It is now
					// gone.");
				}

				return;
			}
		}
	}
	
    @EventHandler
    public void onArmorStandRightClick(PlayerInteractAtEntityEvent event)
    {
    	if(event.getRightClicked()==null) {
    		return;
    	}
        if (!event.getRightClicked().getType().equals(EntityType.ARMOR_STAND))
        {
            return;
        }
        if(!plugin.isAngelChestHologram(event.getRightClicked())) {
        	return;
        }
        AngelChest as = plugin.getAngelChestByHologram((ArmorStand) event.getRightClicked());
		if (!as.owner.equals(event.getPlayer().getUniqueId())
				&& !event.getPlayer().hasPermission("angelchest.protect.ignore") && as.isProtected) {
			event.getPlayer().sendMessage(plugin.messages.MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS);
			event.setCancelled(true);
			return;
		}
		openAngelChest(event.getPlayer(), as.block, as);
    }

}
