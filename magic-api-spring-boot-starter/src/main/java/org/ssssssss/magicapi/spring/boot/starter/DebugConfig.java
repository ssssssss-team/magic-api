package org.ssssssss.magicapi.spring.boot.starter;

/**
 * Debug配置
 *
 * @author mxd
 */
public class DebugConfig {

	/**
	 * 断点超时时间
	 */
	private int timeout = 60;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
