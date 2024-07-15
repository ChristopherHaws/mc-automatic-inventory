package dev.chaws.automaticinventory.listeners;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.GlobalConfig;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.tasks.AutoRefillHotBarTask;
import dev.chaws.automaticinventory.utilities.ItemUtilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RefillStacksListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onToolBreak(PlayerItemBreakEvent event) {
		var player = event.getPlayer();
		var inventory = player.getInventory();
		var slot = this.getSlotWithItemStack(inventory, event.getBrokenItem());

		tryRefillStackInHand(player, slot);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		var player = event.getPlayer();
		tryRefillStackInHand(player, event.getHand());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFertilize(BlockFertilizeEvent event) {
		var player = event.getPlayer();
		if (player == null) {
			return;
		}
		var inventory = player.getInventory();
		var slot = this.getSlotWithItemStack(inventory, new ItemStack(Material.BONE_MEAL));
		tryRefillStackInHand(player, slot);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onConsumeItem(PlayerItemConsumeEvent event) {
		var player = event.getPlayer();
		var inventory = player.getInventory();
		var slot = this.getSlotWithItemStack(inventory, event.getItem());
		tryRefillStackInHand(player, slot);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		var source = event.getEntity().getShooter();
		if (!(source instanceof Player player)) {
			return;
		}

		tryRefillStackInHand(player, EquipmentSlot.HAND);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFeedAnimal(PlayerInteractEntityEvent event) {
		var player = event.getPlayer();
		tryRefillStackInHand(player, event.getHand());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCompostItem(PlayerInteractEvent event) {
		var clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock.getType() == Material.COMPOSTER) {
			tryRefillStackInHand(event.getPlayer(), event.getHand());
		}
	}

	private void tryRefillStackInHand(Player player, EquipmentSlot slot) {
		if (slot == null) {
			return;
		}

		if (!PlayerConfig.featureEnabled(Features.RefillStacks, player)) {
			return;
		}

		ItemStack stack;
		int slotIndex;
		if (slot == EquipmentSlot.HAND) {
			stack = player.getInventory().getItemInMainHand();
			slotIndex = player.getInventory().getHeldItemSlot();
		} else if (slot == EquipmentSlot.OFF_HAND) {
			stack = player.getInventory().getItemInOffHand();
			slotIndex = 40;
		} else {
			return;
		}

		if (GlobalConfig.instance.autoRefillExcludedItems.contains(stack.getType())) {
			return;
		}

		if (stack.getAmount() == 1) {
			var inventory = player.getInventory();
			AutomaticInventory.instance.getServer().getScheduler().scheduleSyncDelayedTask(
				AutomaticInventory.instance,
				new AutoRefillHotBarTask(player, inventory, slotIndex, stack.clone()),
				2L);
		}
	}

	private EquipmentSlot getSlotWithItemStack(PlayerInventory inventory, ItemStack brokenItem) {
		if (inventory.getItemInMainHand().isSimilar(brokenItem)) {
			return EquipmentSlot.HAND;
		}

		if (inventory.getItemInOffHand().isSimilar(brokenItem)) {
			return EquipmentSlot.OFF_HAND;
		}

		return null;
	}
}
