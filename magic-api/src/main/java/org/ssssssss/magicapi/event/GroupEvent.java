package org.ssssssss.magicapi.event;

import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.MagicEntity;

import java.util.Collections;
import java.util.List;

public class GroupEvent extends MagicEvent {

	/**
	 * 分组信息
	 */
	private final Group group;

	/**
	 * 子分组
	 */
	private List<MagicEntity> entities;

	public GroupEvent(String type, EventAction action, Group group) {
		this(type, action, group, Collections.emptyList());
	}

	public GroupEvent(String type, EventAction action, Group group, List<MagicEntity> entities) {
		super(type, action);
		this.group = group;
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
