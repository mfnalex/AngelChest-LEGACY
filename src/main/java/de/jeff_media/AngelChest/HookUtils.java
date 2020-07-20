package de.jeff_media.AngelChest;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.inventory.ItemStack;

public class HookUtils {

    final Main main;

    HookUtils(Main main) {
        this.main=main;
    }

    boolean isSlimefunSoulbound(ItemStack item) {
        if(item==null) return false;
        if(!main.getConfig().getBoolean("use-slimefun")) return false;

        try {
            Class.forName("io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils");
            return SlimefunUtils.isSoulbound(item);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            main.getConfig().set("use-slimefun",false);
            return false;
        }
    }

    boolean keepOnDeath(ItemStack item) {
        if(item==null) return false;
        return isSlimefunSoulbound(item);
    }

}