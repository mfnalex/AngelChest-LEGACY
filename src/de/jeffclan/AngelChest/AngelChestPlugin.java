package de.jeffclan.AngelChest;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AngelChestPlugin extends JavaPlugin {
	
	private static final int updateCheckInterval = 86400;
	int currentConfigVersion = 6;
	boolean usingMatchingConfig = true;
	HashMap<Player,PlayerSetting> playerSettings;
	HashMap<Block,AngelChest> angelChests;
	
	Messages messages;
	UpdateChecker updateChecker;
	
	@Override
	public void onEnable() {
		
		createConfig();
		
		messages = new Messages(this);
		updateChecker = new UpdateChecker(this);
		
		playerSettings = new HashMap<Player,PlayerSetting>();
		angelChests = new HashMap<Block,AngelChest>();
		
		this.getCommand("unlock").setExecutor(new CommandUnlock(this));
		
		getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
		getServer().getPluginManager().registerEvents(new HologramListener(this),this);
		getServer().getPluginManager().registerEvents(new BlockListener(this),  this);
		
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
		
	}
	
	public void onDisable() {
		
		for(Player p : getServer().getOnlinePlayers()) {
			unregisterPlayer(p);
		}
		
		// Destroy all Angel Chests, including hologram AND CONTENTS!
		for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
			Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
		}
	}
	
	public void createConfig() {
		saveDefaultConfig();
		
		getConfig().addDefault("check-for-updates", "true");
		getConfig().addDefault("angelchest-duration", 600);
		getConfig().addDefault("max-radius", 10);
		
		if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
			getLogger().warning("========================================================");
			getLogger().warning("YOU ARE USING AN OLD CONFIG FILE!");
			getLogger().warning("This is not a problem, as AngelChest will just use the");
			getLogger().warning("default settings for unset values. However, if you want");
			getLogger().warning("to configure the new options, please go to");
			getLogger().warning("https://www.spigotmc.org/resources/1-13-angelchest.60383/");
			getLogger().warning("and replace your config.yml with the new one. You can");
			getLogger().warning("then insert your old changes into the new file.");
			getLogger().warning("========================================================");
			usingMatchingConfig = false;
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
			PlayerSetting toBeSaved = playerSettings.get(p);
			playerSettings.remove(p);
			// Save stuff to file
		}
	}
	
	public boolean isPlayerRegistered(Player p) {
		return playerSettings.containsKey(p);
	}

	public boolean isAngelChest(Block block) {
		return angelChests.containsKey(block);
	}
	
	public AngelChest getAngelChest(Block block) {
		if(angelChests.containsKey(block)) {
			return angelChests.get(block);
		}
		return null;
	}

}