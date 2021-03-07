package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.modules.BoundSql;

/**
 * SQL 拦截器
 */
public interface SQLInterceptor {

	/**
	 * 执行SQL之前
	 */
	void preHandle(BoundSql boundSql);

}
