package de.jeffclan.AngelChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {

	public static boolean isEmpty(Inventory inv) {

		if (inv.getContents() == null)
			return true;
		if (inv.getContents().length == 0)
			return true;

		for (ItemStack itemStack : inv.getContents()) {
			if (itemStack == null)
				continue;
			if (itemStack.getAmount() == 0)
				continue;
			if (itemStack.getType().equals(Material.AIR))
				continue;
			return false;
		}

		return true;

	}

	public static boolean isEmpty(ItemStack itemStack) {
		if (itemStack == null)
			return true;
		if (itemStack.getAmount() == 0)
			return true;
		if (itemStack.getType().equals(Material.AIR))
			return true;
		return false;
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public static boolean isWorldEnabled(World world, AngelChestPlugin plugin) {
		
		if(plugin.disabledWorlds.contains(world.getName().toLowerCase())) {
			return false;
		}
		
		return true;
	}

	/**
	 * Puts everything from source into destination.
	 * 
	 * @param source
	 * @param dest
	 * @return true if everything could be stored, otherwise false
	 */
	public static boolean tryToMergeInventories(Inventory source, Inventory dest) {

		ArrayList<ItemStack> sourceItemsWithoutAir = new ArrayList<ItemStack>();
		for (ItemStack item : source.getContents()) {
			if (isEmpty(item))
				continue;
			sourceItemsWithoutAir.add(item);
		}
		HashMap<Integer, ItemStack> unstorable = dest
				.addItem(sourceItemsWithoutAir.toArray(new ItemStack[sourceItemsWithoutAir.size()]));
		source.clear();

		if (unstorable.size() == 0) {
			return true;
		}

		source.addItem(unstorable.values().toArray(new ItemStack[unstorable.size()]));

		return false;
	}

	public static void destroyAngelChest(Block block, AngelChest angelChest, AngelChestPlugin plugin) {
		

		if (!plugin.isAngelChest(block))
			return;

		block.setType(Material.AIR);
		
		for (UUID uuid : angelChest.hologram.armorStandUUIDs) {
			plugin.getServer().getEntity(uuid).remove();
		}
		
		for(ArmorStand armorStand : angelChest.hologram.armorStands) {
			
			armorStand.remove();
		}
		
		angelChest.hologram.destroy();

		// drop contents
		if (!Utils.isEmpty(angelChest.inv)) {
			//plugin.getLogger().info("Dropped AngelChest's contents at " + block.getLocation().toString());
			for (ItemStack itemStack : angelChest.inv.getContents()) {
				if (Utils.isEmpty(itemStack))
					continue;
				block.getWorld().dropItem(block.getLocation(), itemStack);
			}
		}

		angelChest.inv.clear();
		plugin.angelChests.remove(block);
		block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
	}

	public static void sendDelayedMessage(Player p, String message, long delay, AngelChestPlugin plugin) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (p == null)
					return;
				if (!(p instanceof Player))
					return;
				if (!p.isOnline())
					return;
				p.sendMessage(message);
			}
		}, delay);
	}

	public static ArrayList<AngelChest> getAllAngelChestsFromPlayer(Player p, AngelChestPlugin plugin) {
		ArrayList<AngelChest> angelChests = new ArrayList<AngelChest>();
		for (AngelChest angelChest : plugin.angelChests.values()) {
			if (!angelChest.owner.equals(p))
				continue;
			angelChests.add(angelChest);
		}
		return angelChests;
	}
	
	 public static List<Block> getNearbyBlocks(Location location, int radius) {
	        List<Block> blocks = new ArrayList<Block>();
	        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
	            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
	                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
	                   blocks.add(location.getWorld().getBlockAt(x, y, z));
	                }
	            }
	        }
	        return blocks;
	    }

	public static String locationToString(Block block) {
		if(block == null) {
			return "<none>";
		}
		return String.format("%d, %d, %d (%s)", block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	}
	 
}