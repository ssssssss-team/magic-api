package org.ssssssss.magicapi.event;

import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.MagicNotify;

public class NotifyEvent extends MagicEvent {

	private String id;

	public NotifyEvent(MagicNotify notify) {
		super(notify.getType(), notify.getAction(), Constants.EVENT_SOURCE_NOTIFY);
		this.id = notify.getId();
	}

	public String getId() {
		return id;
	}
}
