package org.ssssssss.magicapi.modules;

import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.magicapi.interceptor.SQLInterceptor;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.parsing.GenericTokenParser;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoundSql {

	private static final GenericTokenParser concatTokenParser = new GenericTokenParser("${", "}", false);

	private static final GenericTokenParser replaceTokenParser = new GenericTokenParser("#{", "}", true);

	private static final GenericTokenParser ifTokenParser = new GenericTokenParser("?{", "}", true);

	private static final GenericTokenParser ifParamTokenParser = new GenericTokenParser("?{", ",", true);

	private static final Pattern REPLACE_MULTI_WHITE_LINE = Pattern.compile("(\r?\n(\\s*\r?\n)+)");

	private String sql;

	private List<Object> parameters = new ArrayList<>();

	private SqlCache sqlCache;

	private String cacheName;

	private long ttl;

	public BoundSql(String sql, List<Object> parameters, SQLModule sqlModule) {
		this.sql = sql;
		this.parameters = parameters;
		this.sqlCache = sqlModule.getSqlCache();
		this.cacheName = sqlModule.getCacheName();
		this.ttl = sqlModule.getTtl();
	}

	BoundSql(String sql) {
		MagicScriptContext context = MagicScriptContext.get();
		// 处理?{}参数
		this.sql = ifTokenParser.parse(sql.trim(), text -> {
			AtomicBoolean ifTrue = new AtomicBoolean(false);
			String val = ifParamTokenParser.parse("?{" + text, param -> {
				ifTrue.set(BooleanLiteral.isTrue(context.eval(param)));
				return null;
			});
			return ifTrue.get() ? val : "";
		});
		// 处理${}参数
		this.sql = concatTokenParser.parse(this.sql, text -> String.valueOf(context.eval(text)));
		// 处理#{}参数
		this.sql = replaceTokenParser.parse(this.sql, text -> {
			Object value = context.eval(text);
			if (value == null) {
				parameters.add(null);
				return "?";
			}
			try {
				//对集合自动展开
				List<Object> objects = StreamExtension.arrayLikeToList(value);
				parameters.addAll(objects);
				return IntStream.range(0, objects.size()).mapToObj(t -> "?").collect(Collectors.joining(","));
			} catch (Exception e) {
				parameters.add(value);
				return "?";
			}
		});
		this.sql = this.sql == null ? null : REPLACE_MULTI_WHITE_LINE.matcher(this.sql.trim()).replaceAll("\r\n");
	}

	BoundSql(String sql, SQLModule sqlModule) {
		this(sql);
		this.sqlCache = sqlModule.getSqlCache();
		this.cacheName = sqlModule.getCacheName();
		this.ttl = sqlModule.getTtl();
	}

	private BoundSql() {

	}

	BoundSql copy(String newSql) {
		BoundSql boundSql = new BoundSql();
		boundSql.setParameters(new ArrayList<>(this.parameters));
		boundSql.setSql(this.sql);
		boundSql.ttl = this.ttl;
		boundSql.cacheName = this.cacheName;
		boundSql.sqlCache = this.sqlCache;
		boundSql.sql = newSql;
		return boundSql;
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
		return sql;
	}

	/**
	 * 设置要执行的SQL
	 */
	public void setSql(String sql) {
		this.sql = sql;
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

	/**
	 * 获取缓存值
	 */
	private <T> T getCacheValue(String sql, Object[] params, Supplier<T> supplier) {
		if (cacheName == null) {
			return supplier.get();
		}
		String cacheKey = sqlCache.buildSqlCacheKey(sql, params);
		Object cacheValue = sqlCache.get(cacheName, cacheKey);
		if (cacheValue != null) {
			return (T) cacheValue;
		}
		T value = supplier.get();
		sqlCache.put(cacheName, cacheKey, value, ttl);
		return value;
	}

	/**
	 * 获取缓存值
	 */
	<T> T getCacheValue(List<SQLInterceptor> interceptors, Supplier<T> supplier) {
		interceptors.forEach(interceptor -> interceptor.preHandle(this));
		return getCacheValue(this.getSql(), this.getParameters(), supplier);
	}
}