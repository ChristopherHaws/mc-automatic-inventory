package dev.chaws.automaticinventory;

import dev.chaws.automaticinventory.commands.*;
import dev.chaws.automaticinventory.common.DepositRecord;
import dev.chaws.automaticinventory.configuration.*;
import dev.chaws.automaticinventory.listeners.*;
import dev.chaws.automaticinventory.messaging.*;
import dev.chaws.automaticinventory.utilities.ItemUtilities;
import kr.entree.spigradle.annotations.PluginMain;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

@PluginMain
public class AutomaticInventory extends JavaPlugin {
	//for convenience, a reference to the instance of this plugin
	public static AutomaticInventory instance;

	//for logging to the console and log file
	public static Logger log;

	//this handles data storage, like player and region data
	public LocalizedMessages localizedMessages;

	public void onEnable() {
		log = getLogger();
		instance = this;
		GlobalConfig.Initialize(this.getDataFolder());
		this.localizedMessages = new LocalizedMessages();

		//register for events
		var pluginManager = this.getServer().getPluginManager();

		var aIEventHandler = new AutomaticInventoryListener();
		pluginManager.registerEvents(aIEventHandler, this);

		for (Player player : this.getServer().getOnlinePlayers()) {
			PlayerConfig.Preload(player);
		}

		try {
			new Metrics(this, 3547);
		} catch (Throwable ignored) {
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		PlayerConfig playerConfig = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			playerConfig = PlayerConfig.FromPlayer(player);
		}

		if (cmd.getName().equalsIgnoreCase("debugai") && player != null) {
			return new DebugCommand().execute(player, playerConfig, args);
		} else if (cmd.getName().equalsIgnoreCase("autosort") && player != null) {
			return new AutoSortCommand().execute(player, playerConfig, args);
		} else if (cmd.getName().equalsIgnoreCase("depositall") && player != null) {
			return new DepositAllCommand().execute(player, playerConfig, args);
		} else if (cmd.getName().equalsIgnoreCase("quickdeposit") && player != null) {
			return new QuickDepositCommand().execute(player, playerConfig, args);
		} else if (cmd.getName().equalsIgnoreCase("autorefill") && player != null) {
			return new AutoRefillCommand().execute(player, playerConfig, args);
		}

		return false;
	}

	public void DeliverTutorialHyperlink(Player player) {
		//todo: deliver tutorial link to player
	}

	public void onDisable() {
		for (Player player : this.getServer().getOnlinePlayers()) {
			var data = PlayerConfig.FromPlayer(player);
			data.saveChanges();
			data.waitForSaveComplete();
		}

		log.info("AutomaticInventory disabled.");
	}

	public static boolean hasPermission(Features feature, Player player) {
		var hasPermission = false;
		switch (feature) {
			case SortInventory:
				hasPermission = player.hasPermission("automaticinventory.sortinventory");
				break;
			case SortChests:
				hasPermission = player.hasPermission("automaticinventory.sortchests");
				break;
			case RefillStacks:
				hasPermission = player.hasPermission("automaticinventory.refillstacks");
				break;
			case QuickDeposit:
				hasPermission = player.hasPermission("automaticinventory.quickdeposit");
				break;
			case DepositAll:
				hasPermission = player.hasPermission("automaticinventory.depositall");
				break;
		}

		return hasPermission;
	}

	public static DepositRecord depositMatching(PlayerInventory source, Inventory destination, boolean depositHotbar) {
		var eligibleSignatures = new HashSet<String>();
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

			if (GlobalConfig.instance.config_noAutoDeposit.contains(sourceStack.getType())) {
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
