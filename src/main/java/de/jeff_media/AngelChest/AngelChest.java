package de.jeff_media.AngelChest;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        plugin.debug("Creating AngelChest from file " + file.getName());
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

        if(!block.getWorld().isChunkLoaded(block.getX(),block.getZ())) {
            plugin.debug("Chunk is not loaded, trying to load chunk async...");
            PaperLib.getChunkAtAsync(block.getLocation());
            if(!block.getWorld().isChunkLoaded(block.getX(),block.getZ())) {
                plugin.debug("The chunk is still unloaded... Trying to load chunk synced...");
                block.getChunk().load();
                if(!block.getWorld().isChunkLoaded(block.getX(),block.getZ())) {
                    plugin.debug("The chunk is still unloaded... creating the chest will probably fail.");
                }
            }
        }

        createChest(block,owner);

        // Load OverflowInv
        AngelChestHolder holder = new AngelChestHolder();
        overflowInv = Bukkit.createInventory(holder, 54, inventoryName);
        holder.setInventory(overflowInv);
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

        plugin.debug("Creating AngelChest natively for player "+p.getName());

        this.plugin = plugin;
        this.owner = owner;
        this.block = block;
        this.isProtected = plugin.getServer().getPlayer(owner).hasPermission("angelchest.protect");
        this.secondsLeft = plugin.groupUtils.getDurationPerPlayer(plugin.getServer().getPlayer(owner));

        String inventoryName = String.format(plugin.messages.ANGELCHEST_INVENTORY_NAME, plugin.getServer().getPlayer(owner).getName());
        overflowInv = Bukkit.createInventory(null, 54, inventoryName);
        createChest(block,p.getUniqueId());

        // Remove curse of vanishing equipment and Minepacks backpacks
        for (int i = 0; i<playerItems.getSize();i++) {
            if (Utils.isEmpty(playerItems.getItem(i))) {
                continue;
            }

            if(toBeRemoved(playerItems.getItem(i))) playerItems.setItem(i,null);
        }

        armorInv = playerItems.getArmorContents();
        storageInv = playerItems.getStorageContents();
        extraInv = playerItems.getExtraContents();

        removeKeepedItems();

    }


    private void removeKeepedItems() {

        for(int i = 0; i <armorInv.length;i++) {
            if(plugin.hookUtils.keepOnDeath(armorInv[i])
                    || plugin.hookUtils.removeOnDeath(armorInv[i])) {
                armorInv[i]=null;
            }
        }
        for(int i = 0; i <storageInv.length;i++) {
            if(plugin.hookUtils.keepOnDeath(storageInv[i])
                    || plugin.hookUtils.removeOnDeath(storageInv[i])) {
                storageInv[i]=null;
            }
        }for(int i = 0; i <extraInv.length;i++) {
            if(plugin.hookUtils.keepOnDeath(extraInv[i])
                    || plugin.hookUtils.removeOnDeath(extraInv[i])) {
                extraInv[i]=null;
            }
        }
    }

    private boolean toBeRemoved(ItemStack i) {
        if(i==null) return false;
        if(plugin.getConfig().getBoolean("remove-curse-of-vanishing")
                && i.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
            return true;
        }
        if(plugin.getConfig().getBoolean("remove-curse-of-binding")
                && i.getEnchantments().containsKey(Enchantment.BINDING_CURSE)) {
            return true;
        }
        if (MinepacksHook.isMinepacksBackpack(i, plugin)) {
            return true;
        }


        //if(m.hasLore() && m.getLore().contains)


        return false;
    }

    private YamlConfiguration loadYaml(File file) throws Throwable {
        return YamlConfiguration.loadConfiguration(file);
    }

    // Creates a physcial chest
    protected void createChest(Block block, UUID uuid) {
        plugin.debug("Attempting to create chest with material " + plugin.chestMaterial.name() + " at "+block.getLocation().toString());
        block.setType(plugin.chestMaterial);
        if(plugin.chestMaterial.name().equalsIgnoreCase("PLAYER_HEAD")) {
            if(Material.getMaterial("PLAYER_HEAD") == null) {
                plugin.getLogger().warning("Using a custom PLAYER_HEAD as chest material is NOT SUPPORTED in versions < 1.13. Consider using another chest material.");
            } else {
                Skull state = (Skull) block.getState();
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                String skullName = plugin.getConfig().getString("custom-head");

                if(plugin.getConfig().getBoolean("head-uses-player-name")) {
                    plugin.debug("Player head = username");
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
                    state.setOwningPlayer(player);
                    state.update();
                } else {
                    plugin.debug("Player head = base64");
                    String base64 = plugin.getConfig().getString("custom-head-base64");
                    GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                    profile.getProperties().put("textures", new Property("textures", base64));

                    Field profileField = null;
                    try {
                        /*profileField = state.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                        profileField.set(state, profile);*/

                        // Some reflection because Spigot cannot place ItemStacks in the world, which ne need to keep the SkullMeta

                        Object nmsWorld = block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());

                        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                        Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
                        Class<?> tileEntityClass = Class.forName("net.minecraft.server." + version + ".TileEntitySkull");


                        Constructor<?> cons = blockPositionClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
                        Object blockPosition = cons.newInstance(block.getX(), block.getY(), block.getZ());

                        Method getTileEntity = nmsWorld.getClass().getMethod("getTileEntity", blockPositionClass);
                        Object tileEntity = tileEntityClass.cast(getTileEntity.invoke(nmsWorld, blockPosition));

                        tileEntityClass.getMethod("setGameProfile", GameProfile.class).invoke(tileEntity, profile);

                    } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
                        plugin.getLogger().warning("Could not set custom base64 player head.");
                    }

                }

                //state.set);
                //state.update();

            }
        }
        createHologram(plugin, block, uuid);
    }

    // Destroys a physical chest
    protected void destroyChest(Block b) {
        plugin.debug("Destroying chest at "+b.getLocation()+toString());
        b.setType(Material.AIR);
        b.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, b.getLocation(), 1);
        destroyHologram(plugin);
    }

    public void unlock() {
        this.isProtected = false;
    }

    public File saveToFile() {
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
        return yamlFile;
    }

    public void destroy() {
        plugin.debug("Destroying AngelChest");

        if(!block.getWorld().isChunkLoaded(block.getX(),block.getZ())) {
            plugin.debug("Chunk is not loaded, trying to load chunk async...");
            PaperLib.getChunkAtAsync(block.getLocation());
            if(!block.getWorld().isChunkLoaded(block.getX(),block.getZ())) {
                plugin.debug("The chunk is still unloaded... Trying to load chunk synced...");
                block.getChunk().load();
                if(!block.getWorld().isChunkLoaded(block.getX(),block.getZ())) {
                    plugin.debug("The chunk is still unloaded... destroying the chest will probably fail.");
                }
            }
        }


        if (!plugin.isAngelChest(block))
            return;

        // remove the physical chest
        destroyChest(block);

        // drop contents
        Utils.dropItems(block, armorInv);
        Utils.dropItems(block, storageInv);
        Utils.dropItems(block, extraInv);
        Utils.dropItems(block, overflowInv);

        if (experience > 0) {
            Utils.dropExp(block, experience);
        }
    }

    void remove() {
        plugin.debug("Removing AngelChest");
        plugin.angelChests.remove(block);
    }
    
	public void createHologram(Main plugin, Block block, UUID uuid) {
		String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getOfflinePlayer(uuid).getName());
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