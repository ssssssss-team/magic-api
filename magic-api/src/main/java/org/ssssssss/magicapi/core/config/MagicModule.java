package org.ssssssss.magicapi.core.config;

import org.ssssssss.script.annotation.UnableCall;

/**
 * 模块，主要用于import指令，import时根据模块名获取当前类如：<code>import assert</code>;
 *
 * @author mxd
 */
public interface MagicModule {

	/**
	 * 获取模块名
	 *
	 * @return 返回模块名称
	 */
	@UnableCall
	String getModuleName();
}
