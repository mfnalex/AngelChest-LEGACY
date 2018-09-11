package de.jeffclan.AngelChest;

import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Hologram {

	ArrayList<ArmorStand> armorStands;
	double lineOffset = -0.2D;
	double currentOffset = 0.0D;
	
	public Hologram(Block block, String text,AngelChestPlugin plugin) {
		this(block.getLocation().add(new Vector(0.5,-0.5,0.5)),block,text,plugin);
	}

	public Hologram(Location location, Block block, String text, AngelChestPlugin plugin) {

		armorStands = new ArrayList<ArmorStand>();

		Scanner scanner = new Scanner(text);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(new Vector(0,lineOffset,0)), EntityType.ARMOR_STAND); // Spawn the ArmorStand

			as.setGravity(false);
			as.setCanPickupItems(false);
			as.setCustomName(line);
			as.setCustomNameVisible(true);
			as.setVisible(false);

			armorStands.add(as);
			
			plugin.blockArmorStandCombinations.add(new BlockArmorStandCombination(block,as));
			
			currentOffset += lineOffset;

		}
		scanner.close();
	}
	
	public void destroy() {
		for(ArmorStand armorStand : armorStands) {
			armorStand.remove();
		}
	}

}