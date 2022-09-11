package dev.chaws.automaticinventory.messaging;

import dev.chaws.automaticinventory.messaging.Messages;

public class CustomizableMessage {
	public Messages id;
	public String text;
	public String notes;

	public CustomizableMessage(Messages id, String text, String notes) {
		this.id = id;
		this.text = text;
		this.notes = notes;
	}
}
