package org.ssssssss.magicapi.service;

import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.model.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 资源存储服务
 */
public interface MagicResourceService {

	/**
	 * 刷新缓存
	 */
	void refresh();

	Resource getResource();

	boolean processNotify(MagicNotify magicNotify);

	/**
	 * 保存分组
	 */
	boolean saveGroup(Group group);

	/**
	 * 移动
	 *
	 * @param src     源ID
	 * @param groupId 目标分组
	 */
	boolean move(String src, String groupId);

	/**
	 * 全部分组
	 */
	TreeNode<Group> tree(String type);

	/**
	 * 全部分组
	 */
	Map<String, TreeNode<Group>> tree();

	List<Group> getGroupsByFileId(String id);

	/**
	 * 获取分组 Resource
	 *
	 * @param id 分组ID
	 */
	Resource getGroupResource(String id);

	/**
	 * 保存文件
	 *
	 * @param entity 文件内容
	 */
	<T extends MagicEntity> boolean saveFile(T entity);

	/**
	 * 删除文件或文件夹
	 *
	 * @param id 文件或分组ID
	 */
	boolean delete(String id);

	/**
	 * 获取目录下的所有文件
	 *
	 * @param groupId 分组ID
	 */
	<T extends MagicEntity> List<T> listFiles(String groupId);

	/**
	 * 获取所有文件
	 *
	 * @param type 类型
	 */
	<T extends MagicEntity> List<T> files(String type);

	<T extends MagicEntity> T file(String id);

	void export(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException;

	boolean lock(String id);

	boolean unlock(String id);


	public String getGroupPath(String groupId);

	public String getGroupName(String groupId);


}
