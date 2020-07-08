package de.jeff_media.AngelChest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AngelChest {

    ItemStack[] armorInv;
    ItemStack[] storageInv;
    ItemStack[] extraInv;
    Inventory overflowInv;
    boolean success = true;
    Block block;
    UUID worldid;
    UUID owner;
    Hologram hologram;
    boolean isProtected;
    //long configDuration;
    //long taskStart;
    int secondsLeft;
    int experience = 0;
    int levels = 0;
    Main plugin;

    public AngelChest(File file, Main plugin) {
        YamlConfiguration yaml;
        try {
            yaml = loadYaml(file);
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not load legacy AngelChest file " + file.getName());
            success = false;
            return;
        }
        this.plugin = plugin;

        this.owner = UUID.fromString(yaml.getString("owner"));
        this.levels = yaml.getInt("levels", 0);
        this.isProtected = yaml.getBoolean("isProtected");
        this.secondsLeft = yaml.getInt("secondsLeft");

        // Check if this is the current save format
        int saveVersion = yaml.getInt("angelchest-saveversion", 1);
        if (saveVersion == 1) {
            try {
                this.block = yaml.getLocation("block").getBlock();
                this.worldid = block.getWorld().getUID();
            } catch (Exception ignored) {
                success = false;
            }
            if (!success) return;
        } else {
            this.worldid = UUID.fromString(yaml.getString("worldid"));
            if (plugin.getServer().getWorld(worldid) == null) {
                success = false;
                return;
            }
            this.block = plugin.getServer().getWorld(worldid).getBlockAt(yaml.getInt("x"), yaml.getInt("y"), yaml.getInt("z"));
        }

        //String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
        String inventoryName = String.format(plugin.messages.ANGELCHEST_INVENTORY_NAME, plugin.getServer().getOfflinePlayer(owner).getName());

        createChest(block,owner.toString());
        String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getOfflinePlayer(owner).getName());
        hologram = new Hologram(block, hologramText, plugin);

        // Load OverflowInv
        overflowInv = Bukkit.createInventory(null, 54, inventoryName);
        int iOverflow = 0;
        for (ItemStack is : yaml.getList("overflowInv").toArray(new ItemStack[54])) {
            if (is != null) overflowInv.setItem(iOverflow, is);
            iOverflow++;
        }

        // Load ArmorInv
        armorInv = new ItemStack[4];
        int iArmor = 0;
        for (ItemStack is : yaml.getList("armorInv").toArray(new ItemStack[4])) {
            if (is != null) armorInv[iArmor] = is;
            iArmor++;
        }

        // Load StorageInv
        storageInv = new ItemStack[36];
        int iStorage = 0;
        for (ItemStack is : yaml.getList("storageInv").toArray(new ItemStack[36])) {
            if (is != null) storageInv[iStorage] = is;
            iStorage++;
        }

        // Load ExtraInv
        extraInv = new ItemStack[1];
        int iExtra = 0;
        for (ItemStack is : yaml.getList("extraInv").toArray(new ItemStack[1])) {
            if (is != null) extraInv[iExtra] = is;
            iExtra++;
        }

        file.delete();
    }

    public AngelChest(Player p, Block block, Main plugin) {
    	this(p, p.getUniqueId(), block, p.getInventory(), plugin);
	}


    public AngelChest(Player p, UUID owner, Block block, PlayerInventory playerItems, Main plugin) {

        this.plugin = plugin;
        this.owner = owner;
        this.block = block;
        this.isProtected = plugin.getServer().getPlayer(owner).hasPermission("angelchest.protect");
        this.secondsLeft = plugin.groupUtils.getDurationPerPlayer(plugin.getServer().getPlayer(owner));

        String inventoryName = String.format(plugin.messages.ANGELCHEST_INVENTORY_NAME, plugin.getServer().getPlayer(owner).getName());
        overflowInv = Bukkit.createInventory(null, 54, inventoryName);
        createChest(block,p.getUniqueId().toString());
        createHologram(block, plugin);

        // Remove curse of vanishing equipment and Minepacks backpacks
        for (ItemStack i : playerItems.getContents()) {
            if (!Utils.isEmpty(i)) {
                if (i.getEnchantments().containsKey(Enchantment.VANISHING_CURSE) || MinepacksHook.isMinepacksBackpack(i, plugin)) {
                    playerItems.remove(i);
                }
            }
        }

        armorInv = playerItems.getArmorContents();
        storageInv = playerItems.getStorageContents();
        extraInv = playerItems.getExtraContents();

    }

    private YamlConfiguration loadYaml(File file) throws Throwable {
        return YamlConfiguration.loadConfiguration(file);
    }



    private void createChest(Block block, String uuid) {
        Material mat = plugin.chestMaterial;
        block.setType(plugin.chestMaterial);
        if(plugin.chestMaterial==Material.PLAYER_HEAD) {
            Skull state = (Skull) block.getState();
            state.setOwningPlayer(plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)));
            state.update();
        }

    }

    public void unlock() {
        this.isProtected = false;
    }

    public void saveToFile() {
        File yamlFile = new File(plugin.getDataFolder() + File.separator + "angelchests",
                this.hashCode() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
        yaml.set("angelchest-saveversion", 2);
        yaml.set("armorInv", armorInv);
        yaml.set("storageInv", storageInv);
        yaml.set("extraInv", extraInv);
        yaml.set("overflowInv", overflowInv.getContents());
        yaml.set("worldid", block.getLocation().getWorld().getUID().toString());
        //yaml.set("block", block.getLocation());
        yaml.set("x", block.getX());
        yaml.set("y", block.getY());
        yaml.set("z", block.getZ());
        yaml.set("owner", owner.toString());
        yaml.set("isProtected", isProtected);
        //yaml.set("configDuration", configDuration);
        //yaml.set("taskStart", taskStart);
        yaml.set("secondsLeft", secondsLeft);
        yaml.set("experience", experience);
        yaml.set("levels", levels);

        // Duplicate Start
        block.setType(Material.AIR);
        for (UUID uuid : hologram.armorStandUUIDs) {
            if (plugin.getServer().getEntity(uuid) != null) {
                plugin.getServer().getEntity(uuid).remove();
            }
        }
        for (ArmorStand armorStand : hologram.armorStands) {
            if (armorStand == null) continue;
            armorStand.remove();
        }
        if (hologram != null) hologram.destroy();
        // Duplicate End
        try {
            yaml.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (!plugin.isAngelChest(block))
            return;

        block.setType(Material.AIR);
        destroyHologram(plugin);

        // drop contents
        Utils.dropItems(block, armorInv);
        Utils.dropItems(block, storageInv);
        Utils.dropItems(block, extraInv);
        Utils.dropItems(block, overflowInv);

        if (experience > 0) {
            Utils.dropExp(block, experience);
        }


        block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
    }

    void remove() {
        plugin.angelChests.remove(block);
    }
	
	/*public long secondsRemaining() {
		long seconds = configDuration - ((System.currentTimeMillis() - taskStart) / 1000);
		if(seconds<0) seconds = 0;
		return seconds;
    }*/
    
	public void createHologram(Block block, Main plugin) {
		String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
		hologram = new Hologram(block, hologramText, plugin);
	}

	public void destroyHologram(Main plugin) {
        for (UUID uuid : hologram.armorStandUUIDs) {
            if (plugin.getServer().getEntity(uuid) != null) {
                plugin.getServer().getEntity(uuid).remove();
            }
        }
        for (ArmorStand armorStand : hologram.armorStands) {
            if (armorStand == null) continue;
            armorStand.remove();
        }
        if (hologram != null) hologram.destroy();
	}
}