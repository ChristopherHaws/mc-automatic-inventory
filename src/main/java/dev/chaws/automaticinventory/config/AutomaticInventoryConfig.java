package dev.chaws.automaticinventory.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import org.bukkit.Material;

import java.util.Set;

@Configuration
public final class AutomaticInventoryConfig {
	@Comment("TODO")
	public Boolean autosortEnabledByDefault = true;
	@Comment("TODO")
	public Boolean quickDepositEnabledByDefault = true;
	@Comment("TODO")
	public Boolean autoRefillEnabledByDefault = true;

	@Comment("TODO")
	public Set<String> excludeItemsContainingThisString = Set.of();

	@Comment("Refill hotbar items when they are depleted")
	public AutoRefillConfig autoRefill = new AutoRefillConfig();

	@Comment("Deposit items from your inventory into chests with matching items within a 1 chunk radius")
	public AutoDepositConfig autoDeposit = new AutoDepositConfig();

	@Configuration
	public final class AutoRefillConfig {
		public Set<Material> excludedItems = Set.of(
			Material.AIR,
			Material.POTION
		);
	}

	@Configuration
	public final class AutoDepositConfig {
		public Set<Material> excludedItems = Set.of(
			Material.AIR,
			Material.ARROW,
			Material.SPECTRAL_ARROW,
			Material.TIPPED_ARROW,
			Material.SHULKER_BOX
		);
	}
}
