package dev.chaws.automaticinventory.listeners;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.tasks.InventorySorter;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.InventoryUtilities;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class SortChestsListener implements Listener {
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent event) {
		var playerInventory = InventoryUtilities.getPlayerInventory(event.getView());
		if (playerInventory == null) {
			return;
		}

		var player = InventoryUtilities.getPlayer(playerInventory);
		if (player == null) {
			return;
		}

		var playerConfig = PlayerConfig.fromPlayer(player);

		if (!player.isSneaking() && PlayerConfig.featureEnabled(Features.SortChests, player)) {
			var topInventory = event.getView().getTopInventory();
			if (!InventoryUtilities.isSortableChestInventory(topInventory, event.getView().getTitle())) {
				return;
			}

			var sorter = new InventorySorter(topInventory, 0);
			AutomaticInventory.instance.getServer().getScheduler().scheduleSyncDelayedTask(AutomaticInventory.instance, sorter, 1L);

			if (!playerConfig.hasReceivedChestSortInfo()) {
				Chat.sendMessage(player, Level.Info, Messages.ChestSortEducation3);
				playerConfig.setReceivedChestSortInfo(true);
			}
		}
	}
}
