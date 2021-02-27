package org.ssssssss.magicapi.adapter;

import java.util.List;

public interface Resource {

	/**
	 * 是否是只读
	 */
	default boolean readonly(){
		return false;
	}

	/**
	 * 是否存在
	 */
	default boolean exists(){
		return false;
	}

	/**
	 * 是否是目录
	 */
	default boolean isDirectory(){
		return false;
	}

	/**
	 * 删除
	 */
	default boolean delete(){
		return false;
	}

	/**
	 * 创建目录
	 */
	default boolean mkdir(){
		return false;
	}

	/**
	 * 重命名
	 */
	default boolean renameTo(Resource resource){
		return false;
	}

	/**
	 * 写入
	 */
	default boolean write(String content){
		return false;
	}
	/**
	 * 写入
	 */
	default boolean write(byte[] bytes){
		return false;
	}

	/**
	 * 读取
	 */
	byte[] read();

	/**
	 * 获取子目录
	 */
	default Resource getDirectory(String name){
		return getResource(name);
	}

	/**
	 * 获取子资源
	 */
	Resource getResource(String name);

	/**
	 * 获取资源名
	 */
	String name();

	/**
	 * 获取子资源集合
	 */
	List<Resource> resources();

	/**
	 * 父级资源
	 */
	Resource parent();

	/**
	 * 目录
	 */
	List<Resource> dirs();

	/**
	 * 遍历文件
	 */
	List<Resource> files(String suffix);

	/**
	 * 获取所在位置
	 */
	String getAbsolutePath();

}
