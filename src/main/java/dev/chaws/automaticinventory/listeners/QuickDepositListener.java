package dev.chaws.automaticinventory.listeners;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.utilities.BlockUtilities;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.InventoryUtilities;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

public class QuickDepositListener implements Listener {
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockDamage(BlockDamageEvent event) {
		var player = event.getPlayer();
		if (!player.isSneaking()) {
			return;
		}

		if (!PlayerConfig.featureEnabled(Features.QuickDeposit, player)) {
			return;
		}

		var clickedBlock = event.getBlock();
		if (!BlockUtilities.isContainer(clickedBlock)) {
			return;
		}

		var clickBlockEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getInventory().getItemInMainHand(), clickedBlock, BlockFace.EAST);
		AutomaticInventory.instance.getServer().getPluginManager().callEvent(clickBlockEvent);
		if (clickBlockEvent.useInteractedBlock() == Event.Result.DENY) {
			return;
		}

		var chest = (InventoryHolder) clickedBlock.getState();
		var chestInventory = chest.getInventory();
		var playerInventory = player.getInventory();

		event.setCancelled(true);

		var aboveBlockID = clickedBlock.getRelative(BlockFace.UP).getType();
		if (!BlockUtilities.isOpenable(clickedBlock.getType(), aboveBlockID)) {
			Chat.sendMessage(player, Level.Error, Messages.ChestLidBlocked);
			return;
		}

		var deposits = InventoryUtilities.depositMatching(playerInventory, chestInventory, true);

		//send confirmation message to player with counts deposited.  if none deposited, give instructions on how to set up the chest.
		if (deposits.destinationFull && deposits.totalItems == 0) {
			Chat.sendMessage(player, Level.Error, Messages.FailedDepositChestFull2);
		} else if (deposits.totalItems == 0) {
			Chat.sendMessage(player, Level.Info, Messages.FailedDepositNoMatch);
		} else {
			Chat.sendMessage(player, Level.Success, Messages.SuccessfulDeposit2, String.valueOf(deposits.totalItems));

			//make a note that quick deposit was used so that player will not be bothered with advertisement messages again.
			var playerConfig = PlayerConfig.fromPlayer(player);
			if (!playerConfig.hasUsedQuickDeposit()) {
				playerConfig.setUsedQuickDeposit(true);
			}
		}
	}
}
