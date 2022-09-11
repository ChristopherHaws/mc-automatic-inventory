package dev.chaws.automaticinventory.configuration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.messaging.LocalizedMessages;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerConfig {
	private final static String METADATA_TAG = "AI_PlayerData";
	private Thread loadingThread;
	private Thread savingThread;
	private String playerName;
	private boolean gotChestSortInfo = false;
	private boolean gotInventorySortInfo = false;
	private boolean gotRestackInfo = false;
	private boolean usedQuickDeposit = false;
	private int manualDepositsThisSession = 0;
	private boolean gotQuickDepositInfo = false;
	private boolean gotDepositAllInfo = false;
	private boolean usedDepositAll = false;

	public boolean isUsedQuickDeposit() {
		return usedQuickDeposit;
	}

	public void setUsedQuickDeposit(boolean usedQuickDeposit) {
		this.usedQuickDeposit = usedQuickDeposit;
		this.isDirty = true;
	}

	public boolean isUsedDepositAll() {
		return usedDepositAll;
	}

	public void setUsedDepositAll(boolean usedDepositAll) {
		this.usedDepositAll = usedDepositAll;
		this.isDirty = true;
	}

	public boolean isGotChestSortInfo() {
		return gotChestSortInfo;
	}

	public void setGotChestSortInfo(boolean gotChestSortInfo) {
		this.gotChestSortInfo = gotChestSortInfo;
		this.isDirty = true;
	}

	public boolean isGotInventorySortInfo() {
		return gotInventorySortInfo;
	}

	public void setGotInventorySortInfo(boolean gotInventorySortInfo) {
		this.gotInventorySortInfo = gotInventorySortInfo;
		this.isDirty = true;
	}

	public boolean isGotRestackInfo() {
		return gotRestackInfo;
	}

	public void setGotRestackInfo(boolean gotRestackInfo) {
		this.gotRestackInfo = gotRestackInfo;
		this.isDirty = true;
	}

	private UUID playerID;

	public static void Preload(Player player) {
		new PlayerConfig(player);
	}

	public static PlayerConfig FromPlayer(Player player) {
		var data = player.getMetadata(METADATA_TAG);
		if (data == null || data.isEmpty()) {
			return new PlayerConfig(player);
		} else {
			var playerConfig = (PlayerConfig) (data.get(0).value());
			return playerConfig;
		}
	}

	private PlayerConfig(Player player) {
		this.playerName = player.getName();
		this.playerID = player.getUniqueId();
		this.loadingThread = new Thread(new DataLoader());
		this.loadingThread.start();
		player.setMetadata(METADATA_TAG, new FixedMetadataValue(AutomaticInventory.instance, this));
	}

	public int firstEmptySlot = -1;

	private boolean isDirty = false;

	public static boolean featureEnabled(Features feature, Player player) {
		if (!AutomaticInventory.hasPermission(feature, player)) {
			return false;
		}

		var playerConfig = PlayerConfig.FromPlayer(player);

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

	public void incrementManualDeposits() {
		this.manualDepositsThisSession++;
	}

	public int getManualDeposits() {
		return this.manualDepositsThisSession;
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
			} catch (InterruptedException e) {
			}
			this.loadingThread = null;
		}
	}

	public void waitForSaveComplete() {
		if (this.savingThread != null) {
			try {
				this.savingThread.join();
			} catch (InterruptedException e) {
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
			config.set("Received Messages.Personal Inventory", this.gotInventorySortInfo);
			config.set("Received Messages.Chest Inventory", this.gotChestSortInfo);
			config.set("Received Messages.Restacker", this.gotRestackInfo);
			config.set("Received Messages.Deposit All", this.gotDepositAllInfo);
			var playerFile = new File(LocalizedMessages.playerConfigFolderPath + File.separator + this.playerID.toString());
			config.save(playerFile);
		} catch (Exception e) {
			var errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			AutomaticInventory.log.info("Failed to save player data for " + playerID + " " + errors.toString());
		}

		this.savingThread = null;
		this.isDirty = false;
	}

	private void readDataFromFile() {
		var playerFile = new File(LocalizedMessages.playerConfigFolderPath + File.separator + this.playerID.toString());

		//if it exists as a file, read the file
		if (playerFile.exists()) {
			var needRetry = false;
			var retriesRemaining = 5;
			Exception latestException = null;
			do {
				try {
					needRetry = false;
					FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
					this.sortChests = config.getBoolean("Sort Chests", GlobalConfig.autosortEnabledByDefault);
					this.sortInventory = config.getBoolean("Sort Personal Inventory", GlobalConfig.autosortEnabledByDefault);
					this.quickDepositEnabled = config.getBoolean("Quick Deposit Enabled", GlobalConfig.quickDepositEnabledByDefault);
					this.autoRefillEnabled = config.getBoolean("Auto Refill Enabled", GlobalConfig.autoRefillEnabledByDefault);
					this.usedQuickDeposit = config.getBoolean("Used Quick Deposit", false);
					this.gotChestSortInfo = config.getBoolean("Received Messages.Chest Inventory", false);
					this.gotInventorySortInfo = config.getBoolean("Received Messages.Personal Inventory", false);
					this.gotRestackInfo = config.getBoolean("Received Messages.Restacker", false);
					this.gotDepositAllInfo = config.getBoolean("Received Messages.Deposit All", false);
				}

				//if there's any problem with the file's content, retry up to 5 times with 5 milliseconds between
				catch (Exception e) {
					latestException = e;
					needRetry = true;
					retriesRemaining--;
				}

				try {
                    if (needRetry) {
                        Thread.sleep(5);
                    }
				} catch (InterruptedException exception) {
				}

			} while (needRetry && retriesRemaining >= 0);

			//if last attempt failed, log information about the problem
			if (needRetry) {
				var errors = new StringWriter();
				latestException.printStackTrace(new PrintWriter(errors));
				AutomaticInventory.log.info("Failed to load data for " + playerID + " " + errors.toString());
			}
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

	public boolean isGotDepositAllInfo() {
		return this.gotDepositAllInfo;
	}

	public void setGotDepositAllInfo(boolean status) {
		this.gotDepositAllInfo = status;
		this.isDirty = true;
	}
}
