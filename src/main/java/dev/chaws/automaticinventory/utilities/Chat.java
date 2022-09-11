package dev.chaws.automaticinventory.utilities;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.messaging.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Chat {
	public static void sendMessage(Player player, ChatColor color, Messages messageID, String... args) {
		sendMessage(player, color, messageID, 0, args);
	}

	public static void sendMessage(Player player, ChatColor color, Messages messageID, long delayInTicks, String... args) {
		var message = AutomaticInventory.instance.localizedMessages.getMessage(messageID, args);
		sendMessage(player, color, message, delayInTicks);
	}

	public static void sendMessage(Player player, ChatColor color, String message) {
		if (message == null || message.length() == 0) {
			return;
		}

		if (player == null) {
			AutomaticInventory.log.info(color + message);
		} else {
			player.sendMessage(color + message);
		}
	}

	public static void sendMessage(Player player, ChatColor color, String message, long delayInTicks) {
		var task = new SendPlayerMessageTask(player, color, message);
		if (delayInTicks > 0) {
			AutomaticInventory.instance.getServer().getScheduler().runTaskLater(AutomaticInventory.instance, task, delayInTicks);
		} else {
			task.run();
		}
	}
}

//sends a message to a player
//used to send delayed messages, for example help text triggered by a player's chat
class SendPlayerMessageTask implements Runnable {
	private final Player player;
	private final ChatColor color;
	private final String message;

	public SendPlayerMessageTask(Player player, ChatColor color, String message) {
		this.player = player;
		this.color = color;
		this.message = message;
	}

	@Override
	public void run() {
		if (player == null) {
			AutomaticInventory.log.info(color + message);
			return;
		}

		Chat.sendMessage(this.player, this.color, this.message);
	}
}
