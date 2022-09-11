package dev.chaws.automaticinventory.tasks;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.common.DepositRecord;
import dev.chaws.automaticinventory.configuration.*;
import dev.chaws.automaticinventory.events.*;
import dev.chaws.automaticinventory.listeners.*;
import dev.chaws.automaticinventory.messaging.*;
import dev.chaws.automaticinventory.utilities.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncChestDepositTask extends Thread {
	private World world;
	private ChunkSnapshot[][] snapshots;
	private int minY;
	private int maxY;
	private int startX;
	private int startY;
	private int startZ;
	private Player player;
	private ChunkSnapshot smallestChunk;

	private boolean[][][] seen;

	public AsyncChestDepositTask(World world, ChunkSnapshot[][] snapshots, int minY, int maxY, int startX, int startY, int startZ, Player player) {
		this.world = world;
		this.snapshots = snapshots;
		this.minY = minY;
		this.maxY = maxY;
		this.smallestChunk = this.snapshots[0][0];
		this.startX = startX - this.smallestChunk.getX() * 16;
		this.startY = startY;
		this.startZ = startZ - this.smallestChunk.getZ() * 16;
        if (this.maxY >= world.getMaxHeight()) {
            this.maxY = world.getMaxHeight() - 1;
        }
		this.player = player;
		this.seen = new boolean[48][this.maxY - this.minY + 1][48];
	}

	@Override
	public void run() {
		Queue<Location> chestLocations = new ConcurrentLinkedQueue<Location>();
		Queue<Vector> leftToVisit = new ConcurrentLinkedQueue<Vector>();
		var start = new Vector(this.startX, this.startY, this.startZ);
		leftToVisit.add(start);
		this.markSeen(start);
		while (!leftToVisit.isEmpty()) {
			var current = leftToVisit.remove();

			var type = this.getType(current);
			if (MaterialUtilities.isChest(type)) {
				var overType = this.getType(new Vector(current.getBlockX(), current.getBlockY() + 1, current.getBlockZ()));
				if (!BlockUtilities.preventsChestOpen(type, overType)) {
					chestLocations.add(this.makeLocation(current));
				}
			}

			if (MaterialUtilities.isPassable(type)) {
				var adjacents = new Vector[] {
					new Vector(current.getBlockX() + 1, current.getBlockY(), current.getBlockZ()),
					new Vector(current.getBlockX() - 1, current.getBlockY(), current.getBlockZ()),
					new Vector(current.getBlockX(), current.getBlockY() + 1, current.getBlockZ()),
					new Vector(current.getBlockX(), current.getBlockY() - 1, current.getBlockZ()),
					new Vector(current.getBlockX(), current.getBlockY(), current.getBlockZ() + 1),
					new Vector(current.getBlockX(), current.getBlockY(), current.getBlockZ() - 1),
				};

				for (var adjacent : adjacents) {
					if (!this.alreadySeen(adjacent)) {
						leftToVisit.add(adjacent);
						this.markSeen(adjacent);
					}
				}
			}
		}

		var chain = new QuickDepositChain(chestLocations, new DepositRecord(), player, true);
		Bukkit.getScheduler().runTaskLater(AutomaticInventory.instance, chain, 1L);
	}

	private Location makeLocation(Vector location) {
		return new Location(
			this.world,
			this.smallestChunk.getX() * 16 + location.getBlockX(),
			location.getBlockY(),
			this.smallestChunk.getZ() * 16 + location.getBlockZ());
	}

	private Material getType(Vector location) {
        if (this.outOfBounds(location)) {
            return null;
        }
		var chunkx = location.getBlockX() / 16;
		var chunkz = location.getBlockZ() / 16;
		var chunk = this.snapshots[chunkx][chunkz];
		var x = location.getBlockX() % 16;
		var z = location.getBlockZ() % 16;

		return chunk.getBlockType(x, location.getBlockY(), z);
	}

	private boolean alreadySeen(Vector location) {
        if (this.outOfBounds(location)) {
            return true;
        }
		var y = location.getBlockY() - this.minY;
		return this.seen[location.getBlockX()][y][location.getBlockZ()];
	}

	private void markSeen(Vector location) {
        if (this.outOfBounds(location)) {
            return;
        }
		var y = location.getBlockY() - this.minY;
		this.seen[location.getBlockX()][y][location.getBlockZ()] = true;
	}

	private boolean outOfBounds(Vector location) {
        if (location.getBlockY() > this.maxY) {
            return true;
        }
        if (location.getBlockY() < this.minY) {
            return true;
        }
        if (location.getBlockX() >= 48) {
            return true;
        }
        if (location.getBlockX() < 0) {
            return true;
        }
        if (location.getBlockZ() >= 48) {
            return true;
        }
		return location.getBlockZ() < 0;
	}

	static class QuickDepositChain implements Runnable {
		private final Queue<Location> remainingChestLocations;
		private final DepositRecord runningDepositRecord;
		private final Player player;
		private final boolean respectExclusions;

		QuickDepositChain(Queue<Location> remainingChestLocations, DepositRecord runningDepositRecord, Player player, boolean respectExclusions) {
			super();
			this.remainingChestLocations = remainingChestLocations;
			this.runningDepositRecord = runningDepositRecord;
			this.player = player;
			this.respectExclusions = respectExclusions;
		}

		@Override
		public void run() {
			var chestLocation = this.remainingChestLocations.poll();
			if (chestLocation == null) {
				Chat.sendMessage(this.player, Level.Success, Messages.SuccessfulDepositAll2, String.valueOf(this.runningDepositRecord.totalItems));
				var playerConfig = PlayerConfig.FromPlayer(player);
				if (Math.random() < .1 && !playerConfig.isGotQuickDepositInfo() && PlayerConfig.featureEnabled(Features.QuickDeposit, player)) {
					Chat.sendMessage(player, Level.Instr, Messages.QuickDepositAdvertisement3);
					playerConfig.setGotQuickDepositInfo(true);
				}

				return;
			}

			var block = chestLocation.getBlock();
			var event = new PlayerInteractEvent(player,
				Action.RIGHT_CLICK_BLOCK,
				player.getInventory().getItemInMainHand(),
				block,
				BlockFace.UP
			);

			Bukkit.getServer().getPluginManager().callEvent(event);
			if (event.useInteractedBlock() != Event.Result.DENY) {
				var state = block.getState();
				if (state instanceof InventoryHolder chest) {
					var chestInventory = chest.getInventory();
					if (!this.respectExclusions || AutomaticInventoryListener.isSortableChestInventory(chestInventory,
						state instanceof Nameable nameable ? nameable.getCustomName() : null)) {
						var playerInventory = player.getInventory();

						var deposits = AutomaticInventory.depositMatching(playerInventory, chestInventory, false);

						this.runningDepositRecord.totalItems += deposits.totalItems;
					}
				}
			}

			var chain = new QuickDepositChain(this.remainingChestLocations, this.runningDepositRecord, this.player, this.respectExclusions);
			Bukkit.getScheduler().runTaskLater(AutomaticInventory.instance, chain, 1L);
		}
	}
}
