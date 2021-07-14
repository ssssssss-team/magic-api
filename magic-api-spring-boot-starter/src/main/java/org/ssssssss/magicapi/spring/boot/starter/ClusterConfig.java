package org.ssssssss.magicapi.spring.boot.starter;

import java.util.UUID;

/**
 * 集群配置
 * @since 1.2.0
 */
public class ClusterConfig {

	/**
	 * 是否启用，默认不启用
	 */
	private boolean enable = false;

	/**
	 * 实例ID，集群环境下，要保证每台机器不同。默认启动后随机生成uuid
	 */
	private String instanceId = UUID.randomUUID().toString();

	/**
	 * redis 通道
	 */
	private String channel = "magic-api:notify:channel";

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
