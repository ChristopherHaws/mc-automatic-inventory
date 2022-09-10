package dev.chaws.automaticinventory.utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Chat {
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(message);
	}
}
