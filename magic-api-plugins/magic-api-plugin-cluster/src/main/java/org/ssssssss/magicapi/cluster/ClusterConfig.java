package org.ssssssss.magicapi.cluster;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * 集群配置
 *
 * @author mxd
 * @since 1.2.0
 */
@ConfigurationProperties(prefix = "magic-api.cluster")
public class ClusterConfig {

	/**
	 * redis 通道
	 */
	private String channel = "magic-api:notify:channel";

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
