package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.ApiInfo;

import java.util.List;

public interface StoreServiceProvider<T> {

	/**
	 * 添加接口信息
	 *
	 * @param info 接口信息
	 */
	boolean insert(T info);

	/**
	 * 修改接口信息
	 */
	boolean update(T info);

	/**
	 * 备份历史记录
	 *
	 * @param apiId 接口ID
	 */
	void backup(String apiId);


	/**
	 * 查询历史记录
	 *
	 * @return 时间戳列表
	 */
	List<Long> backupList(String id);

	/**
	 * 查询历史记录详情
	 *
	 * @param id        ID
	 * @param timestamp 时间戳
	 */
	ApiInfo backupInfo(String id, Long timestamp);

	/**
	 * 删除接口
	 */
	boolean delete(String id);

	/**
	 * 查询所有（提供给页面,无需带script）
	 */
	List<T> list();

	/**
	 * 查询所有（内部使用，需要带Script）
	 */
	List<T> listWithScript();

	/**
	 * 查询接口详情（主要给页面使用）
	 *
	 * @param id 接口ID
	 */
	T get(String id);

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
	boolean deleteGroup(List<String> groupIds);

	/**
	 * 包装接口信息（可用于加密）
	 */
	default void wrap(T info) {
	}

	/**
	 * 解除包装接口信息（可用于解密）
	 */
	default void unwrap(T info) {
	}
}
