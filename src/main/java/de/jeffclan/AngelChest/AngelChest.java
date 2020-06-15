package de.jeffclan.AngelChest;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AngelChest {

	final ItemStack[] armorInv;
	final ItemStack[] storageInv;
	final ItemStack[] extraInv;
	final Inventory overflowInv;
	Block block;
	UUID owner;
	Hologram hologram;
	boolean isProtected;
	long configDuration;
	long taskStart;
	int experience = 0;
	AngelChestPlugin plugin;
	
	
	public AngelChest(UUID owner,Block block, PlayerInventory playerItems, AngelChestPlugin plugin) {
		
		this.plugin=plugin;
		this.owner=owner;
		this.block=block;
		this.isProtected = plugin.getServer().getPlayer(owner).hasPermission("angelchest.protect");
		
		String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
		String inventoryName = String.format(plugin.messages.ANGELCHEST_INVENTORY_NAME, plugin.getServer().getPlayer(owner).getName());
		overflowInv = Bukkit.createInventory(null, 54, inventoryName);
		createChest(block);
		hologram = new Hologram(block, hologramText,plugin);
		
		// Remove curse of vanishing equipment
		for(ItemStack i : playerItems.getContents()) {
			if(!Utils.isEmpty(i)) {
				if(i.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
					playerItems.remove(i);
				}
			}
		}
		
		armorInv = playerItems.getArmorContents();
		storageInv = playerItems.getStorageContents();
		extraInv = playerItems.getExtraContents();
		
		AngelChest me = this;
		
		configDuration = plugin.getConfig().getLong("angelchest-duration");
		taskStart = System.currentTimeMillis();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if(plugin.isAngelChest(block)) {
					destroy();
					Player player = plugin.getServer().getPlayer(owner);
					if(player != null) {
						player.sendMessage(plugin.messages.MSG_ANGELCHEST_DISAPPEARED);
					}
				}
			}
		}, configDuration * 20);
	}
	
	private void createChest(Block block) {
		block.setType(plugin.chestMaterial);
	}
	
	public void unlock() {
		this.isProtected = false;
	}
	
	public void saveToFile() {
		// Just stuff everything into a YAMLConfiguration
	}
	
	public void destroy() {
		if (!plugin.isAngelChest(block))
			return;

		block.setType(Material.AIR);
		
		for (UUID uuid : hologram.armorStandUUIDs) {
			if(plugin.getServer().getEntity(uuid)!=null) {
				plugin.getServer().getEntity(uuid).remove();
			}
		}
		
		for(ArmorStand armorStand : hologram.armorStands) {
			if(armorStand==null) continue;
			armorStand.remove();
		}
		
		hologram.destroy();

		// drop contents
		Utils.dropItems(block, armorInv);
		Utils.dropItems(block, storageInv);
		Utils.dropItems(block, extraInv);
		Utils.dropItems(block, overflowInv);
		
		if(experience>0) {
			Utils.dropExp(block, experience);
		}

		plugin.angelChests.remove(block);
		block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
	}
	
	public long secondsRemaining() {
		long seconds = configDuration - ((System.currentTimeMillis() - taskStart) / 1000);
		if(seconds<0) seconds = 0;
		return seconds;
	}
}