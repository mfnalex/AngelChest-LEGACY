package de.jeff_media.AngelChest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
			if(plugin.getConfig().getBoolean("ignore-keep-inventory",false)) {
				plugin.debug("Cancelled: event#getKeepInventory() == true");
				return;
			} else {
				plugin.debug("event#getKeepInventory() == true but we ignore it because of config settings");
				event.setKeepInventory(false);
			}
		}
		
		if(!Utils.isWorldEnabled(p.getLocation().getWorld(), plugin)) {
			plugin.debug("Cancelled: world disabled ("+p.getLocation().getWorld());
			return;
		}

		if(plugin.worldGuardHandler.isBlacklisted(p.getLocation().getBlock())) {
			plugin.debug("Cancelled: region disabled.");
			return;
		}

		if(plugin.getConfig().getBoolean("only-spawn-chests-if-player-may-build")
				&& !ProtectionUtils.playerMayBuildHere(p,p.getLocation(),plugin)) {
			plugin.debug("Cancelled: BlockPlaceEvent cancelled");
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

		// Delete players inventory except excluded items
		clearInventory(p.getInventory());
		
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
						AngelChestCommandUtils.sendListOfAngelChests(plugin, p, p);
					} catch(Throwable throwable) {
						//e.printStackTrace();
					}
				}},2);
		}

		//Utils.reloadAngelChest(ac,plugin);

	}

	private void clearInventory(Inventory inv) {
		for(int i = 0; i < inv.getSize(); i++) {
			if(plugin.hookUtils.keepOnDeath(inv.getItem(i))) {
				continue;
			}
			inv.setItem(i,null);
		}

	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(!plugin.getConfig().getBoolean("auto-respawn")) return;
		int delay = plugin.getConfig().getInt("auto-respawn-delay");

		Bukkit.getScheduler().runTaskLater(plugin,() -> {
			if(e.getEntity().isDead()) {
				e.getEntity().spigot().respawn();
			}
		},1L+(delay*20));
	}

	@EventHandler
	public void onDeathBecauseTotemNotEquipped(EntityResurrectEvent e) {
		if(!(e.getEntity() instanceof Player)) return;

		if(!plugin.getConfig().getBoolean("totem-of-undying-works-everywhere")) return;

		Player p = (Player) e.getEntity();


		for(ItemStack is : p.getInventory()) {
			if(is==null) continue;
			if(is.getType()== Material.TOTEM_OF_UNDYING) {
				e.setCancelled(false);
				is.setAmount(is.getAmount()-1);
				return;
			}
		}

	}

	/* Debug
	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent e) {

		//Player p = e.getPlayer();
		//System.out.println("Respawn");
		for(ItemStack itemStack : p.getInventory()) {
			if(itemStack==null) continue;
			System.out.println(itemStack.getType().name());
		}

	}*/

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



		boolean succesfullyStoredEverything = false;
		boolean isOwnChest = angelChest.owner == p.getUniqueId();

		succesfullyStoredEverything = Utils.tryToMergeInventories(angelChest, p.getInventory());
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
			if (!angelChest.overflowInv.equals(event.getInventory())) {
				continue;
			}

			Inventory inv = event.getInventory();
			if (Utils.isEmpty(angelChest.overflowInv)
					&& Utils.isEmpty(angelChest.armorInv)
					&& Utils.isEmpty(angelChest.extraInv)
					&& Utils.isEmpty(angelChest.storageInv)) {
				// plugin.angelChests.remove(Utils.getKeyByValue(plugin.angelChests,
				// angelChest));
				angelChest.destroy();

				plugin.debug("Inventory empty, removing chest");
				// event.getPlayer().sendMessage("You have emptied an AngelChest. It is now
				// gone.");
			}

			return;
		}
	}
	
    @EventHandler
    public void onArmorStandRightClick(PlayerInteractAtEntityEvent event)
    {
    	if(event.getRightClicked()==null) {
			System.out.println(1);
    		return;
    	}
        if (!event.getRightClicked().getType().equals(EntityType.ARMOR_STAND))
        {
			System.out.println(2);

            return;
        }
        AtomicReference<AngelChest> as  = new AtomicReference<>();
        if(plugin.isAngelChestHologram(event.getRightClicked())) {
			as.set(plugin.getAngelChestByHologram((ArmorStand) event.getRightClicked()));
			//System.out.println("GETBYHOLOGRAM1");
        }

        else {
        	plugin.blockArmorStandCombinations.forEach((combination -> {
        		if(event.getRightClicked().getUniqueId().equals(combination.armorStand.getUniqueId())) {
        			as.set(plugin.getAngelChest(combination.block));
					//System.out.println("GETBYHOLOGRAM2");
				}
			}));

		}
		System.out.println(event.getRightClicked().getUniqueId());

		System.out.println(4);
        if(as.get()==null) return;
		System.out.println(5);

		if (!as.get().owner.equals(event.getPlayer().getUniqueId())
				&& !event.getPlayer().hasPermission("angelchest.protect.ignore") && as.get().isProtected) {
			event.getPlayer().sendMessage(plugin.messages.MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS);
			event.setCancelled(true);
			return;
		}
		openAngelChest(event.getPlayer(), as.get().block, as.get());
    }

}
