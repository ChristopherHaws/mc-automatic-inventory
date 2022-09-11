package dev.chaws.automaticinventory.tasks;

import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.listeners.AutomaticInventoryListener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PickupSortTask implements Runnable {
	private Player player;
	private PlayerConfig playerConfig;
	private Inventory playerInventory;

	public PickupSortTask(Player player, PlayerConfig playerConfig, Inventory playerInventory) {
		this.player = player;
		this.playerConfig = playerConfig;
		this.playerInventory = playerInventory;
	}

	@Override
	public void run() {
		if (this.playerConfig.firstEmptySlot == playerInventory.firstEmpty()) {
			this.playerConfig.firstEmptySlot = -1;
			return;
		}

		AutomaticInventoryListener.sortPlayerIfEnabled(this.player, this.playerConfig, this.playerInventory);

		this.playerConfig.firstEmptySlot = -1;
	}
}
