package org.ssssssss.magicapi.core.config;

/**
 * 备份配置
 *
 * @author mxd
 * @since 2.0.0
 */
public class Backup {

	/**
	 * 是否启用备份配置，默认不启用
	 */
	private boolean enable = false;

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

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
