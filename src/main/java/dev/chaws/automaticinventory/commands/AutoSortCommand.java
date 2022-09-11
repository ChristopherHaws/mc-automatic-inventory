package dev.chaws.automaticinventory.commands;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.entity.Player;

public class AutoSortCommand implements IAutomaticInventoryCommand {
	public boolean execute(Player player, PlayerConfig playerConfig, String[] args) {
		if (args.length < 1) {
			Chat.sendMessage(player, Level.Instr, Messages.AutoSortHelp);
			return true;
		}

		var optionName = args[0].toLowerCase();
		if (optionName.startsWith("chest")) {
			if (!AutomaticInventory.hasPermission(Features.SortChests, player)) {
				Chat.sendMessage(player, Level.Error, Messages.NoPermissionForFeature);
				return true;
			}

			playerConfig.setSortChests(!playerConfig.isSortChests());

			if (playerConfig.isSortChests()) {
				Chat.sendMessage(player, Level.Success, Messages.ChestSortEnabled);
			} else {
				Chat.sendMessage(player, Level.Success, Messages.ChestSortDisabled);
			}
		} else if (optionName.startsWith("inv")) {
			if (!AutomaticInventory.hasPermission(Features.SortInventory, player)) {
				Chat.sendMessage(player, Level.Error, Messages.NoPermissionForFeature);
				return true;
			}

			playerConfig.setSortInventory(!playerConfig.isSortInventory());

			if (playerConfig.isSortInventory()) {
				Chat.sendMessage(player, Level.Success, Messages.InventorySortEnabled);
			} else {
				Chat.sendMessage(player, Level.Success, Messages.InventorySortDisabled);
			}
		} else {
			Chat.sendMessage(player, Level.Error, Messages.AutoSortHelp);
			return true;
		}

		AutomaticInventory.instance.DeliverTutorialHyperlink(player);

		return true;
	}
}
