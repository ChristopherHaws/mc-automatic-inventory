package dev.chaws.automaticinventory.configuration;

import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalConfig {
	public static GlobalConfig instance;

	public static boolean autosortEnabledByDefault = true;
	public static boolean quickDepositEnabledByDefault = true;
	public static boolean autoRefillEnabledByDefault = true;

	public Set<Material> config_noAutoRefill = new HashSet<>();
	public Set<Material> config_noAutoDeposit = new HashSet<>();
	public List<String> excludeItemsContainingThisString;

	public boolean isItemExcludedViaName(ItemStack itemStack) {
		if (this.excludeItemsContainingThisString.isEmpty()) {
			return false;
		}

		var meta = itemStack.getItemMeta();
		if (meta == null || !meta.hasDisplayName()) {
			return false;
		}

		var name = meta.getDisplayName();
		return this.excludeItemsContainingThisString.stream().anyMatch(name::contains);
	}

	public static void Initialize(File dataFolder) {
		instance = new GlobalConfig();

		//read configuration settings (note defaults)
		if (!dataFolder.mkdirs()) {
			AutomaticInventory.log.warning("Could not create configuration directory.");
		}

		var configFile = new File(dataFolder.getPath() + File.separatorChar + "config.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		FileConfiguration outConfig = new YamlConfiguration();

		var noAutoRefillIDs_string = config.getStringList("Auto Refill.Excluded Items");
		if (noAutoRefillIDs_string.size() == 0) {
			noAutoRefillIDs_string.add("AIR");
			noAutoRefillIDs_string.add("POTION");
		}

		for (var idString : noAutoRefillIDs_string) {
			var material = Material.matchMaterial(idString.toUpperCase());
			if (material == null) {
				AutomaticInventory.log.warning(idString + " is not a valid material");
			} else {
				instance.config_noAutoRefill.add(material);
			}
		}

		outConfig.set("Auto Refill.Excluded Items", noAutoRefillIDs_string);

		var noAutoDepositIDs_string = config.getStringList("Auto Deposit.Excluded Items");
		if (noAutoDepositIDs_string.size() == 0) {
			noAutoDepositIDs_string.add("AIR");
			noAutoDepositIDs_string.add("ARROW");
			noAutoDepositIDs_string.add("SPECTRAL_ARROW");
			noAutoDepositIDs_string.add("TIPPED_ARROW");
		}

		for (var idString : noAutoDepositIDs_string) {
			var material = Material.matchMaterial(idString.toUpperCase());
			if (material == null) {
				AutomaticInventory.log.warning(idString + " is not a valid material");
			} else {
				instance.config_noAutoDeposit.add(material);
			}
		}

		outConfig.set("Auto Deposit.Excluded Items", noAutoDepositIDs_string);

		autosortEnabledByDefault = config.getBoolean("autosortEnabledByDefault", true);
		outConfig.set("autosortEnabledByDefault", autosortEnabledByDefault);

		instance.excludeItemsContainingThisString = config.getStringList("excludeItemsContainingThisString");
		var legacyExcludedItem = config.getString("excludeItemsContainingThisString");
		if (legacyExcludedItem != null && !instance.excludeItemsContainingThisString.toString().equals(legacyExcludedItem)) {
			instance.excludeItemsContainingThisString.add(legacyExcludedItem);
		}
		outConfig.set("excludeItemsContainingThisString", instance.excludeItemsContainingThisString);

		try {
			outConfig.save(configFile);
		} catch (IOException e) {
			AutomaticInventory.log.warning("Encountered an issue while writing to the configuration file.");
			e.printStackTrace();
		}
	}
}
