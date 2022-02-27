package org.ssssssss.magicapi.core.annotation;

import org.ssssssss.magicapi.core.interceptor.Authorization;

import java.lang.annotation.*;

/**
 * 接口验证信息
 *
 * @author mxd
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Valid {

	/**
	 * 验证是否有该权限
	 */
	Authorization authorization() default Authorization.NONE;

	/**
	 * 验证是否是只读模式
	 */
	boolean readonly() default true;

	/**
	 * 验证是否需要登录
	 */
	boolean requireLogin() default true;
}
