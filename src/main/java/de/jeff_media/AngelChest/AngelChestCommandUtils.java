package de.jeff_media.AngelChest;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AngelChestCommandUtils {

	static boolean hasEnoughMoney(Player p, double money, Main main, String messageWhenNotEnoughMoney) {

		main.debug("Checking if "+p.getName()+" has at least " + money + " money...");

		if(money <= 0) {
			main.debug("yes: money <= 0");
			return true;
		}

		Plugin v = main.getServer().getPluginManager().getPlugin("Vault");

		if(v == null) {
			main.debug("yes: vault is null");
			return true;
		}

		RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) {
			main.debug("yes: registered service provider<Economy> is null");
			return true;
		}

		if(rsp.getProvider()==null) {
			main.debug("yes: provider is null");
			return true;
		}

		Economy econ = rsp.getProvider();

		if(econ.getBalance(p)>=money) {
			econ.withdrawPlayer(p,"AngelChest",money);
			main.debug("yes, enough money and paid");
			return true;
		} else {
			main.debug("no, not enough money - nothing paid");
			p.sendMessage(messageWhenNotEnoughMoney);
			return false;
		}
	}

	// Parses the first argument for the chest index in acinfo and returns a valid chest if it exists
	protected static AngelChest argIdx2AngelChest(Main plugin, Player sendTo, Player affectedPlayer, String[] args) {
		// Get all AngelChests by this player
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(affectedPlayer, plugin);

		if(angelChestsFromThisPlayer.size()==0) {
			sendTo.sendMessage(plugin.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
			return null;
		}

		if(angelChestsFromThisPlayer.size() > 1 && args.length == 0) {
			sendTo.sendMessage(plugin.messages.MSG_PLEASE_SELECT_CHEST);
			sendListOfAngelChests(plugin, sendTo,affectedPlayer);
			return null;
		}

		int chestIdx = 0;

		if(args.length > 0) {
			chestIdx = Integer.parseInt(args[0]) - 1;
		}

		if(chestIdx >= angelChestsFromThisPlayer.size() || chestIdx < 0) {
			sendTo.sendMessage(plugin.messages.ERR_INVALIDCHEST);
			return null;
		}

		return angelChestsFromThisPlayer.get(chestIdx);
	}

	static void sendConfirmMessage(CommandSender sender, String command, double price , String message, Main main) {

		TextComponent text = new TextComponent(message.replaceAll("\\{price}", String.valueOf(price)).replaceAll("\\{currency}",getCurrency(price,main)));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		sender.spigot().sendMessage(text);
	}



	protected static void teleportPlayerToChest(Main plugin, Player p, AngelChest ac, CommandSender sender, String[] args) {
		if(!p.hasPermission("angelchest.tp")) {
			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
			return;
		}

		if(!ac.owner.equals(p.getUniqueId())) {
			p.sendMessage(plugin.messages.ERR_NOTOWNER);
			return;
		}
		
		double price = plugin.getConfig().getDouble("price-teleport");

		if (price > 0) {
			PendingConfirm confirm = plugin.pendingConfirms.get(((Player) sender).getUniqueId());
			if (confirm == null || !confirm.chest.equals(ac) || confirm.action != PendingConfirm.Action.TP) {
				plugin.pendingConfirms.put(((Player) sender).getUniqueId(), new PendingConfirm(ac, PendingConfirm.Action.TP));
				String confirmCommand = "/actp " + String.join(" ", args);
				AngelChestCommandUtils.sendConfirmMessage(sender, confirmCommand, price, plugin.messages.MSG_CONFIRM, plugin);
				return;
			}
			plugin.pendingConfirms.remove(((Player) sender).getUniqueId());
		}

		if(price>0 && !hasEnoughMoney(p,price,plugin,plugin.messages.MSG_NOT_ENOUGH_MONEY)) {
			return;
		}

		Location acloc = ac.block.getLocation();
		Location tploc = acloc.clone();
		 try {
			 // offset the target location
			 switch (AngelChestBlockDataUtils.getBlockDirection(ac.block)) {
				 case SOUTH:
					 tploc.add(0, 0, 2);
					 break;
				 case WEST:
					 tploc.add(-2, 0, 0);
					 break;
				 case NORTH:
					 tploc.add(0, 0, -2);
					 break;
				 case EAST:
					 tploc.add(2, 0, 0);
					 break;
				 default:
					 break;
			 }
		 } catch (Throwable ignored) {

		 }

		// Search for a safe spawn point
		List<Block> possibleSpawnPoints = Utils.getPossibleTPLocations(tploc, plugin.getConfig().getInt("max-radius"), plugin);
		Utils.sortBlocksByDistance(tploc.getBlock(), possibleSpawnPoints);

		if(possibleSpawnPoints.size()>0) {
			tploc = possibleSpawnPoints.get(0).getLocation();
		}
		if(possibleSpawnPoints.size()==0) {
			tploc = acloc.getBlock().getRelative(0,1,0).getLocation();
		}

		// Set yaw and pitch of camera
		Location headloc = tploc.clone();
		headloc.add(0,1,0);
		tploc.setDirection(acloc.toVector().subtract(headloc.toVector()));
		tploc.add(0.5,0,0.5);
		
		p.teleport(tploc, TeleportCause.PLUGIN);
	}

	private static String getCurrency(double money, Main main) {

		Plugin v = main.getServer().getPluginManager().getPlugin("Vault");
		if(v==null) return "";

		RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) return "";

		if(rsp.getProvider()==null) return "";

		Economy econ = rsp.getProvider();

		if(econ==null) return "";

		return money == 1 ? econ.currencyNameSingular() : econ.currencyNamePlural();

	}

	protected static void unlockSingleChest(Main plugin, Player sendTo, Player affectedPlayer, AngelChest ac) {
//		if(!p.hasPermission("angelchest.tp")) {
//			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
//			return;
//		}

		if(!ac.owner.equals(affectedPlayer.getUniqueId())) {
			affectedPlayer.sendMessage(plugin.messages.ERR_NOTOWNER);
			return;
		}
		if(!ac.isProtected) {
			affectedPlayer.sendMessage(plugin.messages.ERR_ALREADYUNLOCKED);
			return;
		}
		
		ac.unlock();
		sendTo.sendMessage(plugin.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
	}

	protected static void sendListOfAngelChests(Main plugin, Player sendTo, Player affectedPlayer) {
		// Get all AngelChests by this player
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(affectedPlayer, plugin);
		
		if(angelChestsFromThisPlayer.size()==0) {
			sendTo.sendMessage(plugin.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
			return;
		}
		
		int chestIndex = 1;
		Block b;

		for(AngelChest angelChest : angelChestsFromThisPlayer) {


			String affectedPlayerParameter = "";
			if(!affectedPlayer.equals(sendTo)) affectedPlayerParameter = " "+affectedPlayer.getName();

			b = angelChest.block;
			String tpCommand=null;
			String fetchCommand=null;
			String unlockCommand=null;
			if(sendTo.hasPermission("angelchest.tp")) {
				tpCommand="/actp " + chestIndex + affectedPlayerParameter;
			}
			if(sendTo.hasPermission("angelchest.fetch")) {
				fetchCommand="/acfetch " + chestIndex + affectedPlayerParameter;
			}
			if(angelChest.isProtected) {
				unlockCommand="/acunlock " + chestIndex + affectedPlayerParameter;
			}
			
			String text;

			text = plugin.getConfig().getString("angelchest-list");
			text = text.replaceAll("\\{id}", String.valueOf(chestIndex));
			text = text.replaceAll("\\{x}", String.valueOf(b.getX()));
			text = text.replaceAll("\\{y}", String.valueOf(b.getY()));
			text = text.replaceAll("\\{z}", String.valueOf(b.getZ()));
			text = text.replaceAll("\\{time}",getTimeLeft(angelChest));
			text = text.replaceAll("\\{world}",b.getWorld().getName());
			text = ChatColor.translateAlternateColorCodes('&',text);
			sendTo.spigot().sendMessage(LinkUtils.getLinks(sendTo, affectedPlayer, text, tpCommand, unlockCommand, fetchCommand, plugin));
			chestIndex++;
		}
	}

	public static String getTimeLeft(AngelChest angelChest) {
		int remaining = angelChest.secondsLeft;
		int sec = remaining % 60;
		int min = (remaining / 60) % 60;
		int hour = (remaining / 60) / 60;

		String time;
		if(angelChest.infinite) {
			//text = String.format("[%d] §aX:§f %d §aY:§f %d §aZ:§f %d | %s ",
			//		chestIndex, b.getX(), b.getY(), b.getZ(), b.getWorld().getName()
			time = "∞";
			//);
		}
		else if(hour>0) {
			time = String.format("%02d:%02d:%02d",
					hour, min, sec
			);

		} else {
			time = String.format("%02d:%02d",
					min, sec
			);
		}

		return time;
	}

	protected static void unlockAllChests(Main plugin, Player p) {
		ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p, plugin);

		int chestsUnlocked = 0;
			
		for(AngelChest angelChest : angelChestsFromThisPlayer) {
			if(angelChest.isProtected) {
				angelChest.unlock();
				chestsUnlocked++;
			}
		}
		
		if(chestsUnlocked == 0) {
			p.sendMessage(plugin.messages.MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED);
		}
		
		else if(chestsUnlocked == 1) {
			p.sendMessage(plugin.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
		}
		
		else {
			p.sendMessage(String.format(plugin.messages.MSG_UNLOCKED_MORE_ANGELCHESTS,chestsUnlocked));
		}
	}
}
