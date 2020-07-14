package de.jeff_media.AngelChest;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockListener implements Listener {

	Main plugin;

	public BlockListener(Main plugin) {

		this.plugin = plugin;

		plugin.debug("BlockListener created");
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!plugin.isAngelChest(event.getBlock()))
			return;
		AngelChest angelChest = plugin.getAngelChest(event.getBlock());
		if (!angelChest.owner.equals(event.getPlayer().getUniqueId())
				&& !event.getPlayer().hasPermission("angelchest.protect.ignore") && angelChest.isProtected) {
			event.getPlayer().sendMessage(plugin.messages.MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS);
			event.setCancelled(true);
			return;
		}
		angelChest.destroy();
		angelChest.remove();
	}

	@EventHandler
	public void onLiquidDestroysChest(BlockFromToEvent event) {
		// Despite the name, this event only fires when liquid or a teleporting dragon egg changes a block
		if(plugin.isAngelChest(event.getToBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList().toArray(new Block[event.blockList().size()])) {
			if (plugin.isAngelChest(block)) {
				event.blockList().remove(block);
			}
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		for (Block block : event.blockList().toArray(new Block[event.blockList().size()])) {
			if (plugin.isAngelChest(block)) {
				event.blockList().remove(block);
			}
		}
	}

	@EventHandler
	public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
		Block block = event.getBlock();
		if (plugin.isAngelChest(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
		Block block = event.getBlock();
		if (plugin.isAngelChest(block)) {
			event.setCancelled(true);
		}
	}
}