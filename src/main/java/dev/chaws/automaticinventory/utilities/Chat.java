package dev.chaws.automaticinventory.utilities;

import dev.chaws.automaticinventory.AutomaticInventory;
import dev.chaws.automaticinventory.messaging.LocalizedMessages;
import dev.chaws.automaticinventory.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

public class Chat {
	public static void broadcastMessage(NamedTextColor color, Messages messageID, String... args) {
		broadcastMessage(color, messageID, 0, args);
	}

	public static void broadcastMessage(NamedTextColor color, Messages messageID, long delayInTicks, String... args) {
		var message = LocalizedMessages.instance.getMessage(messageID, args);
		broadcastMessage(color, message, delayInTicks);
	}

	public static void broadcastMessage(NamedTextColor color, String message) {
		if (message == null || message.length() == 0) {
			return;
		}

		broadcastMessage(color, message, 0);
	}

	public static void broadcastMessage(NamedTextColor color, String message, long delayInTicks) {
		var task = new BroadcastMessageTask(color, message);
		if (delayInTicks > 0) {
			AutomaticInventory.instance.getServer().getScheduler().runTaskLater(AutomaticInventory.instance, task,
					delayInTicks);
		} else {
			task.run();
		}
	}

	public static void sendMessage(Player player, NamedTextColor color, Messages messageID, String... args) {
		sendMessage(player, color, messageID, 0, args);
	}

	public static void sendMessage(Player player, NamedTextColor color, Messages messageID, long delayInTicks,
			String... args) {
		var message = LocalizedMessages.instance.getMessage(messageID, args);
		sendMessage(player, color, message, delayInTicks);
	}

	public static void sendMessage(Player player, NamedTextColor color, String message) {
		if (message == null || message.length() == 0) {
			return;
		}

		sendMessage(player, color, message, 0);
	}

	public static void sendMessage(Player player, NamedTextColor color, String message, long delayInTicks) {
		var task = new SendPlayerMessageTask(player, color, message);
		if (delayInTicks > 0) {
			AutomaticInventory.instance.getServer().getScheduler().runTaskLater(AutomaticInventory.instance, task,
					delayInTicks);
		} else {
			task.run();
		}
	}
}

// sends a message to a player
// used to send delayed messages, for example help text triggered by a player's
// chat
class SendPlayerMessageTask implements Runnable {
	private final Player player;
	private final NamedTextColor color;
	private final String message;

	public SendPlayerMessageTask(Player player, NamedTextColor color, String message) {
		this.player = player;
		this.color = color;
		this.message = message;
	}

	@Override
	public void run() {
		if (player == null) {
			AutomaticInventory.log.info(color + message);
		} else {
			player.sendMessage(Component.text(message).color(color));
		}
	}
}

class BroadcastMessageTask implements Runnable {
	private final NamedTextColor color;
	private final String message;

	public BroadcastMessageTask(NamedTextColor color, String message) {
		this.color = color;
		this.message = message;
	}

	@Override
	public void run() {
		AutomaticInventory.instance.getServer().sendMessage(Component.text(message).color(color));
	}
}
