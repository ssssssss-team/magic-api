package org.ssssssss.magicapi.functions;

import org.ssssssss.magicapi.cache.SqlCache;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.parsing.GenericTokenParser;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class BoundSql {

	private static final GenericTokenParser concatTokenParser = new GenericTokenParser("${", "}", false);

	private static final GenericTokenParser replaceTokenParser = new GenericTokenParser("#{", "}", true);

	private static final GenericTokenParser ifTokenParser = new GenericTokenParser("?{", "}", true);

	private static final GenericTokenParser ifParamTokenParser = new GenericTokenParser("?{", ",", true);

	private static final Pattern REPLACE_MULTI_WHITE_LINE = Pattern.compile("(\r?\n(\\s*\r?\n)+)");

	private String sql;

	private List<Object> parameters = new ArrayList<>();

	private String cacheKey;


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
	 * 获取要执行的参数
	 */
	public Object[] getParameters() {
		return parameters.toArray();
	}

	/**
	 * 清空缓存key
	 */
	public BoundSql removeCacheKey() {
		this.cacheKey = null;
		return this;
	}

	/**
	 * 获取缓存key
	 */
	public String getCacheKey(SqlCache sqlCache) {
		if (cacheKey == null) {
			cacheKey = sqlCache.buildSqlCacheKey(this);
		}
		return cacheKey;
	}

	/**
	 * 获取缓存值
	 */
	public <T> Optional<T> getCacheValue(SqlCache sqlCache, String cacheName) {
		return Optional.ofNullable(cacheName == null ? null : sqlCache.get(cacheName, getCacheKey(sqlCache)));
	}
}