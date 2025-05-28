package dev.chaws.automaticinventory.commands;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoSortCommand extends AutomaticInventoryCommand {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player) || args.length == 0) {
			return true;
		}

		var playerConfig = PlayerConfig.fromPlayer(player);

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

			return true;
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

			return true;
		} else {
			Chat.sendMessage(player, Level.Error, Messages.AutoSortHelp);
			return false;
		}
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length != 1) {
			return Collections.emptyList();
		}

		var commands = new ArrayList<String>();
		commands.add("chest");
		commands.add("inventory");

		return commands;
	}
}
