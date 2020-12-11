package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.ApiInfo;

/**
 * API存储接口
 */
public interface ApiServiceProvider extends StoreServiceProvider<ApiInfo> {


	/**
	 * 判断接口是否存在
	 *
	 * @param groupId 分组Id
	 * @param method  请求方法
	 * @param path    请求路径
	 */
	boolean exists(String groupId, String method, String path);

	/**
	 * 判断接口是否存在
	 *
	 * @param groupId 分组ID
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param id      排除接口
	 */
	boolean existsWithoutId(String groupId, String method, String path, String id);


}
