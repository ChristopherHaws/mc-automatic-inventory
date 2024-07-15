package dev.chaws.automaticinventory.utilities;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemUtilities {
	public static String getSignature(ItemStack stack) {
		var signature = stack.getType().name();
		if (stack.getMaxStackSize() > 1) {
			var data = stack.getData();
			if (data != null) {
				// getData will not actually be deprecated according to the spigot forums
				//noinspection deprecation
				signature += "." + String.valueOf(data.getData());
			}
		}

		var meta = stack.getItemMeta();
		if (meta != null && meta.hasDisplayName()) {
			// Append the name of the item is there is a custom name given to it
			signature += "." + meta.getDisplayName();
		}

		//differentiate potion types. Original credit to pugabyte: https://github.com/Pugabyte/AutomaticInventory/commit/01bbdbfa0ea1bc7dc397fc8a8ff625f3f22e1ed6
		//Modified to use PotionType instead of PotionEffectType in signature
		if (stack.getType().toString().toLowerCase().contains("potion")) {
			var itemMeta = stack.getItemMeta();
			if (itemMeta == null) {
				return signature;
			}

			if (!(itemMeta instanceof PotionMeta potionMeta)) {
				return signature;
			}

			var potionType = potionMeta.getBasePotionType();
			if (potionType == null) {
				return signature;
			}

			signature += "." + potionType;
			if (potionType.isExtendable()) {
				signature += ".extendable";
			}
			if (potionType.isUpgradeable()) {
				signature += ".upgradable";
			}
		}

		return signature;
	}
}
