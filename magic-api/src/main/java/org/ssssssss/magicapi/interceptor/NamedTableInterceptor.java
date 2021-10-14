package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.model.SqlMode;
import org.ssssssss.magicapi.modules.table.NamedTable;

/**
 * 单表模块拦截器
 *
 * @since 1.5.3
 */
public interface NamedTableInterceptor {

	/**
	 * 执行之前
	 */
	void preHandle(SqlMode sqlMode, NamedTable namedTable);
}
