package de.jeff_media.AngelChest;

import java.io.File;
import java.util.ArrayList;

import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bukkit.Material;

public class ConfigUtils {
	
	static void createDirectories(Main main) {

		File categoriesFolder = new File(main.getDataFolder().getPath() + File.separator + "angelchests");
		if (!categoriesFolder.getAbsoluteFile().exists()) {
			categoriesFolder.mkdir();
		}
	}

	static void reloadCompleteConfig(Main main,boolean reload) {
		if(reload) {
			main.saveAllAngelChestsToFile();
		}
		main.reloadConfig();
		createConfig(main);
		if(main.updateChecker != null) {
			main.updateChecker.stop();
		}
		initUpdateChecker(main);
		main.debug = main.getConfig().getBoolean("debug",false);
		main.messages = new Messages(main);
		File groupsFile = new File(main.getDataFolder()+File.separator+"groups.yml");
		main.groupUtils = new GroupUtils(main,groupsFile);
		main.worldGuardHandler = new WorldGuardHandler(main);
		if(reload) {
			main.loadAllAngelChestsFromFile();
		}

	}

	static void initUpdateChecker(Main main) {
		main.updateChecker = new PluginUpdateChecker(main,"https://api.jeff-media.de/angelchest/angelchest-latest-version.txt","https://www.spigotmc.org/resources/1-12-1-15-angelchest.60383/","https://github.com/JEFF-Media-GbR/Spigot-AngelChest/blob/master/CHANGELOG.md","https://www.paypal.me/mfnalex");

		if (main.getConfig().getString("check-for-updates", "true").equalsIgnoreCase("true")) {
			main.updateChecker.check((long) (main.getConfig().getDouble("check-interval")*60*60));
		} else if (main.getConfig().getString("check-for-updates", "true").equalsIgnoreCase("on-startup")) {
			main.updateChecker.check();
		}

	}
	
	static void createConfig(Main main) {
		main.saveDefaultConfig();
		main.saveResource("groups.example.yml", true);
		createDirectories(main);

		main.getConfig().addDefault("check-for-updates", "true");
		main.getConfig().addDefault("check-interval",4);
		main.getConfig().addDefault("totem-of-undying-works-everywhere",true);
		main.getConfig().addDefault("show-location", true);
		main.getConfig().addDefault("angelchest-duration", 600);
		main.getConfig().addDefault("max-radius", 10);
		main.getConfig().addDefault("material", "CHEST");
		main.getConfig().addDefault("player-head","{PLAYER}");
		main.getConfig().addDefault("preserve-xp", true);
		main.getConfig().addDefault("remove-curse-of-vanishing",true);
		main.getConfig().addDefault("remove-curse-of-binding",true);
		main.getConfig().addDefault("only-spawn-chests-if-player-may-build",false);
		main.getConfig().addDefault("disable-worldguard-integration",false);
		main.getConfig().addDefault("ignore-keep-inventory",false);
		main.getConfig().addDefault("head-uses-player-name",true);
		main.getConfig().addDefault("full-xp", false); // Currently not in config because there is no way to get players XP
		main.disabledWorlds = (ArrayList<String>) main.getConfig().getStringList("disabled-worlds");
		main.disabledRegions = (ArrayList<String>) main.getConfig().getStringList("disabled-worldguard-regions");
		
		ArrayList<String> dontSpawnOnTmp = (ArrayList<String>) main.getConfig().getStringList("dont-spawn-on");
		main.dontSpawnOn = new ArrayList<Material>();
		
		ArrayList<String> onlySpawnInTmp = (ArrayList<String>) main.getConfig().getStringList("only-spawn-in");
		main.onlySpawnIn = new ArrayList<Material>();
		
		for(String string : dontSpawnOnTmp) {
			Material mat = Material.getMaterial(string.toUpperCase());
			if(mat==null) {			
				main.getLogger().warning(String.format("Invalid Material while parsing %s: %s", string,"dont-spawn-on"));
				continue;
			}
			if(!mat.isBlock()) {
				main.getLogger().warning(String.format("Invalid Block while parsing %s: %s", string, "dont-spawn-on"));
				continue;
			}
			//System.out.println(mat.name() + " added to blacklist");
			main.dontSpawnOn.add(mat);
		}
		dontSpawnOnTmp=null;

		if(false) return;
		
		for(String string : onlySpawnInTmp) {
			Material mat = Material.getMaterial(string.toUpperCase());
			if(mat==null) {
				main.getLogger().warning(String.format("Invalid Material while parsing %s: %s", string,"only-spawn-in"));
				continue;
			}
			if(!mat.isBlock()) {
				main.getLogger().warning(String.format("Invalid Block while parsing %s: %s", string, "only-spawn-in"));
				continue;
			}
			//System.out.println(mat.name() + " added to whitelist");
			main.onlySpawnIn.add(mat);
		}
		onlySpawnInTmp=null;
		
		main.debug("Latest config version: "+main.currentConfigVersion);
		main.debug("Your config version: "+main.getConfig().getInt("config-version"));
		if (main.getConfig().getInt("config-version", 0) != main.currentConfigVersion) {
			//System.out.println("ANGELCHEST DEBUG: " + main.getConfig().getInt("config-version",0)+" / "+main.currentConfigVersion);
			showOldConfigWarning(main);
			ConfigUpdater configUpdater = new ConfigUpdater(main);
			configUpdater.updateConfig();
			configUpdater = null;
			main.usingMatchingConfig = true;
			//createConfig();
		}

		
		if(Material.getMaterial(main.getConfig().getString("material").toUpperCase())==null) {
			main.getLogger().warning("Invalid Material: "+main.getConfig().getString("material")+" - falling back to CHEST");
			main.chestMaterial = Material.CHEST;
		} else {
			main.chestMaterial = Material.getMaterial(main.getConfig().getString("material").toUpperCase());
			if(!main.chestMaterial.isBlock()) {
				main.getLogger().warning("Not a block: "+main.getConfig().getString("material")+" - falling back to CHEST");
				main.chestMaterial = Material.CHEST;
			}
		}

	}
	
	private static void showOldConfigWarning(Main main) {
		main.getLogger().warning("==============================================");
		main.getLogger().warning("You were using an old config file. AngelChest");
		main.getLogger().warning("has updated the file to the newest version.");
		main.getLogger().warning("Your changes have been kept.");
		main.getLogger().warning("==============================================");
	}

}
