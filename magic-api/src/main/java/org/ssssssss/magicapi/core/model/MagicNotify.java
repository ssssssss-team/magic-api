package org.ssssssss.magicapi.core.model;

import org.ssssssss.magicapi.core.event.EventAction;

/**
 * 消息通知对象
 *
 * @author mxd
 */
public class MagicNotify {

	/**
	 * 消息来源(instanceId)
	 */
	private String from;

	/**
	 * 文件或文件夹id
	 */
	private String id;

	/**
	 * 动作
	 */
	private EventAction action = null;

	/**
	 * 操作对象，如接口、函数、分组、数据源
	 */
	private String type = null;

	/**
	 * WebSocket clientId
	 */
	private String clientId;

	/**
	 * WebSocket消息内容
	 */
	private String content;

	public MagicNotify() {
	}

	public MagicNotify(String from) {
		this.from = from;
	}

	public MagicNotify(String from, EventAction action, String clientId, String content) {
		this.from = from;
		this.clientId = clientId;
		this.action = action;
		this.content = content;
	}

	public MagicNotify(String from, String id, EventAction action, String type) {
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

	public EventAction getAction() {
		return action;
	}

	public void setAction(EventAction action) {
		this.action = action;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "MagicNotify{" +
				"from='" + from + '\'' +
				", id='" + id + '\'' +
				", action=" + action +
				", type='" + type + '\'' +
				", clientId='" + clientId + '\'' +
				", content='" + content + '\'' +
				'}';
	}
}
