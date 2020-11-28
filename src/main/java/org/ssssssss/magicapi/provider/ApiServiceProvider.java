package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.ApiInfo;

import java.util.List;

/**
 * API存储接口
 */
public interface ApiServiceProvider {
	/**
	 * 删除接口
	 *
	 * @param id 接口ID
	 */
	boolean delete(String id);

	/**
	 * 查询所有接口（提供给页面,无需带script）
	 */
	List<ApiInfo> list();

	/**
	 * 查询所有接口（内部使用，需要带Script）
	 */
	List<ApiInfo> listWithScript();

	/**
	 * 查询接口详情（主要给页面使用）
	 *
	 * @param id 接口ID
	 */
	ApiInfo get(String id);

	/**
	 * 移动接口
	 *
	 * @param id      接口ID
	 * @param groupId 分组ID
	 */
	boolean move(String id, String groupId);

	/**
	 * 根据组ID删除
	 */
	boolean deleteGroup(String groupId);

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

	/**
	 * 添加接口信息
	 *
	 * @param info 接口信息
	 */
	boolean insert(ApiInfo info);

	/**
	 * 修改接口信息
	 *
	 * @param info 接口信息
	 */
	boolean update(ApiInfo info);

	/**
	 * 备份历史记录
	 *
	 * @param apiId 接口ID
	 */
	void backup(String apiId);


	/**
	 * 查询API历史记录
	 *
	 * @param apiId 接口ID
	 * @return 时间戳列表
	 */
	List<Long> backupList(String apiId);

	/**
	 * 查询API历史记录详情
	 *
	 * @param apiId     接口ID
	 * @param timestamp 时间戳
	 */
	ApiInfo backupInfo(String apiId, Long timestamp);

	/**
	 * 包装接口信息（可用于加密）
	 *
	 * @param info 接口信息
	 */
	default void wrap(ApiInfo info) {
	}

	/**
	 * 解除包装接口信息（可用于解密）
	 *
	 * @param info 接口信息
	 */
	default void unwrap(ApiInfo info) {
	}
}
