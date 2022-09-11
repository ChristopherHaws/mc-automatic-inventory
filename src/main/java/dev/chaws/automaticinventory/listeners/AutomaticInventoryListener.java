package dev.chaws.automaticinventory.listeners;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.configuration.Features;
import dev.chaws.automaticinventory.configuration.GlobalConfig;
import dev.chaws.automaticinventory.configuration.PlayerConfig;
import dev.chaws.automaticinventory.events.FakePlayerInteractEvent;
import dev.chaws.automaticinventory.messaging.Messages;
import dev.chaws.automaticinventory.tasks.AutoRefillHotBarTask;
import dev.chaws.automaticinventory.tasks.InventorySorter;
import dev.chaws.automaticinventory.tasks.PickupSortTask;
import dev.chaws.automaticinventory.utilities.BlockUtilities;
import dev.chaws.automaticinventory.utilities.Chat;
import dev.chaws.automaticinventory.utilities.ItemUtilities;
import dev.chaws.automaticinventory.utilities.Level;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

public class AutomaticInventoryListener implements Listener {
	private EquipmentSlot getSlotWithItemStack(PlayerInventory inventory, ItemStack brokenItem) {
		if (ItemUtilities.itemsAreSimilar(inventory.getItemInMainHand(), brokenItem)) {
			return EquipmentSlot.HAND;
		}
		if (ItemUtilities.itemsAreSimilar(inventory.getItemInOffHand(), brokenItem)) {
			return EquipmentSlot.OFF_HAND;
		}

		return null;
	}

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
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.COMPOSTER) {
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

		if (GlobalConfig.instance.config_noAutoRefill.contains(stack.getType())) {
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
		if (!(clickedBlock.getState() instanceof Chest)
			&& !(clickedBlock.getState() instanceof ShulkerBox)
			&& !(clickedBlock.getState() instanceof Barrel)) {
			return;
		}

		PlayerInteractEvent fakeEvent = new FakePlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getInventory().getItemInMainHand(), clickedBlock, BlockFace.EAST);
		Bukkit.getServer().getPluginManager().callEvent(fakeEvent);
		if (fakeEvent.isCancelled()) {
			return;
		}

		var chest = (InventoryHolder) clickedBlock.getState();
		var chestInventory = chest.getInventory();
		var playerInventory = player.getInventory();

		event.setCancelled(true);

		var aboveBlockID = clickedBlock.getRelative(BlockFace.UP).getType();
		if (BlockUtilities.preventsChestOpen(clickedBlock.getType(), aboveBlockID)) {
			Chat.sendMessage(player, Level.Error, Messages.ChestLidBlocked);
			return;
		}

		var deposits = AutomaticInventory.depositMatching(playerInventory, chestInventory, true);

		//send confirmation message to player with counts deposited.  if none deposited, give instructions on how to set up the chest.
		if (deposits.destinationFull && deposits.totalItems == 0) {
			Chat.sendMessage(player, Level.Error, Messages.FailedDepositChestFull2);
		} else if (deposits.totalItems == 0) {
			Chat.sendMessage(player, Level.Info, Messages.FailedDepositNoMatch);
		} else {
			Chat.sendMessage(player, Level.Success, Messages.SuccessfulDeposit2, String.valueOf(deposits.totalItems));

			//make a note that quick deposit was used so that player will not be bothered with advertisement messages again.
			var playerConfig = PlayerConfig.FromPlayer(player);
			if (!playerConfig.isUsedQuickDeposit()) {
				playerConfig.setUsedQuickDeposit(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent event) {
		var bottomInventory = event.getView().getBottomInventory();
		if (bottomInventory.getType() != InventoryType.PLAYER) {
			return;
		}

		var holder = ((PlayerInventory) bottomInventory).getHolder();
		if (!(holder instanceof Player player)) {
			return;
		}

		var playerConfig = PlayerConfig.FromPlayer(player);
		sortPlayerIfEnabled(player, playerConfig, bottomInventory);

		if (!player.isSneaking() && PlayerConfig.featureEnabled(Features.SortChests, player)) {
			var topInventory = event.getView().getTopInventory();
			if (!isSortableChestInventory(topInventory, event.getView().getTitle())) {
				return;
			}

			var sorter = new InventorySorter(topInventory, 0);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(AutomaticInventory.instance, sorter, 1L);

			if (!playerConfig.isGotChestSortInfo()) {
				Chat.sendMessage(player, Level.Info, Messages.ChestSortEducation3);
				playerConfig.setGotChestSortInfo(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		var bottomInventory = event.getView().getBottomInventory();
		if (bottomInventory.getType() != InventoryType.PLAYER) {
			return;
		}

		var holder = ((PlayerInventory) bottomInventory).getHolder();
		if (!(holder instanceof Player player)) {
			return;
		}

		var playerConfig = PlayerConfig.FromPlayer(player);

		sortPlayerIfEnabled(player, playerConfig, bottomInventory);

		if (player.getGameMode() != GameMode.CREATIVE && Math.random() < .1 && !playerConfig.isGotDepositAllInfo() && PlayerConfig.featureEnabled(Features.DepositAll, player)) {
			var topInventory = event.getView().getTopInventory();
			if (topInventory.getType() == InventoryType.CHEST) {
				Chat.sendMessage(player, Level.Instr, Messages.DepositAllAdvertisement);
				playerConfig.setGotDepositAllInfo(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPickupItem(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (!PlayerConfig.featureEnabled(Features.SortInventory, player)) {
			return;
		}
		var playerConfig = PlayerConfig.FromPlayer(player);
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
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(AutomaticInventory.instance, task, 10L);
	}

	public static void sortPlayerIfEnabled(Player player, PlayerConfig playerConfig, Inventory inventory) {
		if (!PlayerConfig.featureEnabled(Features.SortInventory, player)) {
			return;
		}

		new InventorySorter(inventory, 9).run();

		if (!playerConfig.isGotInventorySortInfo()) {
			Chat.sendMessage(player, Level.Info, Messages.InventorySortEducation);
			playerConfig.setGotInventorySortInfo(true);
		}
	}

	public static boolean isSortableChestInventory(Inventory inventory, String name) {
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

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerJoin(PlayerJoinEvent event) {
		var player = event.getPlayer();
		PlayerConfig.Preload(player);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		var player = event.getPlayer();
		PlayerConfig.FromPlayer(player).saveChanges();
	}
}
