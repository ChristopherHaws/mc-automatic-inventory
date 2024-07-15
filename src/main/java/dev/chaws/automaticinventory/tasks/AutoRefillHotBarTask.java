package dev.chaws.automaticinventory.tasks;

import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.ItemUtilities;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AutoRefillHotBarTask implements Runnable {
	private final Player player;
	private final PlayerInventory targetInventory;
	private final int slotToRefill;
	private final ItemStack stackToReplace;

	public AutoRefillHotBarTask(Player player, PlayerInventory targetInventory, int slotToRefill, ItemStack stackToReplace) {
		this.player = player;
		this.targetInventory = targetInventory;
		this.slotToRefill = slotToRefill;
		this.stackToReplace = stackToReplace;
	}

	@Override
	public void run() {
		var currentStack = this.targetInventory.getItem(this.slotToRefill);
		if (currentStack != null) {
			return;
		}

		ItemStack bestMatchStack = null;
		var bestMatchSlot = -1;
		var bestMatchStackSize = Integer.MAX_VALUE;
		for (var i = 0; i < 36; i++) {
			var itemInSlot = this.targetInventory.getItem(i);
			if (itemInSlot == null) {
				continue;
			}

			if (itemInSlot.isSimilar(this.stackToReplace)) {
				var stackSize = itemInSlot.getAmount();
				if (stackSize < bestMatchStackSize) {
					bestMatchStack = itemInSlot;
					bestMatchSlot = i;
					bestMatchStackSize = stackSize;
				}

				if (bestMatchStackSize == 1) {
					break;
				}
			}
		}

		if (bestMatchStack == null) {
			return;
		}

		this.targetInventory.setItem(this.slotToRefill, bestMatchStack);
		this.targetInventory.clear(bestMatchSlot);

		var playerConfig = PlayerConfig.fromPlayer(player);
		if (!playerConfig.hasReceivedRestackInfo()) {
			Chat.sendMessage(player, Level.Info, Messages.AutoRefillEducation);
			playerConfig.setReceivedRestackInfo(true);
		}
	}
}
