# Changelog

## 2.8.1-SNAPSHOT
- Updated Chinese and Chinese (Traditional) translations

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
