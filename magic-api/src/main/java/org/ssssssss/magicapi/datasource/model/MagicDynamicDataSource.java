package org.ssssssss.magicapi.datasource.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.ssssssss.magicapi.modules.db.dialect.DialectAdapter;
import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.modules.db.dialect.Dialect;
import org.ssssssss.magicapi.utils.Assert;
import org.ssssssss.magicapi.utils.IoUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * 动态数据源对象
 *
 * @author mxd
 */
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
	 * @param dataSourceKey 数据源Key
	 */
	public void put(String dataSourceKey, DataSource dataSource) {
		put(dataSourceKey, dataSource, -1);
	}

	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param dataSourceKey 数据源Key
	 * @param maxRows       最大返回行数
	 */
	public void put(String dataSourceKey, DataSource dataSource, int maxRows) {
		put(null, dataSourceKey, dataSourceKey, dataSource, maxRows);
	}

	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param id             数据源ID
	 * @param dataSourceKey  数据源Key
	 * @param datasourceName 数据源名称
	 */
	public void put(String id, String dataSourceKey, String datasourceName, DataSource dataSource, int maxRows) {
		if (dataSourceKey == null) {
			dataSourceKey = "";
		}
		logger.info("注册数据源：{}", StringUtils.isNotBlank(dataSourceKey) ? dataSourceKey : "default");
		DataSourceNode node = this.dataSourceMap.put(dataSourceKey, new DataSourceNode(dataSource, dataSourceKey, datasourceName, id, maxRows));
		if (node != null) {
			node.close();
		}
		if (id != null) {
			String finalDataSourceKey = dataSourceKey;
			this.dataSourceMap.entrySet().stream()
					.filter(it -> id.equals(it.getValue().getId()) && !finalDataSourceKey.equals(it.getValue().getKey()))
					.findFirst()
					.ifPresent(it -> {
						logger.info("移除旧数据源:{}", it.getValue().getKey());
						this.dataSourceMap.remove(it.getValue().getKey()).close();
					});
		}
	}

	/**
	 * 获取全部数据源
	 */
	public List<String> datasources() {
		return new ArrayList<>(this.dataSourceMap.keySet());
	}

	public boolean isEmpty() {
		return this.dataSourceMap.isEmpty();
	}

	/**
	 * 获取全部数据源
	 */
	public Collection<DataSourceNode> datasourceNodes() {
		return this.dataSourceMap.values();
	}

	/**
	 * 删除数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public boolean delete(String datasourceKey) {
		boolean result = false;
		// 检查参数是否合法
		if (datasourceKey != null && !datasourceKey.isEmpty()) {
			DataSourceNode node = this.dataSourceMap.remove(datasourceKey);
			result = node != null;
			if (result) {
				node.close();
			}
		}
		logger.info("删除数据源：{}:{}", datasourceKey, result ? "成功" : "失败");
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
	 *
	 * @param datasourceKey 数据源Key
	 */
	public MagicDynamicDataSource.DataSourceNode getDataSource(String datasourceKey) {
		if (datasourceKey == null) {
			datasourceKey = "";
		}
		MagicDynamicDataSource.DataSourceNode dataSourceNode = dataSourceMap.get(datasourceKey);
		Assert.isNotNull(dataSourceNode, String.format("找不到数据源%s", datasourceKey));
		return dataSourceNode;
	}

	/**
	 * 设置默认数据源
	 */
	public void setDefault(DataSource dataSource) {
		put(dataSource);
	}

	/**
	 * 设置默认数据源
	 *
	 * @param maxRows 最大返回行数
	 */
	public void setDefault(DataSource dataSource, int maxRows) {
		put(null, null, null, dataSource, maxRows);
	}


	public void add(String dataSourceKey, DataSource dataSource) {
		put(dataSourceKey, dataSource);
	}

	public void add(String dataSourceKey, DataSource dataSource, int maxRows) {
		put(null, dataSourceKey, dataSourceKey, dataSource, maxRows);
	}

	public static class DataSourceNode {

		private final String id;

		private final String key;

		private final String name;

		/**
		 * 事务管理器
		 */
		private final DataSourceTransactionManager dataSourceTransactionManager;

		private final JdbcTemplate jdbcTemplate;

		private final DataSource dataSource;

		private Dialect dialect;

		DataSourceNode(DataSource dataSource, String key, String name, String id, int maxRows) {
			this.dataSource = dataSource;
			this.key = key;
			this.name = name;
			this.id = id;
			this.dataSourceTransactionManager = new DataSourceTransactionManager(this.dataSource);
			this.jdbcTemplate = new JdbcTemplate(dataSource);
			this.jdbcTemplate.setMaxRows(maxRows);
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getKey() {
			return key;
		}

		public JdbcTemplate getJdbcTemplate() {
			return this.jdbcTemplate;
		}

		public DataSourceTransactionManager getDataSourceTransactionManager() {
			return dataSourceTransactionManager;
		}

		public Dialect getDialect(DialectAdapter dialectAdapter) {
			if (this.dialect == null) {
				Connection connection = null;
				try {
					connection = this.dataSource.getConnection();
					this.dialect = dialectAdapter.getDialectFromConnection(connection);
					if (this.dialect == null) {
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

		public DataSource getDataSource() {
			return dataSource;
		}

		public void close() {
			IoUtils.closeDataSource(this.dataSource);
		}
	}
}
