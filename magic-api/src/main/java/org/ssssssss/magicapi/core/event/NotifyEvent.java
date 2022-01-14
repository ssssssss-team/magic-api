package org.ssssssss.magicapi.core.event;

import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.model.MagicNotify;

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
