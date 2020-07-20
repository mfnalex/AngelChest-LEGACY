package de.jeff_media.AngelChest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class HologramListener implements Listener {
	
	final Main plugin;
	
	public HologramListener(Main plugin) {
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