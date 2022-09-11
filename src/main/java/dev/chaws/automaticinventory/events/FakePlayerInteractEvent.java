package dev.chaws.automaticinventory.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FakePlayerInteractEvent extends PlayerInteractEvent {
	public FakePlayerInteractEvent(Player player, Action rightClickBlock, ItemStack itemInHand, Block clickedBlock, BlockFace blockFace) {
		super(player, rightClickBlock, itemInHand, clickedBlock, blockFace);
	}
}
