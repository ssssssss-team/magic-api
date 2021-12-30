package org.ssssssss.magicapi.event;

import org.ssssssss.magicapi.model.MagicEntity;

public class FileEvent extends MagicEvent {

	private final MagicEntity entity;

	public FileEvent(String type, EventAction action, MagicEntity entity) {
		super(type, action);
		this.entity = entity;
	}

	public FileEvent(String type, EventAction action, MagicEntity entity, String source) {
		super(type, action, source);
		this.entity = entity;
	}

	public MagicEntity getEntity() {
		return entity;
	}

}
