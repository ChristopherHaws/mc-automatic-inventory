package dev.chaws.automaticinventory.utilities;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;

public class BlockUtilities {
	public static boolean isContainer(Block block) {
		if (block.getState() instanceof Chest) {
			return true;
		}

		if (block.getState() instanceof ShulkerBox) {
			return true;
		}

		if (block.getState() instanceof Barrel) {
			return true;
		}

		return false;
	}

	/**
	 * Function to check if a chest would open based only on its block above
	 *
	 * @param aboveBlock the block above the ctest
	 * @return whether the chest would not open
	 */
	public static boolean isOpenable(Material container, Material aboveBlock) {
		// This functions might make it into spigot at some point:
		// https://hub.spigotmc.org/jira/browse/SPIGOT-5070
		if (container == Material.BARREL) {
			return true;
		}

		if (MaterialUtilities.isShulkerBox(container)) {
			// Shulker boxes can face in more directions that UP. We should check the block
			// based on the facing direction, but for now just return true to make this work.
			return true;
		}

		return !aboveBlock.isOccluding();
	}
}
