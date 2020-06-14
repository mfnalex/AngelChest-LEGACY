package de.jeffclan.AngelChest;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TpLinkUtil {
	
	protected static void sendLink(Player p, String preText, String command) {
		  TextComponent text = new TextComponent(preText);
		
		  TextComponent link = createCommandLink("[Teleport]",command,net.md_5.bungee.api.ChatColor.GOLD); 
		  
		  //TextComponent placeholder = new TextComponent(" | ");
		  //placeholder.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		  
		  text.addExtra(link);
//		  text.addExtra(placeholder);
//		  text.addExtra(donate);
//		  text.addExtra(placeholder);
//		  text.addExtra(changelog);
	        
	      p.spigot().sendMessage(text);
	}
	
	private static TextComponent createCommandLink(String text, String command, net.md_5.bungee.api.ChatColor color) {
		// Hover text
		/*ComponentBuilder hoverCB = new ComponentBuilder(
                text+" Link: ").bold(true)
                .append(link).bold(false);*/
		
		TextComponent tc = new TextComponent(text);
		tc.setBold(true);
		tc.setColor(color);
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
		return tc;
	}

}
