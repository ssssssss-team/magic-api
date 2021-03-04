package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.ssssssss.magicapi.adapter.DialectAdapter;
import org.ssssssss.magicapi.dialect.Dialect;
import org.ssssssss.magicapi.exception.MagicAPIException;
import org.ssssssss.magicapi.utils.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicDynamicDataSource {

	private static final Logger logger = LoggerFactory.getLogger(MagicDynamicDataSource.class);

	private final Map<String, MagicDynamicDataSource.DataSourceNode> dataSourceMap = new HashMap<>();

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
		private final DataSourceTransactionManager dataSourceTransactionManager;

		private final JdbcTemplate jdbcTemplate;

		private final DataSource dataSource;

		private Dialect dialect;

		DataSourceNode(DataSource dataSource) {
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

		public Dialect getDialect(DialectAdapter dialectAdapter){
			if(this.dialect == null){
				Connection connection = null;
				try {
					connection = this.dataSource.getConnection();
					this.dialect = dialectAdapter.getDialectFromUrl(connection.getMetaData().getURL());
					if(this.dialect == null){
						throw new MagicAPIException("自动获取数据库方言失败");
					}
				} catch (Exception e) {
					throw new MagicAPIException("自动获取数据库方言失败", e);
				} finally {
					DataSourceUtils.releaseConnection(connection, this.dataSource);
				}
			}
			return dialect;
		}
	}

	public void setDefault(DataSource dataSource) {
		put(dataSource);
	}

	public void add(String dataSourceName, DataSource dataSource) {
		put(dataSourceName, dataSource);
	}
}
