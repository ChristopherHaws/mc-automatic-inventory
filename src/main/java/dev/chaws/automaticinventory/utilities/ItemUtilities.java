package dev.chaws.automaticinventory.utilities;

import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemUtilities {
	public static String getSignature(ItemStack stack) {
		var signature = stack.getType().name();
		if (stack.getMaxStackSize() > 1) {
			// getData will not actually be removed according to the spigot forums
            //noinspection removal
            var data = stack.getData();
			if (data != null) {
				// getData will not actually be removed according to the spigot forums
				//noinspection deprecation
				signature += "." + String.valueOf(data.getData());
			}
		}

		var meta = stack.getItemMeta();
		if (meta != null && meta.hasDisplayName()) {
			// Append the name of the item is there is a custom name given to it
			signature += "." + meta.displayName();
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

	/**
	 * Returns the content signature of a shulker box if it is:
	 * - Completely full (all 27 slots occupied)
	 * - Every stack is at max size (64/64, 16/16, 1/1, etc.)
	 * - Contains only one item type
	 *
	 * @param shulkerBox The shulker box item to check
	 * @return The signature of the contained item, or null if conditions are not met
	 */
	public static String getShulkerContentSignature(ItemStack shulkerBox) {
		if (!MaterialUtilities.isShulkerBox(shulkerBox.getType())) {
			return null;
		}

		var meta = shulkerBox.getItemMeta();
		if (!(meta instanceof BlockStateMeta blockMeta)) {
			return null;
		}

		var state = blockMeta.getBlockState();
		if (!(state instanceof ShulkerBox shulker)) {
			return null;
		}

		var inventory = shulker.getInventory();
		String contentSignature = null;

		for (int i = 0; i < inventory.getSize(); i++) {
			var item = inventory.getItem(i);

			// Slot must be occupied
			if (item == null || item.getType().isAir()) {
				return null;
			}

			// Stack must be full (64/64, 16/16, 1/1, etc.)
			if (item.getAmount() != item.getMaxStackSize()) {
				return null;
			}

			var sig = getSignature(item);
			if (contentSignature == null) {
				contentSignature = sig;
			} else if (!contentSignature.equals(sig)) {
				return null; // Different items
			}
		}

		return contentSignature;
	}
}
