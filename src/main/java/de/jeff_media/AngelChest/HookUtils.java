package de.jeff_media.AngelChest;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class HookUtils {

    final static String SOULBOUND_LORE= ChatColor.GRAY + "Soulbound";

    static boolean isSlimefunSoulbound(ItemStack item) {

        if(item==null) return false;
        if(!item.hasItemMeta()) return false;
        if(!item.getItemMeta().hasLore()) return false;
        for(String lore : item.getItemMeta().getLore()) {
            if(lore.equals(SOULBOUND_LORE)) {
                return true;
            }
        }
        return false;

    }

    static boolean keepOnDeath(ItemStack item) {
        if(item==null) return false;
        return isSlimefunSoulbound(item);
    }

}
