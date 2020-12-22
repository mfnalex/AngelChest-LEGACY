# Changelog

## 2.20.0
- Added option to refund players when they don't open their AngelChest before it expires (default: false)

## 2.19.5
- Fixed Exception (again, sorry)

## 2.19.4
- Fixed Exception

## 2.19.3
- Added Soulbound detection for basically every plugin offering soulbound items.

## 2.19.2
- Added option to log player accesses to AngelChests to console

## 2.19.1
- Added PlacerholderAPI support for the hologram text

## 2.19.0
- Added configurable price to spawn the chest (default: 0)
- Added support for PlaceholderAPI, see here: https://github.com/JEFF-Media-GbR/Spigot-AngelChest/blob/master/PlaceholderAPI.md 

## 2.18.0
- Added support for Vanilla Tweaks Player Head Drops datapack

## 2.17.4
- Fixed duplication bug when breaking an AngelChest with a piston while attempting to remove stuff from the chest
- Improved error message when using outdated WorldGuard versions

## 2.17.2
- Updated Turkish translation (thanks to @Cazcez)

## 2.17.1
- Added compatibility for SSSpigot (a fork of Paper or something, IDK)

## 2.17.0
- Added option to show remaining in AngelChest hologram (will be updated every second).
- IMPORTANT: Please note that you have to replace the old "%s" for the player's name with the new placeholder "{player}". The new default value now is this:
```yaml
##### Hologram text and name of the AngelChest.
# {player} is the player's name.
# {time} is the amount of time left.
# \n is a new line.
##### Use an empty string to disable the hologram.
hologram-text: "&a&l[AngelChest]&r\n&b{player}\n&6{time}"
angelchest-inventory-name: "&a[AngelChest] &b{player}&r"
```

## 2.16.4-SNAPSHOT
- Fixed exception when using unsupported WorldGuard versions

## 2.16.3
- Fixed auto config updater causing exceptions on start

## 2.16.2
- Fixed essentials.keepinv not working anymore

## 2.16.1
- Made /aclist text translatable

## 2.16.0
- Added confirmation message for actions that cost money (default: enabled)
- Prevents player from spawning above lava when teleporting to the chest
- Made message after fetching the AngelChest configurable
- Fixed "no-angelchest-in-pvp" message not being configurable
- Updated Polish translation

## 2.15.3
- Fixed AngelChest cancelling PlayerInteractEvents for armor stands that are not AngelChest holograms

## 2.15.2
- Readded Minepacks support. Will instantly work with Spigot and Paper. Tuinity users have to wait for Minepacks update 2.3.14

## 2.15.1
- Fixed plugin not enabling when InventoryPages is not installed
- Fixed chunk problems
- Temporarily disabled Minepacks hook

## 2.15.0
- Added InventoryPages support (will be further improved in next releases)

## 2.14.4
- Fixed generated config is broken on 1.12.2
- Fixed error when using "totem-of-undying-works-everywhere" on 1.12.2

## 2.14.3
- Fixed "ignore-keep-inventory" behaving opposite of how it should

## 2.14.2
- Added possibility to use AngelChests of infinite duration (just set the duration to 0)

## 2.14.1
- Fixed AngelChests being destroyed when player died in a massive TNT explosion
- Fixed players with permission "angelchest.xp.levels" not getting their full levels restored
- Fixed no AngelChest being spawned if player killed himself and "allow-angelchest-in-pvp" is false
- Skip repeating tasks if chunk is not loaded

## 2.14.0
- Added option to not spawn a chest if player died in a PVP battle (thanks to Bibithom who let me kill them for "testing purposes" ^^)
- Added option to move the hologram up or down (e.g. when using a head as chest material, you can move the hologram down so it looks better)

## 2.13.0
- Added disabled-materials list to prevent certain items from being put into the chest. This will later be improved by including custom display names or lores.

## 2.12.0
- Added option to limit the maximum amount of AngelChests for all players and/or per group
- /aclist will only show the amount of hours if the AngelChest's duration exceeds one hour

## 2.11.3
- Added async chunk loading if Paper or forks of Paper are used. On Spigot, chunks will be loaded normally.
- Fixed unlock, fetch and tp links not being shown in /aclist

## 2.11.2
- Added Italian translation
- Added option to show links in /acinfo on a separate line (default: true)

## 2.11.1
- Removed forgotten debug messages (sorry)

## 2.11.0
- Added check if chunk is loaded on death. If not, AngelChest tries to load the chunk
- Fixed AngelChests duplicating items when the chest material is one that drops when the block below is broken, e.g. torch, lantern, sign etc.
- Fixed holograms not being clickable
- Made message "message-already-unlocked" configurable
- Updated German, Spanish and Turkish translation

## 2.10.0
- Added option to use custom player heads instead of the player's head using their base64 value
- Added option to disable holograms for AngelChests
- Added option to auto-respawn the player, either instantly or after a set amount of seconds.
- Added permission node "angelchest.others" to use /aclist, /acunlock, /acfetch and /actp for other players
- Improved Soulbound detection for Slimefun items

## 2.9.0
- Items with Slimefun's "Soulbound" enchantment will not be put into the chest but remain in the player inventory
- Added config option to allow totems to be used from every inventory slot
- Added config option "ignore-keep-inventory" so that a chest is spawned even if another plugin allows you to keep your inventory

## 2.8.2
- Changed PlayerInteractEvent priority to lowest so that GriefDefender does not show a warning when opening the AngelChest. If you use GriefDefender, please also update that to the latest version as that plugin also changed the EventPriority to avoid this issue :)

## 2.8.1
- Fixed exceptions when spawning chest. Sorry about that.

## 2.8.0
- Fixed compatibility with 1.12.2 again
- Fixed WorldGuard compatability, now works with:
  - WorldGuard 6
  - WorldGuard 7+
  - No WorldGuard at all

## 2.7.1
- Fixed exception and plugin not enabling when WorldGuard is not installed
- Removed forgotten debug message

## 2.7.0
- Added WorldGuard region blacklist
  - You can add certain WorldGuard regions to your `disabled-worldguard-regions` list in the config.yml. Players in that region will not spawn an AngelChest.
- Added Russian translation
- Removed xp settings from groups.default.yml because that is controlled via permissions

## 2.6.1
- Fixed AngelChests disappearing at the moment it is spawned when the player dies at the exact spot where and when the ender dragon spawns a crystal

## 2.6.0
- Added "remove-curse-of-binding" and "remove-curse-of-vanishing" config options. When true, items with those enchantments will not be added to the AngelChest

## 2.5.1
- Fixed AngelChests not working anymore when using /acreload after changing the chest material
- Fixed AngelChests being destroyed by water if PLAYER_HEAD is used as chest material
- Updated Turkish translation
- Added debug mode

## 2.5.0
- Added /acfetch command (thanks @ XDleader555)
- Updated API to 1.16.1
- Added Dutch translation (thanks @ Xeyame)

## 2.4.0
- Added option to charge a player for teleports

## 2.3.0
- Added Spanish translation
- Added option to use player heads as chest material

## 2.2.1
- Fixed AngelChest still being visible in /acinfo after it has been collected

## 2.2.0
- Added groups to allow custom chest durations per player (see groups.example.yml)
- Added /acreload command to reload the configuration file
- Added permissions:
  - angelchest.xp: stores and restores amount of dropped XP in the chest
  - angelchest.xp.levels: stores and restores amount of levels the player had
  - angelchest.reload: allows usage of /acreload
- Fixed weird config update bug regarding UTF-8 problems
- Fixed exception in console when an AngelChest despawns because of its time limit
- Improved general performance

## 2.1.2
- Fixed exception on server startup when AngelChests were located in Multiverse worlds
- AngelChests now survive even when the world is renamed
- Improved the way AngelChests saved to disk on server shutdown

## 2.1.1
- Fixed infinite experience bug when a user does not take all items out of the AngelChest

## 2.1.0
- AngelChests survive server restarts
- Prevent Minepacks backpacks from being put into AngelChests
- Fixed newlines disappearing in hologram-text during automatic config update (you will have to add the newline again once)

## 2.0.0
- Hopefully fixed the armor stands not disappearing once and for all
- Added option to list all AngelChests
- Added new permission "angelchest.tp" that allows to tp to your AngelChests. When you have this permission, there will be a clickable link next to each AngelChest when running /acinfo or /aclist
- AngelChest can hold the experience that would be normally dropped, or drop the XP orbs naturally like in vanilla (configurable)
- Prevent the AngelChest from spawning on certain blocks (configurable)
- Automatically applies armor when opening an AngelChest
- Prevent the AngelChest from being damaged by bed explosions, pistons, etc.
- Made AngelChest material configurable, default: CHEST
- Items with Curse of Vanishing will disappear
- AngelChests can also be opened by rightclicking the hologram
- Automatic config updater
- Converted project to maven

Thanks to XDleader555 for his awesome contributions!
