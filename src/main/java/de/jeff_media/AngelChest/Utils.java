package de.jeff_media.AngelChest;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class Utils {

	public static boolean isSafeSpot(Location location) {
		if(!location.getBlock().getType().isSolid()
				&&

				(location.getBlock().getRelative(0,-1,0).getType().isSolid()
						|| location.getBlock().getRelative(0,-1,0).getType() == Material.WATER)

				&& !isAboveLava(location,10)
				&& location.getBlockY() > 0) {
			return true;
		}
		return false;
	}

	public static List<Block> getPossibleTPLocations(Location location, int radius, Main plugin) {
		List<Block> blocks = new ArrayList<>();
		for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
			for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
				for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
					Block block = location.getWorld().getBlockAt(x,y,z);
					if(isSafeSpot(location))
						blocks.add(block);
				}
			}
		}
		return blocks;
	}

	static boolean isAboveLava(Location loc, int height) {
		Block block = loc.getBlock();
		for(int i = 0; i < height; i++) {
			if(block.getRelative(0,-i,0).getType()==Material.LAVA) return true;
		}
		return false;
	}

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
		return itemStack.getType().equals(Material.AIR);
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public static boolean isWorldEnabled(World world, Main plugin) {
		
		for(String worldName : plugin.disabledWorlds) {
			if(world.getName().equalsIgnoreCase(worldName)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static Block findSafeBlock(Block playerLoc, Main plugin) {
		Block fixedAngelChestBlock = playerLoc;

		//System.out.println(plugin.getConfig().getInt("max-radius"));

		if (!playerLoc.getType().equals(Material.AIR)) {
			List<Block> blocksNearby = Utils.getPossibleChestLocations(playerLoc.getLocation(),
					plugin.getConfig().getInt("max-radius"), plugin);

			if (blocksNearby.size() > 0) {
				Utils.sortBlocksByDistance(fixedAngelChestBlock, blocksNearby);
				fixedAngelChestBlock = blocksNearby.get(0);
			}
		}

		return fixedAngelChestBlock;
	}


	/**
	 * Puts everything from source into destination.
	 * 
	 * @param source
	 * @param dest
	 * @return true if everything could be stored, otherwise false
	 */
	public static boolean tryToMergeInventories(AngelChest source, PlayerInventory dest) {
		if(!isEmpty(source.overflowInv))
			return false; // Already applied inventory

		ArrayList<ItemStack> overflow = new ArrayList<>();
		ItemStack[] armor_merged = dest.getArmorContents();
		ItemStack[] storage_merged = dest.getStorageContents();
		ItemStack[] extra_merged = dest.getExtraContents();

		// Try to auto-equip armor
		for(int i = 0; i < armor_merged.length; i ++) {
			if(isEmpty(armor_merged[i])) {
				armor_merged[i] = source.armorInv[i];
			} else if(!isEmpty(source.armorInv[i])) {
				overflow.add(source.armorInv[i]);
			}
			source.armorInv[i] = null;
		}

		// Try keep storage layout
		for(int i = 0; i < storage_merged.length; i ++) {
			if(isEmpty(storage_merged[i])) {
				storage_merged[i] = source.storageInv[i];
			} else if(!isEmpty(source.storageInv[i])) {
				overflow.add(source.storageInv[i]);
			}
			source.storageInv[i] = null;
		}

		// Try to merge extra (offhand?)
		for(int i = 0; i < extra_merged.length; i ++) {
			if(isEmpty(extra_merged[i])) {
				extra_merged[i] = source.extraInv[i];
			} else if(!isEmpty(source.extraInv[i])) {
				overflow.add(source.extraInv[i]);
			}
			source.extraInv[i] = null;
		}

		// Apply merged inventories
		dest.setArmorContents(armor_merged);
		dest.setStorageContents(storage_merged);
		dest.setExtraContents(extra_merged);

		// Try to place overflow items into empty storage slots
		HashMap<Integer, ItemStack> unstorable = dest
			.addItem(overflow.toArray(new ItemStack[0]));
		source.overflowInv.clear();

		if (unstorable.size() == 0) {
			return true;
		}

		source.overflowInv.addItem(unstorable.values()
			.toArray(new ItemStack[0]));

		return false;
	}

	public static void dropItems(Block block, ItemStack[] invContent) {
		for (ItemStack itemStack : invContent) {
			if (Utils.isEmpty(itemStack))
				continue;
			block.getWorld().dropItem(block.getLocation(), itemStack);
		}
	}

	static <T> T[] appendToArray(T[] arr, T element) {
		final int N = arr.length;
		arr = Arrays.copyOf(arr, N + 1);
		arr[N] = element;
		return arr;
	}
	
	public static void dropExp(Block block, int xp) {
		ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
		orb.setExperience(xp);
	}

	public static void dropItems(Block block, Inventory inv) {
		dropItems(block, inv.getContents());
		inv.clear();
	}
	
	public static void sendDelayedMessage(Player p, String message, long delay, Main plugin) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (p == null)
				return;
			if (!(p instanceof Player))
				return;
			if (!p.isOnline())
				return;
			p.sendMessage(message);
		}, delay);
	}

	public static ArrayList<AngelChest> getAllAngelChestsFromPlayer(Player p, Main plugin) {
		ArrayList<AngelChest> angelChests = new ArrayList<>();
		for (AngelChest angelChest : plugin.angelChests.values()) {
			if (!angelChest.owner.equals(p.getUniqueId()))
				continue;
			angelChests.add(angelChest);
		}
		return angelChests;
	}
	
	public static void sortBlocksByDistance(Block angelChestBlock, List<Block> blocksNearby) {
		blocksNearby.sort((b1, b2) -> {
			double dist1 = b1.getLocation().distance(angelChestBlock.getLocation());
			double dist2 = b2.getLocation().distance(angelChestBlock.getLocation());
			return Double.compare(dist1, dist2);
		});
	}
	
	 /*public static List<Block> getNearbyBlocks(Location location, int radius) {
	        List<Block> blocks = new ArrayList<Block>();
	        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
	            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
	                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
	                   blocks.add(location.getWorld().getBlockAt(x, y, z));
	                }
	            }
	        }
	        return blocks;
	    }*/
	
	 public static List<Block> getPossibleChestLocations(Location location, int radius, Main plugin) {
	        List<Block> blocks = new ArrayList<>();
	        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
	            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
	                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
						Block block = location.getWorld().getBlockAt(x,y,z);
						Block oneBelow = location.getWorld().getBlockAt(x,y - 1,z);
						if(plugin.onlySpawnIn.contains(block.getType())
								&& !plugin.dontSpawnOn.contains(oneBelow.getType())
								&& y > 0) {
	                		blocks.add(block);
	                	}
	                }
	            }
	        }
	        return blocks;
	    }

	/*public static String locationToString(Block block) {
		if(block == null) {
			return "<none>";
		}
		return String.format(ChatColor.GREEN+"X:"+ChatColor.WHITE+" %d " + ChatColor.GREEN + "Y: " + ChatColor.WHITE+"%d " +
		ChatColor.GREEN+"Z: " + ChatColor.WHITE+"%d (%s)", block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	}*/
	
	static void renameFileInPluginDir(Main plugin,String oldName, String newName) {
		File oldFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + oldName);
		File newFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + newName);
		oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile());
	}

	// from sk89q
	public static String getCardinalDirection(Player player) {
		double rotation = player.getLocation().getYaw() % 360;
		if (rotation < 0) {
			rotation += 360.0;
		}
		if (0 <= rotation && rotation < 22.5) {
			return "S";
		}
		if (22.5 <= rotation && rotation < 67.5) {
			return "SW";
		}
		if (67.5 <= rotation && rotation < 112.5) {
			return "W";
		}
		if (112.5 <= rotation && rotation < 157.5) {
			return "NW";
		}
		if (157.5 <= rotation && rotation < 202.5) {
			return "N";
		}
		if (202.5 <= rotation && rotation < 247.5) {
			return "NE";
		}
		if (247.5 <= rotation && rotation < 292.5) {
			return "E";
		}
		if (292.5 <= rotation && rotation < 337.5) {
			return "SE";
		}
		if (337.5 <= rotation && rotation < 360.0) {
			return "S";
		}
		return null;

	}

	/**
	 * Somehow the holograms are often not clickable after creation. It gets fixed by using /acreload or fetching the chest.
	 * Instead of debugging this now, I just added this reload method. I know it's a stupid fix but I want this
	 * fixed now. TODO: Remove this and properly check why some holograms get not get recognized properly
	 */
	static void reloadAngelChest(AngelChest ac, Main main) {
		ac.saveToFile();
		main.loadAllAngelChestsFromFile();

	}

    public static boolean isEmpty(ItemStack[] items) {
		if(items==null) return true;
		return (items.length==0);
    }
}