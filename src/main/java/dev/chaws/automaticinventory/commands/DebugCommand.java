package dev.chaws.automaticinventory.commands;

import dev.chaws.automaticinventory.configuration.PlayerConfig;
import org.bukkit.entity.Player;

public class DebugCommand implements IAutomaticInventoryCommand {
	@Override
	public boolean execute(Player player, PlayerConfig playerConfig, String[] args) {
		var inventory = player.getInventory();
		inventory.getItemInMainHand().setDurability(Short.MAX_VALUE);

//		for(int i = 0; i < inventory.getSize(); i++)
//		{
//			ItemStack stack = inventory.getItem(i);
//			if(stack != null)
//			AutomaticInventory.AddLogEntry(String.valueOf(i) + " : " + stack.getType().name());
//		}

		return true;
	}
}
