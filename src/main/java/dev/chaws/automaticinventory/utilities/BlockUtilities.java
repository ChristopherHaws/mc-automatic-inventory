package dev.chaws.automaticinventory.utilities;

import org.bukkit.Material;

public class BlockUtilities {
	/**
	 * Function to check if a chest would open based only on its block above
	 *
	 * @param aboveBlockID the block above the ctest
	 * @return whether or not the chest would not open
	 */
	public static boolean preventsChestOpen(Material container, Material aboveBlockID) {
		if (container == Material.BARREL) {
			return false;
		}

		if (aboveBlockID == null) {
			return false;
		}
		return aboveBlockID.isOccluding();
	}
}
