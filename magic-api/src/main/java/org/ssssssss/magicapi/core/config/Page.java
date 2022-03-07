package org.ssssssss.magicapi.core.config;

/**
 * 分页配置
 *
 * @author mxd
 */
public class Page {

	/**
	 * 默认page表达式
	 */
	private String page = "page";

	/**
	 * 默认size表达式
	 */
	private String size = "size";

	/**
	 * 默认首页
	 */
	private long defaultPage = 1;

	/**
	 * 默认页大小
	 */
	private long defaultSize = 10;

	/**
	 * 最大页大小， -1 为不限制
	 */
	private long maxPageSize = -1;

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public long getDefaultPage() {
		return defaultPage;
	}

	public void setDefaultPage(long defaultPage) {
		this.defaultPage = defaultPage;
	}

	public long getDefaultSize() {
		return defaultSize;
	}

	public void setDefaultSize(long defaultSize) {
		this.defaultSize = defaultSize;
	}

	public long getMaxPageSize() {
		return maxPageSize;
	}

	public void setMaxPageSize(long maxPageSize) {
		this.maxPageSize = maxPageSize;
	}
}
