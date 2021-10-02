package org.ssssssss.magicapi.spring.boot.starter;

/**
 * 备份配置
 *
 * @since 1.3.5
 */
public class BackupConfig{

	/**
	 * 存储类型，可选 file， database
	 */
	private String resourceType = "file";

	/**
	 * 存储位置，选择存储为文件时专用
	 */
	private String location = "/data/magic-api/backup";

	/**
	 * 保留天数，<=0 为不限制
	 */
	private int maxHistory = -1;

	/**
	 * 使用数据库存储时的表名
	 */
	private String tableName;

	/**
	 * 使用数据库存储时使用的数据源
	 */
	private String datasource;

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getMaxHistory() {
		return maxHistory;
	}

	public void setMaxHistory(int maxHistory) {
		this.maxHistory = maxHistory;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}
}
