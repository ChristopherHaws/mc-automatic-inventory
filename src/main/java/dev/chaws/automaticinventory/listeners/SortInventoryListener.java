package dev.chaws.automaticinventory.listeners;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.tasks.InventorySorter;
import dev.chaws.automaticinventory.tasks.PickupSortTask;
import dev.chaws.automaticinventory.utilities.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;

public class SortInventoryListener implements Listener {
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
		sortPlayerIfEnabled(player, playerConfig, playerInventory);

	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		var playerInventory = InventoryUtilities.getPlayerInventory(event.getView());
		if (playerInventory == null) {
			return;
		}

		var player = InventoryUtilities.getPlayer(playerInventory);
		if (player == null) {
			return;
		}

		var playerConfig = PlayerConfig.fromPlayer(player);

		sortPlayerIfEnabled(player, playerConfig, playerInventory);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPickupItem(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (!PlayerConfig.featureEnabled(Features.SortInventory, player)) {
			return;
		}
		var playerConfig = PlayerConfig.fromPlayer(player);
		if (playerConfig.firstEmptySlot >= 0) {
			return;
		}

		var inventory = player.getInventory();
		var firstEmpty = inventory.firstEmpty();
		if (firstEmpty < 9) {
			return;
		}

		playerConfig.firstEmptySlot = firstEmpty;
		var task = new PickupSortTask(player, playerConfig, inventory);
		AutomaticInventory.instance.getServer().getScheduler().scheduleSyncDelayedTask(AutomaticInventory.instance, task, 10L);
	}

	public static void sortPlayerIfEnabled(Player player, PlayerConfig playerConfig, Inventory inventory) {
		if (!PlayerConfig.featureEnabled(Features.SortInventory, player)) {
			return;
		}

		new InventorySorter(inventory, 9).run();

		if (!playerConfig.hasReceivedInventorySortInfo()) {
			Chat.sendMessage(player, Level.Info, Messages.InventorySortEducation);
			playerConfig.setReceivedInventorySortInfo(true);
		}
	}
}
