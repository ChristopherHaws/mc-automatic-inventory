package dev.chaws.automaticinventory.commands;

import dev.chaws.automaticinventory.configuration.PlayerConfig;
import org.bukkit.entity.Player;

public interface IAutomaticInventoryCommand {
	boolean execute(Player player, PlayerConfig playerConfig, String[] args);
}
