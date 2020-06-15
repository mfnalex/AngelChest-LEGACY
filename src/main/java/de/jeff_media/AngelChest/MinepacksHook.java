package de.jeff_media.AngelChest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;

public class MinepacksHook {
	public static boolean isMinepacksBackpack(ItemStack is,Main plugin) {
		if(is==null) return false;
		if(!(Bukkit.getPluginManager().getPlugin("Minepacks") instanceof MinepacksPlugin)) {
			return false;
		}
		MinepacksPlugin minepacks = (MinepacksPlugin) Bukkit.getPluginManager().getPlugin("Minepacks");
		try {
			if(minepacks.getClass().getMethod("isBackpackItem", ItemStack.class) != null) {
				if(minepacks.isBackpackItem(is)) return true;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			plugin.getLogger().warning("You are using a version of Minepacks that is too old and does not implement every API method needed by AngelChest. Minepacks hook will be disabled.");
			return false;
		}
		return false;
	}
}
