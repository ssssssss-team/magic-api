package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.Group;

import java.util.List;
import java.util.Map;

/**
 * API调用接口
 */
public interface MagicAPIService extends MagicModule {

	/**
	 * 执行MagicAPI中的接口,原始内容，不包含code以及message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 请求上下文，主要给脚本中使用
	 */
	Object execute(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的接口,带code和message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 请求上下文，主要给脚本中使用
	 */
	Object call(String method, String path, Map<String, Object> context);

	/**
	 * 保存接口
	 *
	 * @return 保存成功后，返回Id
	 */
	String saveApi(ApiInfo apiInfo);

	/**
	 * 获取接口列表
	 */
	List<ApiInfo> apiList();

	/**
	 * 删除接口
	 *
	 * @param id 接口id
	 */
	boolean deleteApi(String id);

	/**
	 * 移动接口
	 *
	 * @param id      接口id
	 * @param groupId 分组id
	 */
	boolean moveApi(String id, String groupId);

	/**
	 * 保存函数
	 *
	 * @return 保存成功后，返回Id
	 */
	String saveFunction(FunctionInfo functionInfo);

	/**
	 * 获取函数列表
	 */
	List<FunctionInfo> functionList();

	/**
	 * 删除函数
	 *
	 * @param id 函数id
	 */
	boolean deleteFunction(String id);

	/**
	 * 移动函数
	 *
	 * @param id      函数id
	 * @param groupId 分组id
	 */
	boolean moveFunction(String id, String groupId);


	/**
	 * 创建分组
	 *
	 * @param group 分组信息
	 * @return 创建成功后返回分组id
	 */
	String createGroup(Group group);

	/**
	 * 修改分组信息
	 *
	 * @param group 分组信息
	 */
	boolean updateGroup(Group group);

	/**
	 * 删除分组
	 *
	 * @param id 分组id
	 */
	boolean deleteGroup(String id);

	/**
	 * 查询分组列表
	 *
	 * @param type 分组类型，1 接口分组，2 函数分组
	 */
	List<Group> groupList(String type);
}
