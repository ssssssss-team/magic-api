package org.ssssssss.magicapi.spring.boot.starter;

public class ClusterConfig {

	/**
	 * 实例ID，集群环境下，要保证每台机器不同。默认启动后随机生成uuid
	 */
	private String instanceId;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
