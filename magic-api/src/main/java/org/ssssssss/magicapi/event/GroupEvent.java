package org.ssssssss.magicapi.event;

import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.MagicEntity;

import java.util.Collections;
import java.util.List;

public class GroupEvent extends MagicEvent {

	private final Group group;

	private List<MagicEntity> entities = Collections.emptyList();

	public GroupEvent(String type, EventAction action, Group group) {
		super(type, action);
		this.group = group;
	}

	public GroupEvent(String type, EventAction action, Group group, List<MagicEntity> entities) {
		this(type, action, group);
		this.entities = entities;
	}

	public Group getGroup() {
		return group;
	}

	public List<MagicEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<MagicEntity> entities) {
		this.entities = entities;
	}
}
