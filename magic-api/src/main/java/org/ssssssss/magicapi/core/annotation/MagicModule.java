package org.ssssssss.magicapi.core.annotation;

import java.lang.annotation.*;

/**
 * 模块，主要用于import指令，import时根据模块名获取当前类如：<code>import assert</code>;
 *
 * @author mxd
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MagicModule {

	/**
	 * 模块名
	 */
	String value();
}
