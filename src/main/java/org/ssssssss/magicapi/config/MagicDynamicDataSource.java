package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.ssssssss.magicapi.utils.Assert;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicDynamicDataSource {

	private static Logger logger = LoggerFactory.getLogger(MagicDynamicDataSource.class);

	private Map<String, MagicDynamicDataSource.DataSourceNode> dataSourceMap = new HashMap<>();

	/**
	 * 注册默认数据源
	 */
	public void put(DataSource dataSource) {
		put(null, dataSource);
	}

	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param dataSourceName 数据源名称
	 */
	public void put(String dataSourceName, DataSource dataSource) {
		if (dataSourceName == null) {
			dataSourceName = "";
		}
		logger.info("注册数据源：{}", StringUtils.isNotBlank(dataSourceName) ? dataSourceName : "default");
		this.dataSourceMap.put(dataSourceName, new MagicDynamicDataSource.DataSourceNode(dataSource));
	}

	/**
	 * 获取全部数据源
	 */
	public List<String> datasources(){
		return new ArrayList<>(this.dataSourceMap.keySet());
	}

	/**
	 * 删除数据源
	 * @param datasourceName    数据源名称
	 */
	public boolean delete(String datasourceName){
		boolean result = false;
		// 检查参数是否合法
		if(datasourceName != null && !datasourceName.isEmpty()){
			result = this.dataSourceMap.remove(datasourceName) != null;
		}
		logger.info("删除数据源：{}:{}", datasourceName, result ? "成功" : "失败");
		return result;
	}

	/**
	 * 获取默认数据源
	 * @return
	 */
	public MagicDynamicDataSource.DataSourceNode getDataSource() {
		return getDataSource(null);
	}

	/**
	 * 获取数据源
	 * @param dataSourceName    数据源名称
	 */
	public MagicDynamicDataSource.DataSourceNode getDataSource(String dataSourceName) {
		if (dataSourceName == null) {
			dataSourceName = "";
		}
		MagicDynamicDataSource.DataSourceNode dataSourceNode = dataSourceMap.get(dataSourceName);
		Assert.isNotNull(dataSourceNode, String.format("找不到数据源%s", dataSourceName));
		return dataSourceNode;
	}

	public static class DataSourceNode {

		/**
		 * 事务管理器
		 */
		private DataSourceTransactionManager dataSourceTransactionManager;

		private JdbcTemplate jdbcTemplate;

		private DataSource dataSource;

		public DataSourceNode(DataSource dataSource) {
			this.dataSource = dataSource;
			this.dataSourceTransactionManager = new DataSourceTransactionManager(this.dataSource);
			this.jdbcTemplate = new JdbcTemplate(dataSource);
		}

		public JdbcTemplate getJdbcTemplate(){
			return this.jdbcTemplate;
		}

		public DataSourceTransactionManager getDataSourceTransactionManager() {
			return dataSourceTransactionManager;
		}
	}

	public void setDefault(DataSource dataSource) {
		put(dataSource);
	}

	public void add(String dataSourceName, DataSource dataSource) {
		put(dataSourceName, dataSource);
	}
}
