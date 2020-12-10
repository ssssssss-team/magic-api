package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.modules.BoundSql;

public interface SQLInterceptor {

	void preHandle(BoundSql boundSql);

}
