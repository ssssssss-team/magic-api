package org.ssssssss.magicapi.core.config;

/**
 * 接口存储配置
 *
 * @author mxd
 */
public class Resource {

	/**
	 * 存储类型，默认是文件
	 */
	private String type = "file";

	/**
	 * 文件存储位置
	 */
	private String location = "/data/magic-api/";

	/**
	 * 是否是只读模式
	 */
	private boolean readonly = false;

	/**
	 * 前缀
	 */
	private String prefix = "magic-api";

	/**
	 * 使用数据库存储时的表名
	 */
	private String tableName = "magic_api_file";

	/**
	 * 使用数据库存储时使用的数据源
	 */
	private String datasource;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
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
