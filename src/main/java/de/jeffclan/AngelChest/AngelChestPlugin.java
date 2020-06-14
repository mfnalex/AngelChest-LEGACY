package de.jeffclan.AngelChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AngelChestPlugin extends JavaPlugin {
	
	private static final int updateCheckInterval = 86400;
	int currentConfigVersion = 13;
	boolean usingMatchingConfig = true;
	HashMap<Player,PlayerSetting> playerSettings;
	HashMap<Block,AngelChest> angelChests;
	ArrayList<BlockArmorStandCombination> blockArmorStandCombinations;
	Material chestMaterial;
	
	CommandList commandListExecutor;
	//ArrayList<UUID> armorStandUUIDs;
	
	public boolean debug = false;
	
	ArrayList<String> disabledWorlds;
	
	Messages messages;
	UpdateChecker updateChecker;
	
	
	
	@Override
	public void onEnable() {
		
		AngelChestPlugin myself = this;
		
		createConfig();
		
		messages = new Messages(this);
		updateChecker = new UpdateChecker(this);
		
		playerSettings = new HashMap<Player,PlayerSetting>();
		angelChests = new HashMap<Block,AngelChest>();
		blockArmorStandCombinations = new ArrayList<BlockArmorStandCombination>();
		//armorStandUUIDs = new ArrayList<UUID>();
		
		// Deletes old armorstands
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				//getLogger().info(blockArmorStandCombinations.size()+"");
				for(BlockArmorStandCombination comb : blockArmorStandCombinations.toArray(new BlockArmorStandCombination[blockArmorStandCombinations.size()])) {
					if(!isAngelChest(comb.block)) {
						comb.armorStand.remove();
						blockArmorStandCombinations.remove(comb);
						//getLogger().info("Removed armor stand that has been left behind at @ " + comb.block.getLocation().toString());
					}
				}
				for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
					if(!isAngelChest(entry.getKey())) {
						Utils.destroyAngelChest(entry.getKey(), entry.getValue(), myself);
					}
				}
			}
		}, 0L, 1 * 20);
		
		
		this.getCommand("unlock").setExecutor(new CommandUnlock(this));
		commandListExecutor = new CommandList(this);
		this.getCommand("aclist").setExecutor(commandListExecutor);
		
		getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
		getServer().getPluginManager().registerEvents(new HologramListener(this),this);
		getServer().getPluginManager().registerEvents(new BlockListener(this),this);
		
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);
		
		if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("true")) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					updateChecker.checkForUpdate();
				}
			}, 0L, updateCheckInterval * 20);
		} else if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("on-startup")) {
			updateChecker.checkForUpdate();
		}
		
		if (debug) getLogger().info("Disabled Worlds: "+disabledWorlds.size());
		
	}
	
	public void onDisable() {
		
		for(Player p : getServer().getOnlinePlayers()) {
			unregisterPlayer(p);
		}
		
		// Destroy all Angel Chests, including hologram AND CONTENTS!
		Iterator<Entry<Block,AngelChest>> it = angelChests.entrySet().iterator();
		//for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
		//	Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
		//}
		while(it.hasNext()) {
			Entry<Block,AngelChest> entry = (Entry<Block,AngelChest>) it.next();
			Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
		}
	}
	
	public void createConfig() {
		saveDefaultConfig();
		
		getConfig().addDefault("check-for-updates", "true");
		getConfig().addDefault("show-location", true);
		getConfig().addDefault("angelchest-duration", 600);
		getConfig().addDefault("max-radius", 10);
		getConfig().addDefault("material", "CHEST");
		disabledWorlds = (ArrayList<String>) getConfig().getStringList("disabled-worlds");
		
		
		if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
			showOldConfigWarning();
			ConfigUpdater configUpdater = new ConfigUpdater(this);
			configUpdater.updateConfig();
			configUpdater = null;
			usingMatchingConfig = true;
			//createConfig();
		}
		
		if(Material.getMaterial(getConfig().getString("material"))==null) {
			getLogger().warning("Invalid Material: "+getConfig().getString("material")+" - falling back to CHEST");
			chestMaterial = Material.CHEST;
		} else {
			chestMaterial = Material.getMaterial(getConfig().getString("material"));
			if(!chestMaterial.isBlock()) {
				getLogger().warning("Not a block: "+getConfig().getString("material")+" - falling back to CHEST");
				chestMaterial = Material.CHEST;
			}
		}
	}
	
	public PlayerSetting getPlayerSettings(Player p) {
		registerPlayer(p);
		return playerSettings.get(p);
	}
	
	public void registerPlayer(Player p) {
		if(!isPlayerRegistered(p)) {
			playerSettings.put(p, new PlayerSetting());
			// Load default values or saved values here
		}
	}
	
	public void unregisterPlayer(Player p) {
		if(isPlayerRegistered(p)) {
			//PlayerSetting toBeSaved = playerSettings.get(p);
			playerSettings.remove(p);
			// Save stuff to file
		}
	}
	
	public boolean isPlayerRegistered(Player p) {
		return playerSettings.containsKey(p);
	}

	public boolean isAngelChest(Block block) {
		if(block.getType() != chestMaterial) return false;
		return angelChests.containsKey(block);
	}
	
	public boolean isAngelChestHologram(Entity e) {
		// Skip this because it is checked in the listener and this method is not needed elsewhere
		//if(!(e instanceof ArmorStand)) return false;
		
		if(getAllArmorStands().contains(e)) return true;
		return false;
	}
	
	public AngelChest getAngelChest(Block block) {
		if(angelChests.containsKey(block)) {
			return angelChests.get(block);
		}
		return null;
	}
	
	public AngelChest getAngelChestByHologram(ArmorStand armorStand) {
		for(AngelChest as : angelChests.values()) {
			if( as == null) continue;
			if(as.hologram == null) continue;
			if(as.hologram.armorStands.contains(armorStand)) {
				return as;
			}
		}
		return null;
	}
	
	ArrayList<ArmorStand> getAllArmorStands() {
		ArrayList<ArmorStand> armorStands = new ArrayList<ArmorStand>();
		for(AngelChest ac : angelChests.values()) {
			if(ac==null || ac.hologram==null) continue;
			for(ArmorStand armorStand : ac.hologram.armorStands) {
				if(armorStand == null) continue;
				armorStands.add(armorStand);
			}
		}
		return armorStands;
	}
	
	private void showOldConfigWarning() {
		getLogger().warning("==============================================");
		getLogger().warning("You were using an old config file. AngelChest");
		getLogger().warning("has updated the file to the newest version.");
		getLogger().warning("Your changes have been kept.");
		getLogger().warning("==============================================");
	}

}