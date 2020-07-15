package de.jeff_media.AngelChest;

import com.sk89q.worldedit.bukkit.BukkitAdapter;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class WorldGuardHandler {

    Main main;
    WorldGuardPlugin wg;
    RegionContainer container;

    WorldGuardHandler(Main main) {
        this.main=main;
        wg = WorldGuardPlugin.inst();
        if(wg != null) {
            main.getLogger().info("Successfully hooked into WorldGuard");
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        }
    }

    boolean isBlacklisted(Block block) {
        System.out.println("CHECKING IF THIS BLOCK IS IN PROTECTED REGION");
        if(wg==null) return false;
        if(main.disabledRegions==null || main.disabledRegions.size()==0) return false;
        /*com.sk89q.worldedit.util.Location wloc = BukkitAdapter.adapt(loc);
        com.sk89q.worldedit.world.World wworld = BukkitAdapter.adapt(loc.getWorld());*/
        RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));
        List<String> regionList = regions.getApplicableRegionsIDs(getBlockVector3(block));

        for(String r : regionList) {
            main.debug("Player died in region " +r);
            if(main.disabledRegions.contains(r)) {
                main.debug("Preventing AngelChest from spawning in disabled worldguard region");
                return true;
            }
        }
        return false;
    }

    BlockVector3 getBlockVector3(Block block) {
        return BlockVector3.at(block.getX(),block.getY(),block.getZ());
    }
}
