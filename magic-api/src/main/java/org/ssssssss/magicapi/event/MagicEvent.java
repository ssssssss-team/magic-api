package org.ssssssss.magicapi.event;

public class MagicEvent {

	private String type;

	private EventAction action;

	public MagicEvent(String type, EventAction action) {
		this.type = type;
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public EventAction getAction() {
		return action;
	}
}
