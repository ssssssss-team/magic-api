package org.ssssssss.magicapi.core.service;

import org.ssssssss.magicapi.core.resource.Resource;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.utils.PathUtils;

import java.io.IOException;
import java.io.InputStream;
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
	 * 复制分组
	 *
	 * @param src    源ID
	 * @param target 目标分组
	 */
	String copyGroup(String src, String target);

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

	/**
	 * 获取文件详情
	 */
	<T extends MagicEntity> T file(String id);

	/**
	 * 获取分组详情
	 */
	Group getGroup(String id);

	void export(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException;

	/**
	 * 锁定资源
	 */
	boolean lock(String id);

	/**
	 * 解锁资源
	 */
	boolean unlock(String id);

	boolean upload(InputStream inputStream, boolean full) throws IOException;

	/**
	 * 获取完整分组路径
	 */
	String getGroupPath(String groupId);

	/**
	 * 获取完整分组名称
	 */
	String getGroupName(String groupId);

	default String getScriptName(MagicEntity entity){
		String fullName;
		if(entity instanceof PathMagicEntity){
			PathMagicEntity pme = (PathMagicEntity) entity;
			fullName = String.format("/%s/%s(/%s/%s)", getGroupName(pme.getGroupId()), pme.getName(), getGroupPath(pme.getGroupId()), pme.getPath());
		} else {
			fullName = String.format("/%s/%s", getGroupName(entity.getGroupId()), entity.getName());
		}
		return PathUtils.replaceSlash(fullName);
	}


}
