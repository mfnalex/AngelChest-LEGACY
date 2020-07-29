package de.jeff_media.AngelChest;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MinepacksHook {

	//Method isBackpackitem;

	public static boolean isMinepacksBackpack(ItemStack is,Main plugin) {
		return false;

		// Currently disabled: https://github.com/GeorgH93/Minepacks/issues/105


		/*
		if(is==null) return false;
		plugin.debug("Minepacks-Hook");
		if(!(Bukkit.getPluginManager().getPlugin("Minepacks") != null)) {
			return false;
		}
		MinepacksPlugin minepacks = (MinepacksPlugin) Bukkit.getPluginManager().getPlugin("Minepacks");
		plugin.debug("Checking if this is a Minepacks backpack");
		try {
			minepacks.getClass().getMethod("isBackpackItem", ItemStack.class);

				if(minepacks.isBackpackItem(is)) {
				//if(minepacks.isBackpackItem(is)) {
					plugin.debug("This is a Minepacks backpack");
					return true;

			}
		} catch (NoSuchMethodException | SecurityException e) {
			plugin.getLogger().warning("You are using a version of Minepacks that is too old and does not implement every API method needed by AngelChest. Minepacks hook will be disabled.");
			if(plugin.debug) e.printStackTrace();
			return false;
		}
		plugin.debug("This is not a Minepacks backpack");
		return false;
		*/

	}
}
