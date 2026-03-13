# AutomaticInventory (Fork)

> This is a maintained fork of [ChristopherHaws/mc-automatic-inventory](https://github.com/ChristopherHaws/mc-automatic-inventory).

**Deposit ALL your loot into ALL the right boxes with a single slash command! Plus automatic inventory/chest sorting AND MORE!**

## Features

### /DepositAll

Instantly dump all your loot into all the right chests. Finds all chests (within one chunk of your location) that you have permission to open and automatically deposits any items from your backpack (not your hotbar) which match items in those chests.

**Shulker Box Handling:** Named shulker boxes match by name. Unnamed shulker boxes are only deposited if completely full with a single item type (e.g., a shulker with 27 full stacks of cobblestone goes into a chest containing cobblestone). Mixed or partially filled shulkers stay in your inventory.

Aliases: `da`, `dumpitems`, `dumploot`, `depositloot`

### Quick Deposit (Shift-left-click)

Like /DepositAll, but includes your hotbar items and only matches against items in the specific chest you're opening.

### Auto-sort Chests

When a player opens a chest, the contents will be automatically condensed and sorted. Players can disable this or exclude specific chests by renaming them with an asterisk (*).

### Auto-sort Player Inventory

When a player opens their inventory, it's sorted (hotbar untouched).

### Auto-refill Hotbar

When a player uses the last item in a stack or breaks a tool, a replacement item will be pulled from inventory.

## Permissions

By default, all players have all permissions.

| Permission                          | Description                                    |
| ----------------------------------- | ---------------------------------------------- |
| `automaticinventory.user.*`         | All permissions below                          |
| `automaticinventory.sortinventory`  | Personal inventory sorting                     |
| `automaticinventory.sortchests`     | Chest sorting                                  |
| `automaticinventory.refillstacks`   | Auto-replace broken tools and depleted stacks  |
| `automaticinventory.quickdeposit`   | Shift+right-click quick deposit                |
| `automaticinventory.depositall`     | Access to /DepositAll command                  |

## Commands

| Command                               | Description                           |
| ------------------------------------- | ------------------------------------- |
| `/autosort chests`                    | Toggle automatic chest sorting        |
| `/autosort inventory`                 | Toggle automatic inventory sorting    |
| `/depositall`                         | Dump backpack loot into nearby chests |
| `/quickdeposit toggle/enable/disable` | Control quick deposit                 |
| `/autorefill toggle/enable/disable`   | Control hotbar auto-refill            |

## Installation

Copy `AutomaticInventory.jar` into your server's plugins folder and reload/restart.

**Note:** Some anti-cheat plugins may flag /DepositAll as cheating. Consider relaxing right-click reach checks in your anti-cheat config.

## Credits

Fork chain:

- [ChristopherHaws/mc-automatic-inventory](https://github.com/ChristopherHaws/mc-automatic-inventory)
- [MLG-Fortress/AutomaticInventory](https://github.com/MLG-Fortress/AutomaticInventory)
- [HelloWorldMinecraft/AutomaticInventory](https://github.com/HelloWorldMinecraft/AutomaticInventory)
- [BigScary/AutomaticInventory](https://github.com/BigScary/AutomaticInventory)
