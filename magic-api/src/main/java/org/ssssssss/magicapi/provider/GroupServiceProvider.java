package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.TreeNode;
import org.ssssssss.magicapi.utils.PathUtils;

import java.util.List;

/**
 * 分组存储接口
 *
 * @author mxd
 */
public interface GroupServiceProvider {

	/**
	 * 添加分组
	 *
	 * @param group 分组信息
	 * @return 是否添加成功
	 */
	boolean insert(Group group);

	/**
	 * 修改分组
	 *
	 * @param group 分组信息
	 * @return 是否修改成功
	 */
	boolean update(Group group);

	/**
	 * 删除分组
	 *
	 * @param groupId 分组ID
	 * @return 是否删除成功
	 */
	boolean delete(String groupId);

	/**
	 * 分组是否存在
	 *
	 * @param group 分组信息
	 * @return 是否存在
	 */
	boolean exists(Group group);

	/**
	 * 是否有该分组
	 *
	 * @param groupId 分组ID
	 * @return 是否存在
	 */
	boolean containsApiGroup(String groupId);

	/**
	 * 读取分组信息
	 *
	 * @param resource 资源对象
	 * @return 分组信息
	 */
	Group readGroup(Resource resource);

	/**
	 * 接口分组列表
	 *
	 * @return 返回API分组的树结构
	 */
	TreeNode<Group> apiGroupTree();

	/**
	 * 函数分组列表
	 *
	 * @return 返回函数分组的树结构
	 */
	TreeNode<Group> functionGroupTree();

	/**
	 * 分组列表
	 *
	 * @param type 分组类型，Constants.GROUP_TYPE_API、Constants.GROUP_TYPE_FUNCTION
	 * @return 分组信息列表
	 */
	List<Group> groupList(String type);

	/**
	 * 已缓存的分组列表
	 *
	 * @param type 分组类型，Constants.GROUP_TYPE_API、Constants.GROUP_TYPE_FUNCTION
	 * @return 分组信息列表
	 */
	List<Group> cachedGroupList(String type);

	/**
	 * 根据分组Id获取分组路径
	 *
	 * @param groupId 分组ID
	 * @return 分组的完整路径
	 */
	String getFullPath(String groupId);

	/**
	 * 根据分组Id获取分组名称
	 *
	 * @param groupId 分组ID
	 * @return 分组的完整名称
	 */
	String getFullName(String groupId);

	/**
	 * 根据ID获取资源对象
	 *
	 * @param groupId 分组ID
	 * @return 资源对象
	 */
	Resource getGroupResource(String groupId);

	/**
	 * 获取脚本中的完整名称
	 *
	 * @param groupId 分组ID
	 * @param name    脚本名称
	 * @param path    脚本路径
	 * @return 完整名称
	 */
	default String getScriptName(String groupId, String name, String path) {
		return PathUtils.replaceSlash("/" + getFullName(groupId) + "/" + name) + "(" + PathUtils.replaceSlash(getFullPath(groupId) + "/" + path) + ")";
	}
}
