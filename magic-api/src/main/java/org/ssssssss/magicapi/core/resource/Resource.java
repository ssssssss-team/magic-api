package org.ssssssss.magicapi.core.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 资源对象接口
 *
 * @author mxd
 */
public interface Resource {

	/**
	 * 判断是否是只读
	 *
	 * @return 返回资源是否是只读
	 */
	default boolean readonly() {
		return false;
	}

	/**
	 * 判断是否存在
	 *
	 * @return 返回资源是否存在
	 */
	default boolean exists() {
		return false;
	}

	/**
	 * 判断是否是目录
	 *
	 * @return 返回资源是否是目录
	 */
	default boolean isDirectory() {
		return false;
	}

	/**
	 * 删除
	 *
	 * @return 返回是否删除成功
	 */
	default boolean delete() {
		return false;
	}

	/**
	 * 创建目录
	 *
	 * @return 返回是否创建成功
	 */
	default boolean mkdir() {
		return false;
	}

	/**
	 * 重命名
	 *
	 * @param resource 目标资源
	 * @return 是否重命名成功
	 */
	default boolean renameTo(Resource resource) {
		return false;
	}

	/**
	 * 写入
	 *
	 * @param content 写入的内容
	 * @return 是否写入成功
	 */
	default boolean write(String content) {
		return false;
	}

	/**
	 * 写入
	 *
	 * @param bytes 写入的内容
	 * @return 是否写入成功
	 */
	default boolean write(byte[] bytes) {
		return false;
	}

	/**
	 * 获取分隔符
	 *
	 * @return 返回分隔符
	 */
	default String separator() {
		return null;
	}

	/**
	 * 处理导出
	 *
	 * @param zos       zip 输出流
	 * @param path      路径
	 * @param directory 目录资源对象
	 * @param resources 资源集合
	 * @param excludes  排除的目录
	 * @throws IOException 处理过程中抛出的异常
	 */
	default void processExport(ZipOutputStream zos, String path, Resource directory, List<Resource> resources, List<String> excludes) throws IOException {
		for (Resource resource : resources) {
			String fullName = directory.getAbsolutePath();
			if (!fullName.endsWith(separator())) {
				fullName += separator();
			}
			fullName += resource.name();
			if (resource.isDirectory()) {
				fullName += separator();
			}
			if (fullName.equals(resource.getAbsolutePath()) && !excludes.contains(resource.name())) {
				if (resource.isDirectory()) {
					String newPath = path + resource.name() + "/";
					zos.putNextEntry(new ZipEntry(newPath));
					zos.closeEntry();
					processExport(zos, newPath, resource, resources, excludes);
				} else {
					zos.putNextEntry(new ZipEntry(path + resource.name()));
					zos.write(resource.read());
					zos.closeEntry();
				}
			}
		}
	}

	/**
	 * 处理导出
	 *
	 * @param os       输出流
	 * @param excludes 排除的目录
	 * @throws IOException 处理过程中抛出的异常
	 */
	default void export(OutputStream os, String... excludes) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(os);
		processExport(zos, "", this, resources(), Arrays.asList(excludes == null ? new String[0] : excludes));
		zos.close();
	}

	/**
	 * 读取
	 *
	 * @return 读取的资源内容
	 */
	byte[] read();

	/**
	 * 读取当前资源下的所有内容,主要是缓存作用。
	 */
	default void readAll() {
	}

	/**
	 * 获取子目录
	 *
	 * @param name 目录名称
	 * @return 返回资源对象
	 */
	default Resource getDirectory(String name) {
		return getResource(name);
	}

	/**
	 * 获取子资源
	 *
	 * @param name 文件名称
	 * @return 返回资源对象
	 */
	Resource getResource(String name);

	/**
	 * 获取资源名
	 *
	 * @return 返回资源名称
	 */
	String name();

	/**
	 * 获取子资源集合
	 *
	 * @return 返回资源集合
	 */
	List<Resource> resources();

	/**
	 * 父级资源
	 *
	 * @return 返回父级资源
	 */
	Resource parent();

	/**
	 * 目录
	 *
	 * @return 返回当前资源下的目录
	 */
	List<Resource> dirs();

	/**
	 * 遍历文件
	 *
	 * @param suffix 文件名后缀
	 * @return 返回当前资源下的文件
	 */
	List<Resource> files(String suffix);

	/**
	 * 获取所在位置
	 *
	 * @return 获取绝对路径
	 */
	String getAbsolutePath();

	/**
	 * 获取文件路径
	 *
	 * @return 返回文件路径
	 */
	String getFilePath();

}
