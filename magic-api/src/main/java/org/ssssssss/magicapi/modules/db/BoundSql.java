package org.ssssssss.magicapi.modules.db;

import org.ssssssss.magicapi.core.context.RequestContext;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.modules.db.inteceptor.SQLInterceptor;
import org.ssssssss.magicapi.modules.db.mybatis.MybatisParser;
import org.ssssssss.magicapi.modules.db.mybatis.SqlNode;
import org.ssssssss.magicapi.modules.db.mybatis.TextSqlNode;
import org.ssssssss.script.runtime.RuntimeContext;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * SQL参数处理
 *
 * @author mxd
 */
public class BoundSql {

	private static final Pattern REPLACE_MULTI_WHITE_LINE = Pattern.compile("(\r?\n(\\s*\r?\n)+)");

	private static final List<String> MYBATIS_TAGS = Arrays.asList("</where>", "</if>", "</trim>", "</set>", "</foreach>");

	private String sqlOrXml;

	private List<Object> parameters = new ArrayList<>();

	private Set<String> excludeColumns;

	private SQLModule sqlModule;

	private Map<String, Object> bindParameters;

	private RuntimeContext runtimeContext;

	public BoundSql(RuntimeContext runtimeContext, String sqlOrXml, List<Object> parameters, SQLModule sqlModule) {
		this.sqlOrXml = sqlOrXml;
		this.parameters = parameters;
		this.sqlModule = sqlModule;
		this.runtimeContext = runtimeContext;
	}

	public BoundSql(RuntimeContext runtimeContext, String sqlOrXml, Map<String, Object> parameters, SQLModule sqlModule) {
		this.sqlOrXml = sqlOrXml;
		this.bindParameters = parameters;
		this.sqlModule = sqlModule;
		this.runtimeContext = runtimeContext;
		this.init();
	}

	private BoundSql(RuntimeContext runtimeContext, String sqlOrXml) {
		this.sqlOrXml = sqlOrXml;
		this.runtimeContext = runtimeContext;
		this.init();
	}

	BoundSql(RuntimeContext runtimeContext, String sql, SQLModule sqlModule) {
		this(runtimeContext, sql);
		this.sqlModule = sqlModule;
	}

	private BoundSql() {

	}

	private void init() {
		Map<String, Object> varMap = new HashMap<>();
		if (this.bindParameters != null) {
			varMap.putAll(this.bindParameters);
		} else {
			varMap.putAll(runtimeContext.getVarMap());
		}
		if (MYBATIS_TAGS.stream().anyMatch(it -> this.sqlOrXml.contains(it))) {
			SqlNode sqlNode = MybatisParser.parse(this.sqlOrXml);
			this.sqlOrXml = sqlNode.getSql(varMap);
			this.parameters = sqlNode.getParameters();
		} else {
			normal(varMap);
		}
	}

	private void normal(Map<String, Object> varMap) {
		this.sqlOrXml = TextSqlNode.parseSql(this.sqlOrXml, varMap, parameters);
		this.sqlOrXml = this.sqlOrXml == null ? null : REPLACE_MULTI_WHITE_LINE.matcher(this.sqlOrXml.trim()).replaceAll("\r\n");
	}

	public SQLModule getSqlModule() {
		return sqlModule;
	}

	BoundSql copy(String newSqlOrXml) {
		BoundSql boundSql = new BoundSql();
		boundSql.parameters = this.parameters;
		boundSql.bindParameters = this.bindParameters;
		boundSql.sqlOrXml = newSqlOrXml;
		boundSql.excludeColumns = this.excludeColumns;
		boundSql.sqlModule = this.sqlModule;
		boundSql.runtimeContext = this.runtimeContext;
		return boundSql;
	}

	public Set<String> getExcludeColumns() {
		return excludeColumns;
	}

	public void setExcludeColumns(Set<String> excludeColumns) {
		this.excludeColumns = excludeColumns;
	}

	/**
	 * 添加SQL参数
	 */
	public void addParameter(Object value) {
		parameters.add(value);
	}

	/**
	 * 获取要执行的SQL
	 */
	public String getSql() {
		return sqlOrXml;
	}

	/**
	 * 设置要执行的SQL
	 */
	public void setSql(String sql) {
		this.sqlOrXml = sql;
	}

	/**
	 * 获取要执行的参数
	 */
	public Object[] getParameters() {
		return parameters.toArray();
	}

	/**
	 * 设置要执行的参数
	 */
	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}


	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

	/**
	 * 获取缓存值
	 */
	@SuppressWarnings({"unchecked"})
	private <T> T getCacheValue(String sql, Object[] params, Supplier<T> supplier) {
		if (sqlModule.getCacheName() == null) {
			return supplier.get();
		}
		String cacheKey = sqlModule.getSqlCache().buildSqlCacheKey(sql, params);
		Object cacheValue = sqlModule.getSqlCache().get(sqlModule.getCacheName(), cacheKey);
		if (cacheValue != null) {
			return (T) cacheValue;
		}
		T value = supplier.get();
		sqlModule.getSqlCache().put(sqlModule.getCacheName(), cacheKey, value, sqlModule.getTtl());
		return value;
	}

	/**
	 * 获取缓存值
	 */
	@SuppressWarnings("unchecked")
	<T> T execute(List<SQLInterceptor> interceptors, Supplier<T> supplier) {
		RequestEntity requestEntity = RequestContext.getRequestEntity();
		interceptors.forEach(interceptor -> interceptor.preHandle(this, requestEntity));
		Supplier<T> newSupplier = () -> {
			Object result = supplier.get();
			for (SQLInterceptor interceptor : interceptors) {
				result = interceptor.postHandle(this, result, requestEntity);
			}
			return (T) result;
		};
		return getCacheValue(this.getSql(), this.getParameters(), newSupplier);
	}
}
