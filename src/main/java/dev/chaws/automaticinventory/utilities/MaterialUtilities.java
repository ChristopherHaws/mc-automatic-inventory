package dev.chaws.automaticinventory.utilities;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.HashSet;
import java.util.Set;

public class MaterialUtilities {
	public static boolean isChest(Material material) {
		if (material == null) {
			return false;
		}
		return switch (material) {
			case CHEST, TRAPPED_CHEST, BARREL -> true;
			default -> MaterialColorTag.SHULKER_BOX.isTagged(material);
		};
	}

	public static boolean isPassable(Material material) {
		if (material == null) {
			return false;
		}
		return switch (material) {
			case AIR, CHEST, TRAPPED_CHEST, HOPPER -> true;
			default -> Tag.WALL_SIGNS.isTagged(material)
				|| Tag.SIGNS.isTagged(material);
		};
	}
}

/**
 * Created on 10/5/2018.
 *
 * @author RoboMWM
 */
enum MaterialColorTag {
	TERRACOTTA,
	SHULKER_BOX;

	MaterialColorTag() {
		for (var material : Material.values()) {
			if (material.name().equals(this.name())) {
				materials.add(material);
			} else {
				for (var color : DyeColor.values()) {
					if (material.name().equals(color.name() + "_" + this.name())) {
						materials.add(material);
					}
				}
			}
		}
	}

	private final Set<Material> materials = new HashSet<>();

	public boolean isTagged(Material material) {
		return materials.contains(material);
	}
}
