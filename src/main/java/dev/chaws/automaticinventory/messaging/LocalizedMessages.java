package dev.chaws.automaticinventory.messaging;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocalizedMessages {
	//in-memory cache for messages
	private String[] messages;

	private final static String dataLayerFolderPath = "plugins" + File.separator + "AutomaticInventory";
	public final static String playerConfigFolderPath = dataLayerFolderPath + File.separator + "PlayerConfig";
	public final static String messagesFilePath = dataLayerFolderPath + File.separator + "messages.yml";

	public LocalizedMessages() {
		//ensure data folders exist
		var playerConfigFolder = new File(playerConfigFolderPath);
		if (!playerConfigFolder.exists()) {
			playerConfigFolder.mkdirs();
		}

		this.loadMessages();
	}

	private void loadMessages() {
		var messageIDs = Messages.values();
		this.messages = new String[Messages.values().length];

		var defaults = new HashMap<String, CustomizableMessage>();

		//initialize defaults
		//this.addDefault(defaults, Messages.NoManagedWorld, "The PopulationDensity plugin has not been properly configured.  Please update your configuration.yml to specify a world to manage.", null);
		this.addDefault(defaults, Messages.NoPermissionForFeature, "You don't have permission to use that feature.", null);
		this.addDefault(defaults, Messages.ChestSortEnabled, "Now auto-sorting any chests you use.", null);
		this.addDefault(defaults, Messages.ChestSortDisabled, "Stopped auto-sorting chests you use.", null);
		this.addDefault(defaults, Messages.InventorySortEnabled, "Now auto-sorting your personal inventory.", null);
		this.addDefault(defaults, Messages.InventorySortDisabled, "Stopped auto-sorting your personal inventory.", null);
		this.addDefault(defaults, Messages.AutoSortHelp, "Options are /AutoSort Chests and /AutoSort Inventory.", null);
		this.addDefault(defaults, Messages.AutoRefillEducation, "AutomaticInventory(AI) will auto-replace broken tools and depleted hotbar stacks from your inventory.", null);
		this.addDefault(defaults, Messages.InventorySortEducation, "AutomaticInventory(AI) will keep your inventory sorted.  Use /AutoSort to disable.", null);
		this.addDefault(defaults, Messages.ChestSortEducation3, "AutomaticInventory(AI) will sort the contents of chests you access.  Use /AutoSort to toggle.  TIP: Want some chests sorted but not others?  Chests with names including an asterisk (*) won't auto-sort.  You can rename any chest using an anvil.", null);
		this.addDefault(defaults, Messages.SuccessfulDeposit2, "Deposited {0} items.", null);
		this.addDefault(defaults, Messages.FailedDepositNoMatch, "No items deposited - none of your inventory items match items in that chest.", null);
		this.addDefault(defaults, Messages.QuickDepositAdvertisement3, "Want to deposit quickly from your hotbar?  Just pick a specific chest and sneak (hold shift) while hitting it.", null);
		this.addDefault(defaults, Messages.FailedDepositChestFull2, "That chest is full.", null);
		this.addDefault(defaults, Messages.SuccessfulDepositAll2, "Deposited {0} items into nearby chests.", null);
		this.addDefault(defaults, Messages.ChestLidBlocked, "That chest isn't accessible.", null);
		this.addDefault(defaults, Messages.DepositAllAdvertisement, "TIP: Instantly deposit all items from your inventory into all the right nearby boxes with /DepositAll!", null);
		this.addDefault(defaults, Messages.QuickDepositHelp, "Options are /quickdeposit toggle, /quickdeposit enable, and /quickdeposit disable.", null);
		this.addDefault(defaults, Messages.QuickDepositEnabled, "Quick deposit enabled. Try crouching and clicking on a chest.", null);
		this.addDefault(defaults, Messages.QuickDepositDisabled, "Quick deposit disabled.", null);
		this.addDefault(defaults, Messages.AutoRefillHelp, "Options are /autorefill toggle, /autorefill enable, and /autorefill disable.", null);
		this.addDefault(defaults, Messages.AutoRefillEnabled, "Auto refill enabled.", null);
		this.addDefault(defaults, Messages.AutoRefillDisabled, "Auto refill disabled.", null);

		//load the configuration file
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(messagesFilePath));
		FileConfiguration outConfig = new YamlConfiguration();

		//for each message ID
		for (var i = 0; i < messageIDs.length; i++) {
			//get default for this message
			var messageID = messageIDs[i];
			var messageData = defaults.get(messageID.name());

			//if default is missing, log an error and use some fake data for now so that the plugin can run
			if (messageData == null) {
				AutomaticInventory.log.info("Missing message for " + messageID.name() + ".  Please contact the developer.");
				messageData = new CustomizableMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
			}

			//read the message from the file, use default if necessary
			this.messages[messageID.ordinal()] = config.getString("Messages." + messageID.name() + ".Text", messageData.text);
			outConfig.set("Messages." + messageID.name() + ".Text", this.messages[messageID.ordinal()]);

			//support formatting codes
			this.messages[messageID.ordinal()] = this.messages[messageID.ordinal()].replace('&', (char) 0x00A7);

			if (messageData.notes != null) {
				messageData.notes = config.getString("Messages." + messageID.name() + ".Notes", messageData.notes);
				outConfig.set("Messages." + messageID.name() + ".Notes", messageData.notes);
			}
		}

		//save any changes
		try {
			outConfig.options().header("Use a YAML editor like NotepadPlusPlus to edit this file.  \nAfter editing, back up your changes before reloading the server in case you made a syntax error.  \nUse ampersands (&) for formatting codes, which are documented here: http://minecraft.gamepedia.com/Formatting_codes");
			outConfig.save(LocalizedMessages.messagesFilePath);
		} catch (IOException exception) {
			AutomaticInventory.log.info("Unable to write to the configuration file at \"" + LocalizedMessages.messagesFilePath + "\"");
		}

		defaults.clear();
	}

	private void addDefault(HashMap<String, CustomizableMessage> defaults, Messages id, String text, String notes) {
		var message = new CustomizableMessage(id, text, notes);
		defaults.put(id.name(), message);
	}

	synchronized public String getMessage(Messages messageID, String... args) {
		var message = messages[messageID.ordinal()];

		for (var i = 0; i < args.length; i++) {
			var param = args[i];
			message = message.replace("{" + i + "}", param);
		}

		return message;
	}
}
