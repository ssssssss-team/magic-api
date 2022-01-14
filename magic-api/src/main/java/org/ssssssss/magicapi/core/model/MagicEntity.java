package org.ssssssss.magicapi.core.model;

import java.util.Objects;

public class MagicEntity extends Attributes<Object> {

	protected String id;

	protected String script;

	protected String groupId;

	protected String name;

	protected Long createTime;

	protected Long updateTime;

	protected String lock;

	protected String createBy;

	protected String updateBy;

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

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MagicEntity)) return false;
		MagicEntity that = (MagicEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(script, that.script) && Objects.equals(groupId, that.groupId) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, script, groupId, name);
	}

	public MagicEntity copy() {
		MagicEntity entity = new MagicEntity();
		copyTo(entity);
		return entity;
	}

	public MagicEntity simple() {
		MagicEntity entity = new MagicEntity();
		simple(entity);
		return entity;
	}

	protected void simple(MagicEntity entity) {
		entity.setId(this.id);
		entity.setName(this.name);
		entity.setGroupId(this.groupId);
		entity.setCreateBy(this.createBy);
		entity.setCreateTime(this.createTime);
		entity.setUpdateBy(this.updateBy);
		entity.setUpdateTime(this.updateTime);
		entity.setLock(this.lock);
	}

	protected void copyTo(MagicEntity entity) {
		simple(entity);
		entity.setScript(this.script);
		entity.setProperties(this.properties);
	}
}
