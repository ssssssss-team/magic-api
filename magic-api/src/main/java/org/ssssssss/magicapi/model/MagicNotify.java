package org.ssssssss.magicapi.model;

public class MagicNotify {

	/**
	 * 消息来源
	 */
	private String from;

	/**
	 * 对应的id，如接口id、函数id，分组id、数据源id
	 */
	private String id;

	/**
	 * 动作
	 */
	private int action = -1;

	/**
	 * 操作对象，如接口、函数、分组、数据源
	 */
	private int type = -1;

	public MagicNotify() {
	}

	public MagicNotify(String from) {
		this(from, null, Constants.NOTIFY_ACTION_ALL, -1);
	}

	public MagicNotify(String from, String id, int action, int type) {
		this.from = from;
		this.id = id;
		this.action = action;
		this.type = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
