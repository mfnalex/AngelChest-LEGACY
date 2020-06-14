package de.jeffclan.AngelChest;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Hologram {

	ArrayList<ArmorStand> armorStands;
	ArrayList<UUID> armorStandUUIDs;
	double lineOffset = -0.2D;
	double currentOffset = 0.0D;
	
	public Hologram(Block block, String text,AngelChestPlugin plugin) {
		this(block.getLocation().add(new Vector(0.5,-0.5,0.5)),block,text,plugin);
	}

	public Hologram(Location location, Block block, String text, AngelChestPlugin plugin) {

		armorStands = new ArrayList<ArmorStand>();
		armorStandUUIDs = new ArrayList<UUID>();

		Scanner scanner = new Scanner(text);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(new Vector(0,lineOffset,0)), EntityType.ARMOR_STAND); // Spawn the ArmorStand
			armorStandUUIDs.add(as.getUniqueId());
		//	plugin.armorStandUUIDs.add(as.getUniqueId());
			
			//System.out.println("Spawned Armor Stand");

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
		for(ArmorStand armorStand : armorStands.toArray(new ArmorStand[armorStands.size()])) {
			//System.out.println("DESTROYING ARMOR STAND @ " + armorStand.getLocation().toString());
			armorStand.remove();
			
			armorStands.remove(armorStand);
			
			if(!armorStand.equals(null)) {
				armorStand = null;
			}
			
		}
	}

}