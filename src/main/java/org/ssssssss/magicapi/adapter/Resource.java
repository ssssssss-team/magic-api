package org.ssssssss.magicapi.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public interface Resource {

	/**
	 * 是否是只读
	 */
	default boolean readonly() {
		return false;
	}

	/**
	 * 是否存在
	 */
	default boolean exists() {
		return false;
	}

	/**
	 * 是否是目录
	 */
	default boolean isDirectory() {
		return false;
	}

	/**
	 * 删除
	 */
	default boolean delete() {
		return false;
	}

	/**
	 * 创建目录
	 */
	default boolean mkdir() {
		return false;
	}

	/**
	 * 重命名
	 */
	default boolean renameTo(Resource resource) {
		return false;
	}

	/**
	 * 写入
	 */
	default boolean write(String content) {
		return false;
	}

	/**
	 * 写入
	 */
	default boolean write(byte[] bytes) {
		return false;
	}

	default void processExport(ZipOutputStream zos, String path, Resource directory, List<Resource> resources, List<String> excludes) throws IOException {
		for (Resource resource : resources) {
			if (resource.parent().getAbsolutePath().equals(directory.getAbsolutePath()) && !excludes.contains(resource.name())) {
				if (resource.isDirectory()) {
					String newPath = path + resource.name() + "/";
					zos.putNextEntry(new ZipEntry(newPath));
					zos.closeEntry();
					processExport(zos, newPath, resource, resource.resources(), excludes);
				} else {
					zos.putNextEntry(new ZipEntry(path + resource.name()));
					zos.write(resource.read());
					zos.closeEntry();
				}
			}
		}
	}

	default void export(OutputStream os, String... excludes) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(os);
		processExport(zos, "", this, resources(), Arrays.asList(excludes == null ? new String[0] : excludes));
		zos.close();
	}

	/**
	 * 读取
	 */
	byte[] read();

	/**
	 * 读取当前资源下的所有内容,主要是缓存作用。
	 */
	default void readAll() {
	}

	/**
	 * 获取子目录
	 */
	default Resource getDirectory(String name) {
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
