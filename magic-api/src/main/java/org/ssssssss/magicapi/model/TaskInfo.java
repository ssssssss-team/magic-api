package org.ssssssss.magicapi.model;

import java.util.Objects;

public class TaskInfo extends PathMagicEntity{

	/**
	 *  cron 表达式
	 */
	private String cron;

	/**
	 * 是否启用
	 */
	private boolean enabled;


	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public TaskInfo copy() {
		TaskInfo info = new TaskInfo();
		super.copyTo(info);
		info.setCron(this.cron);
		info.setEnabled(this.enabled);
		return info;
	}

	@Override
	public MagicEntity simple() {
		TaskInfo info = new TaskInfo();
		super.simple(info);
		return info;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		TaskInfo taskInfo = (TaskInfo) o;
		return Objects.equals(id, taskInfo.id) &&
				Objects.equals(path, taskInfo.path) &&
				Objects.equals(script, taskInfo.script) &&
				Objects.equals(name, taskInfo.name) &&
				Objects.equals(cron, taskInfo.cron) &&
				Objects.equals(enabled, taskInfo.enabled);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, path, script, name, groupId, cron, enabled);
	}
}
