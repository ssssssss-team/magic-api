package org.ssssssss.magicapi.service;

import org.ssssssss.magicapi.model.MagicEntity;
import org.ssssssss.magicapi.provider.MagicResourceStorage;

import java.util.List;

public interface MagicDynamicRegistry<T extends MagicEntity> {

	/**
	 * 注册
	 */
	boolean register(T entity);

	/**
	 * 注册全部
	 */
	boolean register(List<T> entities);

	/**
	 * 取消注册
	 */
	boolean unregister(T entity);

	T getMapping(String mappingKey);

	/**
	 * 资源存储器
	 */
	MagicResourceStorage<T> getMagicResourceStorage();


}
