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

	BoundSql(String sql) {
		MagicScriptContext context = MagicScriptContext.get();
		// 处理?{}参数
		this.sql = ifTokenParser.parse(sql.trim(), text -> {
			AtomicBoolean ifTrue = new AtomicBoolean(false);
			String val = ifParamTokenParser.parse("?{" + text, param -> {
				ifTrue.set(BooleanLiteral.isTrue(context.eval(param)));
				return null;
			});
			if (ifTrue.get()) {
				return val;
			}
			return "";
		});
		// 处理${}参数
		this.sql = concatTokenParser.parse(this.sql, text -> String.valueOf(context.eval(text)));
		// 处理#{}参数
		this.sql = replaceTokenParser.parse(this.sql, text -> {
			Object value = context.eval(text);
			try {
				//对集合自动展开
				List<Object> objects = StreamExtension.arrayLikeToList(value);
				StringBuilder sb = new StringBuilder();
				for (int i = 0, size = objects.size(); i < size; i++) {
					sb.append("?");
					if (i + 1 < size) {
						sb.append(",");
					}
					parameters.add(objects.get(i));
				}
				return sb.toString();
			} catch (Exception e) {
				parameters.add(value);
				return "?";
			}
		});
		this.sql = this.sql == null ? null : REPLACE_MULTI_WHITE_LINE.matcher(this.sql.trim()).replaceAll("\r\n");
	}

	BoundSql(String sql, SqlCache sqlCache, String cacheName, long ttl) {
		this(sql);
		this.sqlCache = sqlCache;
		this.cacheName = cacheName;
		this.ttl = ttl;
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
	 * 设置要执行的参数
	 */
	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * 获取要执行的参数
	 */
	public Object[] getParameters() {
		return parameters.toArray();
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