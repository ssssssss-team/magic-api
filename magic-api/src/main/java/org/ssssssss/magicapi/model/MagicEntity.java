package org.ssssssss.magicapi.model;

public class MagicEntity extends Attributes<Object> implements Cloneable {

	protected String id;

	protected String script;

	protected String groupId;

	protected String name;

	protected Long createTime;

	protected Long updateTime;

	protected String lock;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLock() {
		return lock;
	}

	public void setLock(String lock) {
		this.lock = lock;
	}

	public MagicEntity clone() {
		try {
			return (MagicEntity) super.clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}
}
