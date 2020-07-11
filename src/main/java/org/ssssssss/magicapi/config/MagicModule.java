package org.ssssssss.magicapi.config;

/**
 * 模块，主要用于import指令，import时根据模块名获取当前类如：import assert;
 */
public interface MagicModule {

	/**
	 * 获取模块名
	 */
	public String getModuleName();
}
