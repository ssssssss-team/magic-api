package org.ssssssss.magicapi.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.model.RequestEntity;
import org.ssssssss.magicapi.modules.BoundSql;
import org.ssssssss.script.MagicScriptContext;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 默认打印SQL实现
 *
 * @author mxd
 */
public class DefaultSqlInterceptor implements SQLInterceptor {

	@Override
	public void preHandle(BoundSql boundSql, RequestEntity requestEntity) {
		Logger logger = LoggerFactory.getLogger(MagicScriptContext.get().getScriptName());
		String parameters = Arrays.stream(boundSql.getParameters()).map(it -> {
			if (it == null) {
				return "null";
			}
			return it + "(" + it.getClass().getSimpleName() + ")";
		}).collect(Collectors.joining(", "));
		String dataSourceName = boundSql.getSqlModule().getDataSourceName();
		logger.info("执行SQL：{}", boundSql.getSql().trim());
		if (dataSourceName != null) {
			logger.info("数据源：{}", dataSourceName);
		}
		if (parameters.length() > 0) {
			logger.info("SQL参数：{}", parameters);
		}
	}
}
