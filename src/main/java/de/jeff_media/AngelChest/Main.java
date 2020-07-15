package de.jeff_media.AngelChest;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	int currentConfigVersion = 27;
	boolean usingMatchingConfig = true;
	HashMap<Player,PlayerSetting> playerSettings;
	LinkedHashMap<Block,AngelChest> angelChests;
	ArrayList<BlockArmorStandCombination> blockArmorStandCombinations;
	Material chestMaterial;
	
	CommandList commandListExecutor;

	public boolean debug = false;
	
	ArrayList<String> disabledWorlds;
	ArrayList<Material> dontSpawnOn;
	ArrayList<Material> onlySpawnIn;
	
	Messages messages;
	PluginUpdateChecker updateChecker;
	GroupUtils groupUtils;
	
	void debug(String t) {
		if(debug) getLogger().info(t);
	}
	
	@Override
	public void onEnable() {
		
		//Main myself = this;

		ConfigUtils.reloadCompleteConfig(this,false);

		playerSettings = new HashMap<Player,PlayerSetting>();
		angelChests = new LinkedHashMap<Block,AngelChest>();
		blockArmorStandCombinations = new ArrayList<BlockArmorStandCombination>();

		debug("Loading AngelChests from disk");
		loadAllAngelChestsFromFile();
		//armorStandUUIDs = new ArrayList<UUID>();


		
		// Deletes old armorstands and restores broken AngelChests (only case where I currently know this happens is when a endcrystal spanws in a chest)
		Main plugin = this;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {

				//getLogger().info(blockArmorStandCombinations.size()+"");
				for(BlockArmorStandCombination comb : blockArmorStandCombinations.toArray(new BlockArmorStandCombination[blockArmorStandCombinations.size()])) {
					if(!isAngelChest(comb.block)) {
						comb.armorStand.remove();
						blockArmorStandCombinations.remove(comb);
						debug("Removing BlockArmorStandCombination because of missing AngelChest");
						//getLogger().info("Removed armor stand that has been left behind at @ " + comb.block.getLocation().toString());
					}
				}
				for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
					/*if(!isAngelChest(entry.getKey())) {
						entry.getValue().destroy();
						debug("Removing block from list because it's no AngelChest");
					}*/
					if(isBrokenAngelChest(entry.getKey())) {
						Block block = entry.getKey();
						debug("Fixing broken AngelChest at "+block.getLocation());
						entry.setValue(new AngelChest(getAngelChest(block).saveToFile(),plugin));
					}
				}
			}
		}, 0L, 2 * 20);
		
		debug("Registering commands");
		this.getCommand("unlock").setExecutor(new CommandUnlock(this));
		this.getCommand("aclist").setExecutor(new CommandList(this));
		this.getCommand("acfetch").setExecutor(new CommandFetch(this));
		this.getCommand("actp").setExecutor(new CommandTeleportTo(this));
		this.getCommand("acreload").setExecutor(new CommandReload(this));

		debug("Registering listeners");
		getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
		getServer().getPluginManager().registerEvents(new HologramListener(this),this);
		getServer().getPluginManager().registerEvents(new BlockListener(this),this);
		
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);
		

		if (debug) getLogger().info("Disabled Worlds: "+disabledWorlds.size());
		
		
		// Schedule DurationTimer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				Iterator<AngelChest> it = angelChests.values().iterator();
				while(it.hasNext()) {
					AngelChest ac = it.next();
					if(ac==null) continue;
					ac.secondsLeft--;
					if(ac.secondsLeft<=0) {
						if(getServer().getPlayer(ac.owner)!=null) {
							getServer().getPlayer(ac.owner).sendMessage(messages.MSG_ANGELCHEST_DISAPPEARED);
						}
						ac.destroy();
						it.remove();
					}
				}
			}	
		}, 0, 20);
		
	}
	
	void loadAllAngelChestsFromFile() {
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
		saveAllAngelChestsToFile();

	}

	void saveAllAngelChestsToFile() {
		// Destroy all Angel Chests, including hologram AND CONTENTS!
		Iterator<Entry<Block, AngelChest>> it = angelChests.entrySet().iterator();
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
		//if(block.getType() != chestMaterial) return false;
		return angelChests.containsKey(block);
	}

	public boolean isBrokenAngelChest(Block block) {
		return block.getType() != chestMaterial;
	}
	
	public boolean isAngelChestHologram(Entity e) {
		// Skip this because it is checked in the listener and this method is not needed elsewhere
		//if(!(e instanceof ArmorStand)) return false;

		return getAllArmorStands().contains(e);
	}
	
	public AngelChest getAngelChest(Block block) {
		debug("Getting AngelChest for block "+block.getLocation().toString());
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
