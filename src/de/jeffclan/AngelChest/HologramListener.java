package de.jeffclan.AngelChest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class HologramListener implements Listener {
	
	AngelChestPlugin plugin;
	
	public HologramListener(AngelChestPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void manipulate(PlayerArmorStandManipulateEvent e)
	{
	        if(!e.getRightClicked().isVisible())
	        {
	            e.setCancelled(true);
	        }
	}

}