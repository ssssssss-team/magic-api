package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.config.ApiInfo;

import java.util.List;

public interface ApiServiceProvider {
	/**
	 * 删除接口
	 *
	 * @param id
	 */
	boolean delete(String id);

	/**
	 * 根据组名删除接口
	 *
	 * @param groupName 	分组名称
	 */
	boolean deleteGroup(String groupName);

	/**
	 * 查询所有接口（提供给页面,无需带script）
	 *
	 */
	List<ApiInfo> list();

	/**
	 * 查询所有接口（内部使用，需要带Script）
	 *
	 */
	List<ApiInfo> listWithScript();

	/**
	 * 查询接口详情（主要给页面使用）
	 *
	 * @param id
	 */
	ApiInfo get(String id);

	/**
	 * 判断接口是否存在
	 *
	 * @param method 请求方法
	 * @param path   请求路径
	 */
	boolean exists(String method, String path);

	/**
	 * 修改分组信息
	 * @param oldGroupName	旧分组名称
	 * @param groupName	新分组名称
	 * @param groupPrefix	分组前缀
	 */
	boolean updateGroup(String oldGroupName,String groupName, String groupPrefix);

	/**
	 * 判断接口是否存在
	 *
	 * @param method 请求方法
	 * @param path   请求路径
	 * @param id     排除接口
	 * @return
	 */
	boolean existsWithoutId(String method, String path, String id);

	/**
	 * 添加接口信息
	 *
	 * @param info
	 * @return
	 */
	boolean insert(ApiInfo info);

	/**
	 * 修改接口信息
	 *
	 * @param info
	 * @return
	 */
	boolean update(ApiInfo info);

	/**
	 * 备份历史记录
	 *
	 * @param apiId
	 */
	void backup(String apiId);

	/**
	 * 查询API历史记录
	 * @param apiId
	 * @return 时间戳列表
	 */
	List<Long> backupList(String apiId);

	/**
	 * 查询API历史记录详情
	 * @param apiId
	 * @param timestamp 时间戳
	 */
	ApiInfo backupInfo(String apiId, Long timestamp);

	/**
	 * 包装接口信息（可用于加密）
	 *
	 * @param info
	 */
	default void wrap(ApiInfo info) {
	}

	/**
	 * 解除包装接口信息（可用于解密）
	 *
	 * @param info
	 */
	default void unwrap(ApiInfo info) {
	}
}
