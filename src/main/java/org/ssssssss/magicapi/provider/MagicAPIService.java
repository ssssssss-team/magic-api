package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.config.MagicModule;

import java.util.Map;

/**
 * API调用接口
 */
public interface MagicAPIService extends MagicModule {

	/**
	 * 执行MagicAPI中的接口,原始内容，不包含code以及message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 请求上下文，主要给脚本中使用
	 */
	public Object execute(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的接口,带code和message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 请求上下文，主要给脚本中使用
	 */
	Object call(String method, String path, Map<String, Object> context);
}
