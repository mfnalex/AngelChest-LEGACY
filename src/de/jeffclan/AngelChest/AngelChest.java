package de.jeffclan.AngelChest;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AngelChest {

	final Inventory inv;
	Block block;
	UUID owner;
	Hologram hologram;
	boolean isProtected;
	long configDuration;
	long taskStart;
	
	
	public AngelChest(UUID owner,Block block, ItemStack[] playerItems,AngelChestPlugin plugin) {
		
		this.owner=owner;
		this.block=block;
		this.isProtected = plugin.getServer().getPlayer(owner).hasPermission("angelchest.protect");
		
		String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
		String inventoryName = String.format(plugin.messages.ANGELCHEST_INVENTORY_NAME, plugin.getServer().getPlayer(owner).getName());
		
		inv = Bukkit.createInventory(null, 54, inventoryName);
		if(playerItems != null && playerItems.length>0) { inv.addItem(playerItems); }
		createChest(block);
		hologram = new Hologram(block, hologramText,plugin);
		
		AngelChest me = this;
		
		configDuration = plugin.getConfig().getLong("angelchest-duration");
		taskStart = System.currentTimeMillis();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if(plugin.isAngelChest(block)) {
					Utils.destroyAngelChest(block, me, plugin);
					Player player = plugin.getServer().getPlayer(owner);
					if(player != null) {
						player.sendMessage(plugin.messages.MSG_ANGELCHEST_DISAPPEARED);
					}
				}
			}
		}, configDuration * 20);
	}
	
	private void createChest(Block block) {
		block.setType(Material.CHEST);
	}
	
	public void unlock() {
		this.isProtected = false;
	}
	
	public void saveToFile() {
		
	}
	
	public long secondsRemaining() {
		return configDuration - ((System.currentTimeMillis() - taskStart) / 1000);
	}
}