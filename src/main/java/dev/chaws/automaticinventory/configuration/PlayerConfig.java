package dev.chaws.automaticinventory.configuration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerConfig {
	private final static String METADATA_TAG = "AI_PlayerData";
	private static File playerConfigFolder;

	private final File playerConfigFile;
	private final String playerName;
	private final UUID playerID;

	private Thread loadingThread;
	private Thread savingThread;

	private boolean receivedChestSortInfo = false;
	private boolean receivedInventorySortInfo = false;
	private boolean receivedRestackInfo = false;
	private boolean usedQuickDeposit = false;
	private boolean gotQuickDepositInfo = false;
	private boolean receivedDepositAllInfo = false;
	private boolean usedDepositAll = false;
	public int firstEmptySlot = -1;
	private boolean isDirty = false;

	private PlayerConfig(Player player) {
		this.playerName = player.getName();
		this.playerID = player.getUniqueId();
		this.playerConfigFile = new File(PlayerConfig.playerConfigFolder.getPath() + File.separator + this.playerID);
		this.loadingThread = new Thread(new DataLoader());
		this.loadingThread.start();
		player.setMetadata(METADATA_TAG, new FixedMetadataValue(AutomaticInventory.instance, this));
	}

	public static void initialize(File playerConfigFolder) {
		PlayerConfig.playerConfigFolder = playerConfigFolder;
	}

	public static void initializePlayer(Player player) {
		new PlayerConfig(player);
	}

	public static PlayerConfig fromPlayer(Player player) {
		var data = player.getMetadata(METADATA_TAG);
		if (data.isEmpty()) {
			return new PlayerConfig(player);
		} else {
			return (PlayerConfig) data.get(0).value();
		}
	}

	public boolean hasUsedQuickDeposit() {
		return usedQuickDeposit;
	}

	public void setUsedQuickDeposit(boolean usedQuickDeposit) {
		this.usedQuickDeposit = usedQuickDeposit;
		this.isDirty = true;
	}

	public boolean hasUsedDepositAll() {
		return usedDepositAll;
	}

	public void setUsedDepositAll(boolean usedDepositAll) {
		this.usedDepositAll = usedDepositAll;
		this.isDirty = true;
	}

	public boolean hasReceivedChestSortInfo() {
		return receivedChestSortInfo;
	}

	public void setReceivedChestSortInfo(boolean receivedChestSortInfo) {
		this.receivedChestSortInfo = receivedChestSortInfo;
		this.isDirty = true;
	}

	public boolean hasReceivedInventorySortInfo() {
		return receivedInventorySortInfo;
	}

	public void setReceivedInventorySortInfo(boolean receivedInventorySortInfo) {
		this.receivedInventorySortInfo = receivedInventorySortInfo;
		this.isDirty = true;
	}

	public boolean hasReceivedRestackInfo() {
		return receivedRestackInfo;
	}

	public void setReceivedRestackInfo(boolean receivedRestackInfo) {
		this.receivedRestackInfo = receivedRestackInfo;
		this.isDirty = true;
	}

	public static boolean featureEnabled(Features feature, Player player) {
		if (!AutomaticInventory.hasPermission(feature, player)) {
			return false;
		}

		var playerConfig = PlayerConfig.fromPlayer(player);

		return switch (feature) {
			case SortInventory -> playerConfig.isSortInventory();
			case SortChests -> playerConfig.isSortChests();
			case QuickDeposit -> playerConfig.isQuickDepositEnabled();
			case RefillStacks -> playerConfig.isAutoRefillEnabled();
			case DepositAll -> true;
		};
	}

	private boolean sortChests = GlobalConfig.autosortEnabledByDefault;

	public boolean isSortChests() {
		this.waitForLoadComplete();
		return sortChests;
	}

	public void setSortChests(boolean sortChests) {
		this.isDirty = true;
		this.sortChests = sortChests;
	}

	private boolean sortInventory = GlobalConfig.autosortEnabledByDefault;

	public boolean isSortInventory() {
		this.waitForLoadComplete();
		return sortInventory;
	}

	public void setSortInventory(boolean sortInventory) {
		this.isDirty = true;
		this.sortInventory = sortInventory;
	}

	private boolean quickDepositEnabled = GlobalConfig.quickDepositEnabledByDefault;

	public boolean isQuickDepositEnabled() {
		this.waitForLoadComplete();
		return quickDepositEnabled;
	}

	public void setQuickDepositEnabled(boolean quickDepositEnabled) {
		this.isDirty = true;
		this.quickDepositEnabled = quickDepositEnabled;
	}

	private boolean autoRefillEnabled = GlobalConfig.autoRefillEnabledByDefault;

	public boolean isAutoRefillEnabled() {
		this.waitForLoadComplete();
		return autoRefillEnabled;
	}

	public void setAutoRefillEnabled(boolean autoRefillEnabled) {
		this.isDirty = true;
		this.autoRefillEnabled = autoRefillEnabled;
	}

	public boolean isGotQuickDepositInfo() {
		return gotQuickDepositInfo;
	}

	public void setGotQuickDepositInfo(boolean newValue) {
		this.gotQuickDepositInfo = newValue;
	}

	public void saveChanges() {
		if (!this.isDirty) {
			return;
		}

		this.waitForLoadComplete();
		this.savingThread = new Thread(new DataSaver());
		this.savingThread.start();
	}

	private void waitForLoadComplete() {
		if (this.loadingThread != null) {
			try {
				this.loadingThread.join();
			} catch (InterruptedException ignored) {
			}
			this.loadingThread = null;
		}
	}

	public void waitForSaveComplete() {
		if (this.savingThread != null) {
			try {
				this.savingThread.join();
			} catch (InterruptedException ignored) {
			}
		}
	}

	private void writeDataToFile() {
		try {
			FileConfiguration config = new YamlConfiguration();
			config.set("Player Name", this.playerName);
			config.set("Sort Chests", this.sortChests);
			config.set("Sort Personal Inventory", this.sortInventory);
			config.set("Quick Deposit Enabled", this.quickDepositEnabled);
			config.set("Auto Refill Enabled", this.autoRefillEnabled);
			config.set("Used Quick Deposit", this.usedQuickDeposit);
			config.set("Received Messages.Personal Inventory", this.receivedInventorySortInfo);
			config.set("Received Messages.Chest Inventory", this.receivedChestSortInfo);
			config.set("Received Messages.Restacker", this.receivedRestackInfo);
			config.set("Received Messages.Deposit All", this.receivedDepositAllInfo);

			config.save(this.playerConfigFile);
		} catch (Exception e) {
			var errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			AutomaticInventory.log.info("Failed to save player data for " + playerID + " " + errors);
		}

		this.savingThread = null;
		this.isDirty = false;
	}

	private void readDataFromFile() {
		if (!this.playerConfigFile.exists()) {
			return;
		}

		var needRetry = false;
		var retriesRemaining = 5;
		Exception latestException = null;
		do {
			try {
				needRetry = false;
				FileConfiguration config = YamlConfiguration.loadConfiguration(this.playerConfigFile);
				this.sortChests = config.getBoolean("Sort Chests", GlobalConfig.autosortEnabledByDefault);
				this.sortInventory = config.getBoolean("Sort Personal Inventory",
						GlobalConfig.autosortEnabledByDefault);
				this.quickDepositEnabled = config.getBoolean("Quick Deposit Enabled",
						GlobalConfig.quickDepositEnabledByDefault);
				this.autoRefillEnabled = config.getBoolean("Auto Refill Enabled",
						GlobalConfig.autoRefillEnabledByDefault);
				this.usedQuickDeposit = config.getBoolean("Used Quick Deposit", false);
				this.receivedChestSortInfo = config.getBoolean("Received Messages.Chest Inventory", false);
				this.receivedInventorySortInfo = config.getBoolean("Received Messages.Personal Inventory", false);
				this.receivedRestackInfo = config.getBoolean("Received Messages.Restacker", false);
				this.receivedDepositAllInfo = config.getBoolean("Received Messages.Deposit All", false);
			}

			// if there's any problem with the file's content, retry up to 5 times with 5
			// milliseconds between
			catch (Exception e) {
				latestException = e;
				needRetry = true;
				retriesRemaining--;
			}

			try {
				if (needRetry) {
					Thread.sleep(5);
				}
			} catch (InterruptedException ignored) {
			}

		} while (needRetry && retriesRemaining >= 0);

		// if last attempt failed, log information about the problem
		if (needRetry) {
			var errors = new StringWriter();
			if (latestException != null)
				latestException.printStackTrace(new PrintWriter(errors));
			AutomaticInventory.log.info("Failed to load data for " + playerID + " " + errors);
		}
	}

	private class DataSaver implements Runnable {
		@Override
		public void run() {
			writeDataToFile();
		}
	}

	private class DataLoader implements Runnable {
		@Override
		public void run() {
			readDataFromFile();
		}
	}

	public boolean isReceivedDepositAllInfo() {
		return this.receivedDepositAllInfo;
	}

	public void setReceivedDepositAllInfo(boolean status) {
		this.receivedDepositAllInfo = status;
		this.isDirty = true;
	}
}
