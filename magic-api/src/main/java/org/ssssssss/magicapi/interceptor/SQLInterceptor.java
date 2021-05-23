package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.model.RequestEntity;
import org.ssssssss.magicapi.modules.BoundSql;

/**
 * SQL 拦截器
 */
public interface SQLInterceptor {

	/**
	 * 1.1.1 新增
	 */
	void preHandle(BoundSql boundSql, RequestEntity requestEntity);


}
