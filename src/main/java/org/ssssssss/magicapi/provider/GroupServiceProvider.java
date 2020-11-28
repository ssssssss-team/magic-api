package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.TreeNode;

import java.util.List;

public interface GroupServiceProvider {

	/**
	 * 添加分组
	 */
	boolean insert(Group group);

	/**
	 * 修改分组
	 */
	boolean update(Group group);

	/**
	 * 删除分组
	 */
	boolean delete(String groupId);

	/**
	 * 是否有该分组
	 */
	boolean contains(String groupId);

	/**
	 * 接口分组列表
	 */
	TreeNode<Group> apiGroupList();

	/**
	 * 分组列表
	 */
	List<Group> groupList();

	/**
	 * 根据分组Id获取分组路径
	 */
	String getFullPath(String groupId);

	/**
	 * 根据分组Id获取分组名称
	 */
	String getFullName(String groupId);
}
