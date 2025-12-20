package dev.chaws.automaticinventory.messaging;

import dev.chaws.automaticinventory.AutomaticInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalizedMessages {
	public static LocalizedMessages instance = new LocalizedMessages();

	private String[] messages;

	private LocalizedMessages() { }

	public static void initialize(File dataFolder) {
		var localizationFile = new File(dataFolder.getPath() + File.separator + "messages.yml");
		var messageIDs = Messages.values();
		instance.messages = new String[messageIDs.length];

		var defaults = new HashMap<String, LocalizedMessage>();

		//initialize defaults
		//this.addDefault(defaults, Messages.NoManagedWorld, "The PopulationDensity plugin has not been properly configured.  Please update your configuration.yml to specify a world to manage.", null);
		instance.addDefault(defaults, Messages.NoPermissionForFeature, "You don't have permission to use that feature.");
		instance.addDefault(defaults, Messages.ChestSortEnabled, "Now auto-sorting any chests you use.");
		instance.addDefault(defaults, Messages.ChestSortDisabled, "Stopped auto-sorting chests you use.");
		instance.addDefault(defaults, Messages.InventorySortEnabled, "Now auto-sorting your personal inventory.");
		instance.addDefault(defaults, Messages.InventorySortDisabled, "Stopped auto-sorting your personal inventory.");
		instance.addDefault(defaults, Messages.AutoSortHelp, "Options are /AutoSort Chests and /AutoSort Inventory.");
		instance.addDefault(defaults, Messages.AutoRefillEducation, "AutomaticInventory(AI) will auto-replace broken tools and depleted hotbar stacks from your inventory.");
		instance.addDefault(defaults, Messages.InventorySortEducation, "AutomaticInventory(AI) will keep your inventory sorted.  Use /AutoSort to disable.");
		instance.addDefault(defaults, Messages.ChestSortEducation3, "AutomaticInventory(AI) will sort the contents of chests you access.  Use /AutoSort to toggle.  TIP: Want some chests sorted but not others?  Chests with names including an asterisk (*) won't auto-sort.  You can rename any chest using an anvil.");
		instance.addDefault(defaults, Messages.SuccessfulDeposit2, "Deposited {0} items.");
		instance.addDefault(defaults, Messages.FailedDepositNoMatch, "No items deposited - none of your inventory items match items in that chest.");
		instance.addDefault(defaults, Messages.QuickDepositAdvertisement3, "Want to deposit quickly from your hotbar?  Just pick a specific chest and sneak (hold shift) while hitting it.");
		instance.addDefault(defaults, Messages.FailedDepositChestFull2, "That chest is full.");
		instance.addDefault(defaults, Messages.SuccessfulDepositAll2, "Deposited {0} items into nearby chests.");
		instance.addDefault(defaults, Messages.ChestLidBlocked, "That chest isn't accessible.");
		instance.addDefault(defaults, Messages.DepositAllAdvertisement, "TIP: Instantly deposit all items from your inventory into all the right nearby boxes with /DepositAll!");
		instance.addDefault(defaults, Messages.QuickDepositHelp, "Options are /quickdeposit toggle, /quickdeposit enable, and /quickdeposit disable.");
		instance.addDefault(defaults, Messages.QuickDepositEnabled, "Quick deposit enabled. Try crouching and clicking on a chest.");
		instance.addDefault(defaults, Messages.QuickDepositDisabled, "Quick deposit disabled.");
		instance.addDefault(defaults, Messages.AutoRefillHelp, "Options are /autorefill toggle, /autorefill enable, and /autorefill disable.");
		instance.addDefault(defaults, Messages.AutoRefillEnabled, "Auto refill enabled.");
		instance.addDefault(defaults, Messages.AutoRefillDisabled, "Auto refill disabled.");
		instance.addDefault(defaults, Messages.StartingDepositAll, "Checking for items to deposit.. This may take a moment.");

		//load the configuration file
		FileConfiguration config = YamlConfiguration.loadConfiguration(localizationFile);
		FileConfiguration outConfig = new YamlConfiguration();

		//for each message ID
		for (var messageID : messageIDs) {
			//get default for this message
			var messageData = defaults.get(messageID.name());

			//if default is missing, log an error and use some fake data for now so that the plugin can run
			if (messageData == null) {
				AutomaticInventory.log.info("Missing message for " + messageID.name() + ".  Please contact the developer.");
				messageData = new LocalizedMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
			}

			//read the message from the file, use default if necessary
			instance.messages[messageID.ordinal()] = config.getString("Messages." + messageID.name() + ".Text", messageData.text);
			outConfig.set("Messages." + messageID.name() + ".Text", instance.messages[messageID.ordinal()]);

			//support formatting codes
			instance.messages[messageID.ordinal()] = instance.messages[messageID.ordinal()].replace('&', (char) 0x00A7);

			if (messageData.notes != null) {
				messageData.notes = config.getString("Messages." + messageID.name() + ".Notes", messageData.notes);
				outConfig.set("Messages." + messageID.name() + ".Notes", messageData.notes);
			}
		}

		//save any changes
		try {
			var header = new ArrayList<String>();
			header.add("Use a YAML editor like NotepadPlusPlus to edit this file.");
			header.add("After editing, back up your changes before reloading the server in case you made a syntax error.");
			header.add("Use ampersands (&) for formatting codes, which are documented here: http://minecraft.gamepedia.com/Formatting_codes");
			outConfig.options().setHeader(header);
			outConfig.save(localizationFile);
		} catch (IOException exception) {
			AutomaticInventory.log.info("Unable to write to the configuration file at \"" + localizationFile.getPath() + "\"");
		}

		defaults.clear();
	}

	private void addDefault(HashMap<String, LocalizedMessage> defaults, Messages id, String text) {
		var message = new LocalizedMessage(id, text, null);
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

	private static class LocalizedMessage {
		public String text;
		public String notes;

		public LocalizedMessage(Messages id, String text, String notes) {
			this.text = text;
			this.notes = notes;
		}
	}
}
