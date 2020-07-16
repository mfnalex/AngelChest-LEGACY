package de.jeff_media.AngelChest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class PlayerListener implements Listener {

	Main plugin;

	PlayerListener(Main plugin) {
		this.plugin = plugin;

		plugin.debug("PlayerListener created");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		plugin.registerPlayer(event.getPlayer());

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		plugin.unregisterPlayer(event.getPlayer());
	}

	@EventHandler
	public void spawnAngelChest(PlayerDeathEvent event) {

		Objects.requireNonNull(plugin.chestMaterial,"Chest Material is null!");

		/*System.out.println("test");
		if(plugin.chestMaterial==null) {
			System.out.println("chestmat is null");
			return;
		}

		plugin.debug(plugin.chestMaterial.name());*/

		plugin.debug("PlayerListener -> spawnAngelChest");
		Player p = event.getEntity();
		if (!p.hasPermission("angelchest.use")) {
			plugin.debug("Cancelled: no permission (angelchest.use)");
			return;
		}

		if (event.getKeepInventory()) {
			plugin.debug("Cancelled: event#getKeepInventory() == true");
			return;
		}
		
		if(!Utils.isWorldEnabled(p.getLocation().getWorld(), plugin)) {
			plugin.debug("Cancelled: world disabled ("+p.getLocation().getWorld());
			return;
		}

		if(plugin.worldGuardHandler.isBlacklisted(p.getLocation().getBlock())) {
			plugin.debug("Cancelled: region disabled.");
			return;
		}

		// Don't do anything if player's inventory is empty anyway
		if (event.getDrops() == null || event.getDrops().size() == 0) {
			plugin.debug("Cancelled: event#getDrops == null || event#getDrops#size =0 0");
			Utils.sendDelayedMessage(p, plugin.messages.MSG_INVENTORY_WAS_EMPTY, 1, plugin);
			return;
		}

		// Enable keep inventory to prevent drops (this is not preventing the drops at the moment due to spigot)
		event.setKeepInventory(true);

		Block tmp;

		plugin.debug("Debug 1");

		if(p.getLocation().getBlockY() < 1) {
			Location ltmp = p.getLocation();
			ltmp.setY(1);
			tmp = ltmp.getBlock();
		} else {
			tmp = p.getLocation().getBlock();
		}
		
		Block angelChestBlock = Utils.findSafeBlock(tmp, plugin);
		plugin.debug("Debug 2");

		AngelChest ac =new AngelChest(p,p.getUniqueId(), angelChestBlock, p.getInventory(), plugin);
		plugin.angelChests.put(angelChestBlock,ac);
		plugin.debug("Debug 3");
		
		if(!event.getKeepLevel() && event.getDroppedExp()!=0 && p.hasPermission("angelchest.xp")) {
			if(p.hasPermission("angelchest.xp.levels")) {
				ac.levels = p.getLevel();
			}
			ac.experience=event.getDroppedExp();
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
					try {
						AngelChestCommandUtils.sendListOfAngelChests(plugin, p);
					} catch(Throwable throwable) {
						//e.printStackTrace();
					}
				}},2);
		}
	}



	@EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = false)
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

		if((p.hasPermission("angelchest.xp") || p.hasPermission("angelchest.xp.levels")) && angelChest.experience!=0) {
			p.giveExp(angelChest.experience);
			angelChest.levels = 0;
			angelChest.experience=0;
		}
		if(p.hasPermission("angelchest.xp.levels") && angelChest.levels!=0 && angelChest.levels> p.getLevel()) {
			p.setExp(0);
			p.setLevel(angelChest.levels);
			angelChest.levels = 0;
			angelChest.experience = 0;
		}

		boolean succesfullyStoredEverything = Utils.tryToMergeInventories(angelChest, p.getInventory());
		if (succesfullyStoredEverything) {
			p.sendMessage(plugin.messages.MSG_YOU_GOT_YOUR_INVENTORY_BACK);
			angelChest.destroy();
			angelChest.remove();
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
