package de.jeff_media.AngelChest;

import org.bukkit.ChatColor;


public class Messages {
	//private AngelChestPlugin plugin;

	final String MSG_PLAYERSONLY,MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS,MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS,
	MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED, MSG_UNLOCKED_ONE_ANGELCHEST, MSG_UNLOCKED_MORE_ANGELCHESTS, MSG_INVENTORY_WAS_EMPTY,
	MSG_ANGELCHEST_CREATED, MSG_ANGELCHEST_DISAPPEARED, MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS, MSG_YOU_GOT_YOUR_INVENTORY_BACK
	, MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK, HOLOGRAM_TEXT, ANGELCHEST_INVENTORY_NAME, MSG_ANGELCHEST_LOCATION, MSG_NOT_ENOUGH_MONEY;
	
	final String LINK_TP, LINK_FETCH, LINK_UNLOCK;

	Messages(Main plugin) {
		//this.plugin = plugin;

		MSG_PLAYERSONLY = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-error-players-only", "&cError: This command can only be run by players."));
		
		MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-not-allowed-to-break-other-angelchests", "&cYou are not allowed to break other people's AngelChest."));
		
		MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-not-allowed-to-open-other-angelchests", "&cYou are not allowed to open other people's AngelChest."));
		
		MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-you-dont-have-any-angelchests", "&eYou don't have any AngelChests."));
		
		MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-all-your-angelchests-were-already-unlocked", "&eAll your AngelChests were already unlocked."));
		
		MSG_UNLOCKED_ONE_ANGELCHEST = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-unlocked-one-angelchest", "&aYou have unlocked your AngelChest."));
		
		MSG_ANGELCHEST_DISAPPEARED = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-angelchest-disappeared", "&cYou were too slow... Your AngelChest has disappeared and dropped its contents."));
		
		MSG_UNLOCKED_MORE_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("message-unlocked-more-angelchests", "&aYou have unlocked %d AngelChests."));
		
		MSG_INVENTORY_WAS_EMPTY = ChatColor.translateAlternateColorCodes('&',  plugin.getConfig().getString("message-inventory-was-empty", "&eAn Angel searched for your stuff but could not find anything."));
		
		MSG_ANGELCHEST_CREATED = ChatColor.translateAlternateColorCodes('&',  plugin.getConfig().getString("message-angelchest-created", "&aAn Angel collected your stuff and put it into a chest located at the place of your death."));
		
		MSG_YOU_GOT_YOUR_INVENTORY_BACK = ChatColor.translateAlternateColorCodes('&',  plugin.getConfig().getString("message-you-got-your-inventory-back", "&aYou got your inventory back!"));
		
		MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK = ChatColor.translateAlternateColorCodes('&',  plugin.getConfig().getString("message-you-got-part-of-your-inventory-back", "&eYou got a part of your inventory back, but some items are still in the AngelChest."));

		MSG_NOT_ENOUGH_MONEY = ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("message-not-enough-money","&cYou don't have enough money."));
		
		HOLOGRAM_TEXT = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hologram-text","&a&l[AngelChest]&r\n&b%s"));
		
		ANGELCHEST_INVENTORY_NAME = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("angelchest-inventory-name","&a[AngelChest] &b%s&r"));
		
		MSG_ANGELCHEST_LOCATION = ChatColor.translateAlternateColorCodes('&',  plugin.getConfig().getString("message-angelchest-location","&eLocation of your AngelChests:").replaceAll(": %s", ""));
		
		LINK_TP = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("link-teleport","&6[TP]&r"));

		LINK_FETCH = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("link-fetch","&6[Fetch]&r"));
		
		LINK_UNLOCK = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("link-unlock","&5[Unlock]&r"));
		
	}

}