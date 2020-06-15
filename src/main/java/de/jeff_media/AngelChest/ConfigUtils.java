package de.jeff_media.AngelChest;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Material;

public class ConfigUtils {
	
	static void createDirectories(Main main) {

		File categoriesFolder = new File(main.getDataFolder().getPath() + File.separator + "angelchests");
		if (!categoriesFolder.getAbsoluteFile().exists()) {
			categoriesFolder.mkdir();
		}
	}
	
	static void createConfig(Main main) {
		main.saveDefaultConfig();
		createDirectories(main);
		
		main.getConfig().addDefault("check-for-updates", "true");
		main.getConfig().addDefault("show-location", true);
		main.getConfig().addDefault("angelchest-duration", 600);
		main.getConfig().addDefault("max-radius", 10);
		main.getConfig().addDefault("material", "CHEST");
		main.getConfig().addDefault("preserve-xp", true);
		main.getConfig().addDefault("full-xp", false); // Currently not in config because there is no way to get players XP
		main.disabledWorlds = (ArrayList<String>) main.getConfig().getStringList("disabled-worlds");
		
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
		
		
		if (main.getConfig().getInt("config-version", 0) != main.currentConfigVersion) {
			showOldConfigWarning(main);
			ConfigUpdater configUpdater = new ConfigUpdater(main);
			configUpdater.updateConfig();
			configUpdater = null;
			main.usingMatchingConfig = true;
			//createConfig();
		}
		
		if(Material.getMaterial(main.getConfig().getString("material"))==null) {
			main.getLogger().warning("Invalid Material: "+main.getConfig().getString("material")+" - falling back to CHEST");
			main.chestMaterial = Material.CHEST;
		} else {
			main.chestMaterial = Material.getMaterial(main.getConfig().getString("material"));
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
