package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.model.RequestEntity;
import org.ssssssss.magicapi.modules.BoundSql;

/**
 * SQL 拦截器
 *
 * @author mxd
 */
public interface SQLInterceptor {

	/**
	 * 1.1.1 新增
	 *
	 * @param boundSql      SQL信息
	 * @param requestEntity 请求信息
	 */
	void preHandle(BoundSql boundSql, RequestEntity requestEntity);


}
