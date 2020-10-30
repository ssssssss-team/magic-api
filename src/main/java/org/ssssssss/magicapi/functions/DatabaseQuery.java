package org.ssssssss.magicapi.functions;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.config.DynamicDataSource.DataSourceNode;
import org.ssssssss.magicapi.config.MagicDynamicDataSource;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.magicapi.dialect.Dialect;
import org.ssssssss.magicapi.dialect.DialectUtils;
import org.ssssssss.magicapi.exception.MagicAPIException;
import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.annotation.UnableCall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 数据库查询模块
 */
public class DatabaseQuery extends HashMap<String, DatabaseQuery> implements MagicModule {

	@UnableCall
	private MagicDynamicDataSource dynamicDataSource;

	@UnableCall
	private DataSourceNode dataSourceNode;

	@UnableCall
	private PageProvider pageProvider;

	@UnableCall
	private ResultProvider resultProvider;

	@UnableCall
	private RowMapper<Map<String, Object>> rowMapper;

	@UnableCall
	private SqlCache sqlCache;

	@UnableCall
	private String cacheName;

	@UnableCall
	private long ttl;

	public DatabaseQuery() {

	}

	public DatabaseQuery(MagicDynamicDataSource dynamicDataSource) {
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
	public void setRowMapper(RowMapper<Map<String, Object>> rowMapper) {
		this.rowMapper = rowMapper;
	}

	@UnableCall
	public void setDynamicDataSource(MagicDynamicDataSource dynamicDataSource) {
		this.dynamicDataSource = dynamicDataSource;
	}

	@UnableCall
	public void setSqlCache(SqlCache sqlCache) {
		this.sqlCache = sqlCache;
	}

	@UnableCall
	public void setDataSourceNode(DataSourceNode dataSourceNode) {
		this.dataSourceNode = dataSourceNode;
	}

	@UnableCall
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	@UnableCall
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	@UnableCall
	public DatabaseQuery cloneQuery() {
		DatabaseQuery query = new DatabaseQuery();
		query.setDynamicDataSource(this.dynamicDataSource);
		query.setDataSourceNode(this.dataSourceNode);
		query.setPageProvider(this.pageProvider);
		query.setRowMapper(this.rowMapper);
		query.setSqlCache(this.sqlCache);
		query.setTtl(this.ttl);
		query.setResultProvider(this.resultProvider);
		return query;
	}

	/**
	 * 开启事务，在一个回调中进行操作
	 * @param function	回调函数
	 * @return
	 */
	public Object transaction(Function<?, ?> function) {
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
	 * @return
	 */
	public Transaction transaction() {
		return new Transaction(this.dataSourceNode.getDataSourceTransactionManager());
	}

	/**
	 * 添加至缓存
	 * @param value	缓存名
	 */
	@UnableCall
	private <T> T putCacheValue(T value, BoundSql boundSql) {
		if (this.cacheName != null) {
			this.sqlCache.put(this.cacheName, boundSql.getCacheKey(this.sqlCache), value, this.ttl);
		}
		return value;
	}

	/**
	 * 使用缓存
	 * @param cacheName	缓存名
	 * @param ttl 过期时间
	 * @return
	 */
	public DatabaseQuery cache(String cacheName, long ttl) {
		if (cacheName == null) {
			return this;
		}
		DatabaseQuery query = cloneQuery();
		query.setCacheName(cacheName);
		query.setTtl(ttl);
		return query;
	}

	/**
	 * 使用缓存（采用默认缓存时间）
	 * @param cacheName	缓冲名
	 * @return
	 */
	public DatabaseQuery cache(String cacheName) {
		return cache(cacheName, 0);
	}

	/**
	 * 数据源切换
	 */
	@Override
	public DatabaseQuery get(Object key) {
		DatabaseQuery query = cloneQuery();
		if (key == null) {
			query.setDataSourceNode(dynamicDataSource.getDataSource());
		} else {
			query.setDataSourceNode(dynamicDataSource.getDataSource(key.toString()));
		}
		return query;
	}


	/**
	 * 查询List
	 */
	public List<Map<String, Object>> select(String sql) {
		BoundSql boundSql = new BoundSql(sql);
		return (List<Map<String, Object>>) boundSql.getCacheValue(this.sqlCache, this.cacheName)
				.orElseGet(() -> putCacheValue(dataSourceNode.getJdbcTemplate().query(boundSql.getSql(), this.rowMapper, boundSql.getParameters()), boundSql));
	}

	/**
	 * 执行update
	 */
	public int update(String sql) {
		BoundSql boundSql = new BoundSql(sql);
		int value = dataSourceNode.getJdbcTemplate().update(boundSql.getSql(), boundSql.getParameters());
		if (this.cacheName != null) {
			this.sqlCache.delete(this.cacheName);
		}
		return value;
	}

//	public int save(String tableName,Map<String,Object> params){
//		return save(tableName,params,"id");
//	}

	/**
	 * 如果已存在就修改，否则增加
	 */
//	public int save(String tableName,Map<String,Object> data,String primaryKey){
//		Object[] params = new Object[]{data.get(primaryKey)};
//		Integer count = dataSourceNode.getJdbcTemplate().queryForObject("select count(1) from "+tableName+" where "+primaryKey+" =  ?", params, Integer.class);
//		if(count > 0){
//			return jdbcUpdate(tableName,data,primaryKey);
//		}
//		return 0;
//		Object primaryKeyValue = data.get(primaryKey);
//		if(null == primaryKeyValue){
//			return jdbcInsert(tableName,data,primaryKey);
//		}
//		return jdbcUpdate(tableName,data,primaryKey);
//	}

//	public int jdbcUpdate(String tableName,Map<String,Object> data,String primaryKey){
//		StringBuffer sb = new StringBuffer();
//		sb.append("update ");
//		sb.append(tableName);
//		sb.append(" set ");
//		List<Object> params = new ArrayList<>();
//		for(Map.Entry<String, Object> entry : data.entrySet()){
//			String key = entry.getKey();
//			if(!key.equals(primaryKey)){
//				sb.append(key + "=" + "?,");
//				params.add(entry.getValue());
//			}
//		}
//		sb.append(" where ");
//		sb.append(primaryKey);
//		sb.append("=?");
//		params.add(data.get(primaryKey));
//		return dataSourceNode.getJdbcTemplate().update(sb.toString().replace("?, ","? "),params.toArray());
//	}
//
//	public int jdbcInsert(String tableName,Map<String,Object> data,String primaryKey){
//		List<Object> params = new ArrayList<>();
//		params.add("");
//		List<String> fields = new ArrayList<>();
//		List<String> valuePlaceholders = new ArrayList<>();
//		StringBuffer sb = new StringBuffer();
//		sb.append("insert into ");
//		sb.append(tableName);
//		for(Map.Entry<String, Object> entry : data.entrySet()){
//			String key = entry.getKey();
//			if(!key.equals(primaryKey)){
//				fields.add(key);
//				valuePlaceholders.add("?");
//				params.add(entry.getValue());
//			}
//		}
//		sb.append("("+ primaryKey + "," + StringUtils.join(fields,",") +")");
//		sb.append(" values(?,"+StringUtils.join(valuePlaceholders,",")+")");
//		String id = UUID.randomUUID().toString().replace("-","");
//		params.set(0,id);
//		return dataSourceNode.getJdbcTemplate().update(sb.toString(),params.toArray());
//	}

	/**
	 * 插入并返回主键
	 */
	public long insert(String sql){
		BoundSql boundSql = new BoundSql(sql);
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
		if(key == null){
			return -1;
		}
		return key.longValue();
	}

	/**
	 * 分页查询
	 */
	public Object page(String sql) {
		Page page = pageProvider.getPage(MagicScriptContext.get());
		return page(sql, page.getLimit(), page.getOffset());
	}

	/**
	 * 分页查询（手动传入limit和offset参数）
	 */
	public Object page(String sql, long limit, long offset) {
		BoundSql boundSql = new BoundSql(sql);
		Connection connection = null;
		Dialect dialect;
		try {
			connection = dataSourceNode.getJdbcTemplate().getDataSource().getConnection();
			dialect = DialectUtils.getDialectFromUrl(connection.getMetaData().getURL());
		} catch (Exception e) {
			throw new MagicAPIException("自动获取数据库方言失败", e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSourceNode.getJdbcTemplate().getDataSource());
		}
		if (dialect == null) {
			throw new MagicAPIException("自动获取数据库方言失败");
		}
		int count = (int) boundSql.getCacheValue(this.sqlCache, this.cacheName)
				.orElseGet(() -> putCacheValue(dataSourceNode.getJdbcTemplate().queryForObject(dialect.getCountSql(boundSql.getSql()), Integer.class, boundSql.getParameters()), boundSql));
		List<Object> list = null;
		if (count > 0) {
			String pageSql = dialect.getPageSql(boundSql.getSql(), boundSql, offset, limit);
			list = (List<Object>) boundSql.removeCacheKey().getCacheValue(this.sqlCache, this.cacheName)
					.orElseGet(() -> putCacheValue(dataSourceNode.getJdbcTemplate().query(pageSql, this.rowMapper, boundSql.getParameters()), boundSql));
		}
		return resultProvider.buildPageResult(count, list);
	}

	/**
	 * 查询int值
	 */
	public Integer selectInt(String sql) {
		BoundSql boundSql = new BoundSql(sql);
		return (Integer) boundSql.getCacheValue(this.sqlCache, this.cacheName)
				.orElseGet(() -> putCacheValue(dataSourceNode.getJdbcTemplate().queryForObject(boundSql.getSql(), boundSql.getParameters(), Integer.class), boundSql));
	}

	/**
	 * 查询Map
	 */
	public Map<String, Object> selectOne(String sql) {
		BoundSql boundSql = new BoundSql(sql);
		return (Map<String, Object>) boundSql.getCacheValue(this.sqlCache, this.cacheName)
				.orElseGet(() -> {
					List<Map<String, Object>> list = dataSourceNode.getJdbcTemplate().query(boundSql.getSql(), this.rowMapper, boundSql.getParameters());
					return list != null && list.size() > 0 ? list.get(0) : null;
				});
	}

	/**
	 * 查询单行单列的值
	 */
	public Object selectValue(String sql) {
		BoundSql boundSql = new BoundSql(sql);
		return boundSql.getCacheValue(this.sqlCache, this.cacheName)
				.orElseGet(() -> putCacheValue(dataSourceNode.getJdbcTemplate().queryForObject(boundSql.getSql(), boundSql.getParameters(), Object.class), boundSql));
	}

	@UnableCall
	@Override
	public String getModuleName() {
		return "db";
	}

}
