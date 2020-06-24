package de.jeff_media.AngelChest;

import java.io.File;
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

public class Main extends JavaPlugin {
	
	private static final int updateCheckInterval = 86400;
	int currentConfigVersion = 17;
	boolean usingMatchingConfig = true;
	HashMap<Player,PlayerSetting> playerSettings;
	HashMap<Block,AngelChest> angelChests;
	ArrayList<BlockArmorStandCombination> blockArmorStandCombinations;
	Material chestMaterial;
	
	CommandList commandListExecutor;
	//ArrayList<UUID> armorStandUUIDs;
	
	public boolean debug = false;
	
	ArrayList<String> disabledWorlds;
	ArrayList<Material> dontSpawnOn;
	ArrayList<Material> onlySpawnIn;
	
	Messages messages;
	UpdateChecker updateChecker;
	
	
	
	@Override
	public void onEnable() {
		
		Main myself = this;
		
		ConfigUtils.createConfig(this);
		
		messages = new Messages(this);
		
		updateChecker = new UpdateChecker(this);
		
		playerSettings = new HashMap<Player,PlayerSetting>();
		angelChests = new HashMap<Block,AngelChest>();
		blockArmorStandCombinations = new ArrayList<BlockArmorStandCombination>();
		
		loadAllAngelChestsFromFile();
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
						entry.getValue().destroy();
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
		
		
		// Schedule DurationTimer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for(AngelChest ac : angelChests.values()) {
					if(ac==null) continue;
					ac.secondsLeft--;
					if(ac.secondsLeft<=0) {
						ac.destroy();
					}
				}
			}	
		}, 0, 20);
		
	}
	
	protected void loadAllAngelChestsFromFile() {
		File dir = new File(getDataFolder().getPath() + File.separator + "angelchests");
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
				getLogger().info("Loading AngelChest " + child.getName());
		      AngelChest ac = new AngelChest(child,this);
		      if(ac.success) {
				  angelChests.put(ac.block, ac);
			  } else {
				  getLogger().info("Error while loading "+child.getName()+", probably the world is not loaded yet. Will try again on next world load.");
			  }
		    }
		  }
		
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
			entry.getValue().saveToFile();
			entry.getValue().hologram.destroy();
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

}