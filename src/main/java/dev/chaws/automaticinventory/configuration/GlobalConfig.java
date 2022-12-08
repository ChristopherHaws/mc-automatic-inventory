package dev.chaws.automaticinventory.configuration;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.common.LocationRange;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalConfig {
	public static GlobalConfig instance;

	public static boolean autosortEnabledByDefault = true;
	public static boolean quickDepositEnabledByDefault = true;
	public static boolean autoRefillEnabledByDefault = true;

	public Set<Material> autoRefillExcludedItems = new HashSet<>();
	public Set<Material> autoDepositExcludedItems = new HashSet<>();
	public List<String> excludeItemsContainingThisString;
	public LocationRange[] disallowedZones;

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

	public static void initialize(File dataFolder) {
		instance = new GlobalConfig();

		var configFile = new File(dataFolder.getPath() + File.separatorChar + "config.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		FileConfiguration outConfig = new YamlConfiguration();

		var autoRefillExcludedItemNames = config.getStringList("Auto Refill.Excluded Items");
		if (autoRefillExcludedItemNames.size() == 0) {
			autoRefillExcludedItemNames.add("AIR");
			autoRefillExcludedItemNames.add("POTION");
		}

		for (var itemName : autoRefillExcludedItemNames) {
			var material = Material.matchMaterial(itemName.toUpperCase());
			if (material == null) {
				AutomaticInventory.log.warning(itemName + " is not a valid material");
			} else {
				instance.autoRefillExcludedItems.add(material);
			}
		}

		outConfig.set("Auto Refill.Excluded Items", autoRefillExcludedItemNames);

		var autoDepositExcludedItemNames = config.getStringList("Auto Deposit.Excluded Items");
		if (autoDepositExcludedItemNames.size() == 0) {
			autoDepositExcludedItemNames.add("AIR");
			autoDepositExcludedItemNames.add("ARROW");
			autoDepositExcludedItemNames.add("SPECTRAL_ARROW");
			autoDepositExcludedItemNames.add("TIPPED_ARROW");
		}

		for (var itemName : autoDepositExcludedItemNames) {
			var material = Material.matchMaterial(itemName.toUpperCase());
			if (material == null) {
				AutomaticInventory.log.warning(itemName + " is not a valid material");
			} else {
				instance.autoDepositExcludedItems.add(material);
			}
		}

		outConfig.set("Auto Deposit.Excluded Items", autoDepositExcludedItemNames);

		autosortEnabledByDefault = config.getBoolean("autosortEnabledByDefault", true);
		outConfig.set("autosortEnabledByDefault", autosortEnabledByDefault);

		instance.excludeItemsContainingThisString = config.getStringList("excludeItemsContainingThisString");
		var legacyExcludedItem = config.getString("excludeItemsContainingThisString");
		if (legacyExcludedItem != null && !instance.excludeItemsContainingThisString.toString().equals(legacyExcludedItem)) {
			instance.excludeItemsContainingThisString.add(legacyExcludedItem);
		}
		outConfig.set("excludeItemsContainingThisString", instance.excludeItemsContainingThisString);

		var foo = new ArrayList<LocationRange>();
		var bar = config.getList("disallowedZones", foo);

		try {
			outConfig.save(configFile);
		} catch (IOException e) {
			AutomaticInventory.log.warning("Encountered an issue while writing to the configuration file.");
			e.printStackTrace();
		}
	}
}
