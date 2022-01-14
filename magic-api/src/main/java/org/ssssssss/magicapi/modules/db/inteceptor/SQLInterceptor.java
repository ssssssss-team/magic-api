package org.ssssssss.magicapi.modules.db.inteceptor;

import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.modules.db.BoundSql;

/**
 * SQL 拦截器
 *
 * @author mxd
 */
public interface SQLInterceptor {

	/**
	 * 1.1.1 新增
	 *
	 * @since 1.1.1
	 * @param boundSql      SQL信息
	 * @param requestEntity 请求信息
	 */
	default void preHandle(BoundSql boundSql, RequestEntity requestEntity) {

	}

	/**
	 * @since 1.7.2
	 * @param boundSql	SQL信息
	 * @param result	执行结果
	 * @param requestEntity	请求信息
	 */
	default Object postHandle(BoundSql boundSql, Object result, RequestEntity requestEntity){
		return result;
	}


}
