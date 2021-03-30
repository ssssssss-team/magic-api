package org.ssssssss.magicapi.modules;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.ssssssss.magicapi.adapter.ColumnMapperAdapter;
import org.ssssssss.magicapi.adapter.DialectAdapter;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.config.MagicDynamicDataSource;
import org.ssssssss.magicapi.config.MagicDynamicDataSource.DataSourceNode;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.magicapi.dialect.Dialect;
import org.ssssssss.magicapi.interceptor.SQLInterceptor;
import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.modules.table.NamedTable;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.UnableCall;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 数据库查询模块
 */
public class SQLModule extends HashMap<String, SQLModule> implements MagicModule {

	private MagicDynamicDataSource dynamicDataSource;

	private DataSourceNode dataSourceNode;

	private PageProvider pageProvider;

	private ResultProvider resultProvider;

	private ColumnMapperAdapter columnMapperAdapter;

	private DialectAdapter dialectAdapter;

	private RowMapper<Map<String, Object>> columnMapRowMapper;

	private Function<String, String> rowMapColumnMapper;

	private SqlCache sqlCache;

	private String cacheName;

	private List<SQLInterceptor> sqlInterceptors;

	private long ttl;

	public SQLModule() {

	}

	public SQLModule(MagicDynamicDataSource dynamicDataSource) {
		this.dynamicDataSource = dynamicDataSource;
		this.dataSourceNode = dynamicDataSource.getDataSource();
	}

	@UnableCall
	public void setPageProvider(PageProvider pageProvider) {
		this.pageProvider = pageProvider;
	}

	@UnableCall
	public void setResultProvider(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	@UnableCall
	public void setColumnMapperProvider(ColumnMapperAdapter columnMapperAdapter) {
		this.columnMapperAdapter = columnMapperAdapter;
	}

	@UnableCall
	public void setDialectAdapter(DialectAdapter dialectAdapter) {
		this.dialectAdapter = dialectAdapter;
	}

	@UnableCall
	public void setColumnMapRowMapper(RowMapper<Map<String, Object>> columnMapRowMapper) {
		this.columnMapRowMapper = columnMapRowMapper;
	}

	@UnableCall
	public void setRowMapColumnMapper(Function<String, String> rowMapColumnMapper) {
		this.rowMapColumnMapper = rowMapColumnMapper;
	}

	private void setDynamicDataSource(MagicDynamicDataSource dynamicDataSource) {
		this.dynamicDataSource = dynamicDataSource;
	}

	@UnableCall
	public void setSqlInterceptors(List<SQLInterceptor> sqlInterceptors) {
		this.sqlInterceptors = sqlInterceptors;
	}

	@UnableCall
	public void setSqlCache(SqlCache sqlCache) {
		this.sqlCache = sqlCache;
	}

	private void setDataSourceNode(DataSourceNode dataSourceNode) {
		this.dataSourceNode = dataSourceNode;
	}

	private void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	private void setTtl(long ttl) {
		this.ttl = ttl;
	}

	protected String getCacheName() {
		return cacheName;
	}

	protected long getTtl() {
		return ttl;
	}

	protected SqlCache getSqlCache() {
		return sqlCache;
	}

	@UnableCall
	private SQLModule cloneSQLModule() {
		SQLModule sqlModule = new SQLModule();
		sqlModule.setDynamicDataSource(this.dynamicDataSource);
		sqlModule.setDataSourceNode(this.dataSourceNode);
		sqlModule.setPageProvider(this.pageProvider);
		sqlModule.setColumnMapperProvider(this.columnMapperAdapter);
		sqlModule.setColumnMapRowMapper(this.columnMapRowMapper);
		sqlModule.setRowMapColumnMapper(this.rowMapColumnMapper);
		sqlModule.setSqlCache(this.sqlCache);
		sqlModule.setTtl(this.ttl);
		sqlModule.setResultProvider(this.resultProvider);
		sqlModule.setDialectAdapter(this.dialectAdapter);
		sqlModule.setSqlInterceptors(this.sqlInterceptors);
		return sqlModule;
	}

	/**
	 * 开启事务，在一个回调中进行操作
	 *
	 * @param function 回调函数
	 */
	@Comment("开启事务，并在回调中处理")
	public Object transaction(@Comment("回调函数，如：()=>{....}") Function<?, ?> function) {
		Transaction transaction = transaction();    //创建事务
		try {
			Object val = function.apply(null);
			transaction.commit();    //提交事务
			return val;
		} catch (Throwable throwable) {
			transaction.rollback();    //回滚事务
			throw throwable;
		}
	}

	/**
	 * 开启事务，手动提交和回滚
	 */
	@Comment("开启事务，返回事务对象")
	public Transaction transaction() {
		return new Transaction(this.dataSourceNode.getDataSourceTransactionManager());
	}

	/**
	 * 使用缓存
	 *
	 * @param cacheName 缓存名
	 * @param ttl       过期时间
	 */
	@Comment("使用缓存")
	public SQLModule cache(@Comment("缓存名") String cacheName, @Comment("过期时间") long ttl) {
		if (cacheName == null) {
			return this;
		}
		SQLModule sqlModule = cloneSQLModule();
		sqlModule.setCacheName(cacheName);
		sqlModule.setTtl(ttl);
		return sqlModule;
	}

	/**
	 * 使用缓存（采用默认缓存时间）
	 *
	 * @param cacheName 缓冲名
	 */
	@Comment("使用缓存，过期时间采用默认配置")
	public SQLModule cache(@Comment("缓存名") String cacheName) {
		return cache(cacheName, 0);
	}

	@Comment("采用驼峰列名")
	public SQLModule camel() {
		return columnCase("camel");
	}

	@Comment("采用帕斯卡列名")
	public SQLModule pascal() {
		return columnCase("pascal");
	}

	@Comment("采用全小写列名")
	public SQLModule lower() {
		return columnCase("lower");
	}

	@Comment("采用全大写列名")
	public SQLModule upper() {
		return columnCase("upper");
	}

	@Comment("列名保持原样")
	public SQLModule normal() {
		return columnCase("default");
	}

	@Comment("指定列名转换")
	public SQLModule columnCase(String name) {
		SQLModule sqlModule = cloneSQLModule();
		sqlModule.setColumnMapRowMapper(this.columnMapperAdapter.getColumnMapRowMapper(name));
		sqlModule.setRowMapColumnMapper(this.columnMapperAdapter.getRowMapColumnMapper(name));
		return sqlModule;
	}

	/**
	 * 数据源切换
	 */
	@Override
	public SQLModule get(Object key) {
		SQLModule sqlModule = cloneSQLModule();
		if (key == null) {
			sqlModule.setDataSourceNode(dynamicDataSource.getDataSource());
		} else {
			sqlModule.setDataSourceNode(dynamicDataSource.getDataSource(key.toString()));
		}
		return sqlModule;
	}


	/**
	 * 查询List
	 */
	@Comment("查询SQL，返回List类型结果")
	public List<Map<String, Object>> select(@Comment("`SQL`语句") String sql) {
		return select(new BoundSql(sql, this));
	}

	@UnableCall
	public List<Map<String, Object>> select(BoundSql boundSql) {
		return boundSql.getCacheValue(this.sqlInterceptors, () -> dataSourceNode.getJdbcTemplate().query(boundSql.getSql(), this.columnMapRowMapper, boundSql.getParameters()));
	}

	/**
	 * 执行update
	 */
	@Comment("执行update操作，返回受影响行数")
	public int update(@Comment("`SQL`语句") String sql) {
		return update(new BoundSql(sql));
	}

	@UnableCall
	public int update(BoundSql boundSql) {
		sqlInterceptors.forEach(sqlInterceptor -> sqlInterceptor.preHandle(boundSql));
		int value = dataSourceNode.getJdbcTemplate().update(boundSql.getSql(), boundSql.getParameters());
		if (this.cacheName != null) {
			this.sqlCache.delete(this.cacheName);
		}
		return value;
	}

	/**
	 * 插入并返回主键
	 */
	@Comment("执行insert操作，返回插入主键")
	public long insert(@Comment("`SQL`语句") String sql) {
		BoundSql boundSql = new BoundSql(sql);
		sqlInterceptors.forEach(sqlInterceptor -> sqlInterceptor.preHandle(boundSql));
		KeyHolder keyHolder = new GeneratedKeyHolder();
		dataSourceNode.getJdbcTemplate().update(con -> {
			PreparedStatement ps = con.prepareStatement(boundSql.getSql(), Statement.RETURN_GENERATED_KEYS);
			new ArgumentPreparedStatementSetter(boundSql.getParameters()).setValues(ps);
			return ps;
		}, keyHolder);
		if (this.cacheName != null) {
			this.sqlCache.delete(this.cacheName);
		}
		Number key = keyHolder.getKey();
		if (key == null) {
			return -1;
		}
		return key.longValue();
	}

	/**
	 * 分页查询
	 */
	@Comment("执行分页查询，分页条件自动获取")
	public Object page(@Comment("`SQL`语句") String sql) {
		return page(new BoundSql(sql, this));
	}

	/**
	 * 分页查询（手动传入limit和offset参数）
	 */
	@Comment("执行分页查询，分页条件手动传入")
	public Object page(@Comment("`SQL`语句") String sql, @Comment("限制条数") long limit, @Comment("跳过条数") long offset) {
		BoundSql boundSql = new BoundSql(sql, this);
		return page(boundSql, limit, offset);
	}

	@UnableCall
	public Object page(BoundSql boundSql) {
		Page page = pageProvider.getPage(MagicScriptContext.get());
		return page(boundSql, page.getLimit(), page.getOffset());
	}

	private Object page(BoundSql boundSql, long limit, long offset) {
		Dialect dialect = dataSourceNode.getDialect(dialectAdapter);
		BoundSql countBoundSql = boundSql.copy(dialect.getCountSql(boundSql.getSql()));
		int count = countBoundSql.getCacheValue(this.sqlInterceptors, () -> dataSourceNode.getJdbcTemplate().queryForObject(countBoundSql.getSql(), Integer.class, countBoundSql.getParameters()));
		List<Map<String, Object>> list = null;
		if (count > 0) {
			String pageSql = dialect.getPageSql(boundSql.getSql(), boundSql, offset, limit);
			BoundSql pageBoundSql = boundSql.copy(pageSql);
			list = pageBoundSql.getCacheValue(this.sqlInterceptors, () -> dataSourceNode.getJdbcTemplate().query(pageBoundSql.getSql(), this.columnMapRowMapper, pageBoundSql.getParameters()));
		}
		return resultProvider.buildPageResult(count, list);
	}

	/**
	 * 查询int值
	 */
	@Comment("查询int值，适合单行单列int的结果")
	public Integer selectInt(@Comment("`SQL`语句") String sql) {
		BoundSql boundSql = new BoundSql(sql, this);
		return boundSql.getCacheValue(this.sqlInterceptors, () -> dataSourceNode.getJdbcTemplate().queryForObject(boundSql.getSql(), boundSql.getParameters(), Integer.class));
	}

	/**
	 * 查询Map
	 */
	@Comment("查询单条结果，查不到返回null")
	public Map<String, Object> selectOne(@Comment("`SQL`语句") String sql) {
		return selectOne(new BoundSql(sql, this));
	}

	@UnableCall
	public Map<String, Object> selectOne(BoundSql boundSql) {
		return boundSql.getCacheValue(this.sqlInterceptors, () -> {
			List<Map<String, Object>> list = dataSourceNode.getJdbcTemplate().query(boundSql.getSql(), this.columnMapRowMapper, boundSql.getParameters());
			return list.size() > 0 ? list.get(0) : null;
		});
	}

	/**
	 * 查询单行单列的值
	 */
	@Comment("查询单行单列的值")
	public Object selectValue(@Comment("`SQL`语句") String sql) {
		BoundSql boundSql = new BoundSql(sql, this);
		return boundSql.getCacheValue(this.sqlInterceptors, () -> dataSourceNode.getJdbcTemplate().queryForObject(boundSql.getSql(), boundSql.getParameters(), Object.class));
	}

	@Comment("指定table，进行单表操作")
	public NamedTable table(String tableName) {
		return new NamedTable(tableName, this, rowMapColumnMapper);
	}

	@UnableCall
	@Override
	public String getModuleName() {
		return "db";
	}

}
