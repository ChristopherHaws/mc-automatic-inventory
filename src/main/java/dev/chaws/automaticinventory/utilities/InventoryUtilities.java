package dev.chaws.automaticinventory.utilities;

import dev.chaws.automaticinventory.common.DepositRecord;
import dev.chaws.automaticinventory.configuration.GlobalConfig;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class InventoryUtilities {
	@Nullable
	public static PlayerInventory getPlayerInventory(InventoryView view) {
		var bottomInventory = view.getBottomInventory();
		if (bottomInventory.getType() != InventoryType.PLAYER) {
			return null;
		}

		if (!(bottomInventory instanceof PlayerInventory playerInventory)) {
			return null;
		}

		return playerInventory;
	}

	@Nullable
	public static Player getPlayer(PlayerInventory playerInventory) {
		var holder = playerInventory.getHolder();
		if (!(holder instanceof Player player)) {
			return null;
		}

		return player;
	}

	public static boolean isSortableChestInventory(
		@Nullable Inventory inventory,
		@Nullable String name
	) {
		if (inventory == null) {
			return false;
		}

		var inventoryType = inventory.getType();
		if (inventoryType != InventoryType.CHEST
			&& inventoryType != InventoryType.ENDER_CHEST
			&& inventoryType != InventoryType.SHULKER_BOX
			&& inventoryType != InventoryType.BARREL) {
			return false;
		}

		if (name != null && name.contains("*")) {
			return false;
		}

		var holder = inventory.getHolder();
		return holder instanceof Chest
			|| holder instanceof ShulkerBox
			|| holder instanceof DoubleChest
			|| holder instanceof StorageMinecart
			|| holder instanceof Barrel;
	}

	public static DepositRecord depositMatching(PlayerInventory source, Inventory destination, boolean depositHotbar) {
		
		var eligibleSignatures = new HashSet<String>(destination.getSize());
		var deposits = new DepositRecord();
		for (var i = 0; i < destination.getSize(); i++) {
			var destinationStack = destination.getItem(i);
			if (destinationStack == null) {
				continue;
			}

			var signature = ItemUtilities.getSignature(destinationStack);
			eligibleSignatures.add(signature);
		}
		var sourceStartIndex = depositHotbar ? 0 : 9;
		var sourceSize = Math.min(source.getSize(), 36);
		for (var i = sourceStartIndex; i < sourceSize; i++) {
			var sourceStack = source.getItem(i);
			if (sourceStack == null) {
				continue;
			}

			if (GlobalConfig.instance.isItemExcludedViaName(sourceStack)) {
				continue;
			}

			if (GlobalConfig.instance.autoDepositExcludedItems.contains(sourceStack.getType())) {
				continue;
			}

			var signature = ItemUtilities.getSignature(sourceStack);
			var sourceStackSize = sourceStack.getAmount();
			if (eligibleSignatures.contains(signature)) {
				var notMoved = destination.addItem(sourceStack);
				if (notMoved.isEmpty()) {
					source.clear(i);
					deposits.totalItems += sourceStackSize;
				} else {
					var notMovedCount = notMoved.values().iterator().next().getAmount();
					var movedCount = sourceStackSize - notMovedCount;
					if (movedCount == 0) {
						eligibleSignatures.remove(signature);
					} else {
						var newAmount = sourceStackSize - movedCount;
						sourceStack.setAmount(newAmount);
						deposits.totalItems += movedCount;
					}
				}
			}
		}

		if (destination.firstEmpty() == -1) {
			deposits.destinationFull = true;
		}

		return deposits;
	}
}
