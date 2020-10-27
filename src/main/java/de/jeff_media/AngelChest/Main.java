package de.jeff_media.AngelChest;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import de.jeff_media.AngelChest.hooks.MinepacksHook;
import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	final int currentConfigVersion = 46;

	boolean usingMatchingConfig = true;
	HashMap<UUID,PendingConfirm> pendingConfirms;
	HashMap<Player,PlayerSetting> playerSettings;
	LinkedHashMap<Block,AngelChest> angelChests;
	ArrayList<BlockArmorStandCombination> blockArmorStandCombinations;
	Material chestMaterial;
	
	CommandList commandListExecutor;

	public boolean debug = false;

	List<String> disabledMaterials;
	List<String> disabledWorlds;
	List<String> disabledRegions;
	List<Material> dontSpawnOn;
	List<Material> onlySpawnIn;
	
	Messages messages;
	PluginUpdateChecker updateChecker;
	GroupUtils groupUtils;
	WorldGuardHandler worldGuardHandler;
	HookUtils hookUtils;
	MinepacksHook minepacksHook;
	AngelChestDebugger debugger;
	
	void debug(String t) {
		if(debug) getLogger().info("[DEBUG] " + t);
	}
	
	@Override
	public void onEnable() {

		//Main myself = this;


		ConfigUtils.reloadCompleteConfig(this,false);

		playerSettings = new HashMap<>();
		angelChests = new LinkedHashMap<>();
		blockArmorStandCombinations = new ArrayList<>();

		debug("Loading AngelChests from disk");
		loadAllAngelChestsFromFile();
		//armorStandUUIDs = new ArrayList<UUID>();


		
		// Deletes old armorstands and restores broken AngelChests (only case where I currently know this happens is when a endcrystal spanws in a chest)
		Main plugin = this;

		// Rename holograms
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for(AngelChest chest : angelChests.values()) {
				chest.hologram.update(chest);
			}
		},20l,20l);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

			//getLogger().info(blockArmorStandCombinations.size()+"");
			for(BlockArmorStandCombination comb : blockArmorStandCombinations.toArray(new BlockArmorStandCombination[0])) {

				if(!PaperLib.isChunkGenerated(comb.block.getLocation())) {
					debug("Chunk at "+comb.block.getLocation().toString()+" has not been generated!");
				}

				if(!comb.block.getWorld().isChunkLoaded(comb.block.getX() >> 4,comb.block.getZ() >> 4)) {

					debug("Chunk at "+comb.block.getLocation().toString() + " is not loaded, skipping repeating task regarding BlockArmorstandCombination");
					// CONTINUE IF CHUNK IS NOT LOADED

					continue;
				}
				if(!isAngelChest(comb.block)) {
					comb.armorStand.remove();
					blockArmorStandCombinations.remove(comb);
					debug("Removing BlockArmorStandCombination because of missing AngelChest");
					//getLogger().info("Removed armor stand that has been left behind at @ " + comb.block.getLocation().toString());
				}
			}

			// The following might only be needed for chests destroyed by end crystals spawning during the init phase of the ender dragon
			for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {

				if(!PaperLib.isChunkGenerated(entry.getKey().getLocation())) {
					debug("Chunk at "+entry.getKey().getLocation().toString()+" has not been generated!");
				}

				if(!entry.getKey().getWorld().isChunkLoaded(entry.getKey().getX() >> 4,entry.getKey().getZ() >> 4)) {

					debug("Chunk at "+entry.getKey().getLocation().toString() + " is not loaded, skipping repeating task regarding angelChests.entrySet()");
					// CONTINUE IF CHUNK IS NOT LOADED

					continue;
				}
				/*if(!isAngelChest(entry.getKey())) {
					entry.getValue().destroy();
					debug("Removing block from list because it's no AngelChest");
				}*/
				if(isBrokenAngelChest(entry.getKey())) {
					// TODO: Disabled for now, but left behind if someone still has missing chests upon end crystal generation
					Block block = entry.getKey();
					debug("Fixing broken AngelChest at "+block.getLocation());
					entry.setValue(new AngelChest(getAngelChest(block).saveToFile(),plugin));
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
		Metrics metrics = new Metrics(this,3194);
		metrics.addCustomChart(new Metrics.SimplePie("material", () -> chestMaterial.name()));
		metrics.addCustomChart(new Metrics.SimplePie("auto_respawn", () -> getConfig().getBoolean("auto-respawn")+""));
		metrics.addCustomChart(new Metrics.SimplePie("totem_works_everywhere", () -> getConfig().getBoolean("totem-of-undying-works-everywhere")+""));
		

		if (debug) getLogger().info("Disabled Worlds: "+disabledWorlds.size());
		if (debug) getLogger().info("Disabled WorldGuard regions: "+disabledRegions.size());
		
		
		// Schedule DurationTimer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			Iterator<AngelChest> it = angelChests.values().iterator();
			while(it.hasNext()) {
				AngelChest ac = it.next();
				if(ac==null) continue;
				ac.secondsLeft--;
				if(ac.secondsLeft<=0 && !ac.infinite) {
					if(getServer().getPlayer(ac.owner)!=null) {
						getServer().getPlayer(ac.owner).sendMessage(messages.MSG_ANGELCHEST_DISAPPEARED);
					}
					ac.destroy();
					it.remove();
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
		//for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
		//	Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
		//}
		for (Entry<Block, AngelChest> entry : angelChests.entrySet()) {
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
		ArrayList<ArmorStand> armorStands = new ArrayList<>();
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
