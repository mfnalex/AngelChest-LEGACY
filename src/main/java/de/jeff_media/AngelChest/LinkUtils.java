package de.jeff_media.AngelChest;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class LinkUtils {
	
	protected static TextComponent getLinks(Player p, String preText, String commandTp, String commandUnlock, String commandFetch, Main plugin) {
		  TextComponent text = new TextComponent(preText);
		
		  String placeholder = "";
		  if(p.hasPermission("angelchest.tp") && commandTp != null) {
			  TextComponent link = createCommandLink(plugin.messages.LINK_TP,commandTp);
			  text.addExtra(link);
			  placeholder = " ";
		  }
		  if(p.hasPermission("angelchest.fetch") && commandFetch != null) {
			TextComponent link = createCommandLink(plugin.messages.LINK_FETCH,commandFetch);
			text.addExtra(placeholder);
			text.addExtra(link);
		}
		  if(commandUnlock != null) {
			  TextComponent link = createCommandLink(plugin.messages.LINK_UNLOCK,commandUnlock);
			  text.addExtra(placeholder);
			  text.addExtra(link);
		  }
		  
		  //TextComponent placeholder = new TextComponent(" | ");
		  //placeholder.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		  

//		  text.addExtra(placeholder);
//		  text.addExtra(donate);
//		  text.addExtra(placeholder);
//		  text.addExtra(changelog);
	        
		  return text;
	}
	
	private static TextComponent createCommandLink(String text, String command) {
		// Hover text
		/*ComponentBuilder hoverCB = new ComponentBuilder(
                text+" Link: ").bold(true)
                .append(link).bold(false);*/
		
		TextComponent tc = new TextComponent(text);
		//tc.setBold(true);
		//tc.setColor(color);
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
		//tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, null));
		return tc;
	}

}
