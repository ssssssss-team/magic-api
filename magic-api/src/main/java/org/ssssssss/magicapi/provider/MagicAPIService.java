package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.magicapi.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	 * @param context 变量信息
	 */
	Object execute(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的接口,带code和message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 变量信息
	 */
	Object call(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的函数
	 *
	 * @param path    函数路径
	 * @param context 变量信息
	 */
	Object invoke(String path, Map<String, Object> context);

	/**
	 * 保存接口
	 *
	 * @return 保存成功后，返回Id
	 */
	String saveApi(ApiInfo apiInfo);

	/**
	 * 获取接口详情
	 *
	 * @param id 接口id
	 */
	ApiInfo getApiInfo(String id);

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
	 * 获取函数详情
	 *
	 * @param id 函数id
	 */
	FunctionInfo getFunctionInfo(String id);

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

	/**
	 * 查询分组详情
	 * @param id	分组ID
	 */
	Group getGroup(String id);

	/**
	 * 注册数据源
	 */
	void registerAllDataSource();

	/**
	 * 获取数据源详情
	 *
	 * @param id 数据源id
	 */
	DataSourceInfo getDataSource(String id);

	/**
	 * 数据源列表
	 */
	List<DataSourceInfo> datasourceList();

	/**
	 * 测试数据源
	 *
	 * @param properties 数据源属性
	 * @return 返回错误说明，连接正常返回 null
	 */
	String testDataSource(DataSourceInfo properties);

	/**
	 * 保存数据源
	 *
	 * @param properties 数据源属性
	 * @return 返回数据源ID
	 */
	String saveDataSource(DataSourceInfo properties);

	/**
	 * 删除数据源
	 *
	 * @param id 数据源id
	 */
	boolean deleteDataSource(String id);


	/**
	 * 上传
	 */
	void upload(InputStream inputStream, String mode) throws IOException;

	/**
	 * 下载
	 */
	void download(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException;

	JsonBean<?> push(String target, String secretKey, String mode, List<SelectedResource> resources);

	/**
	 * 处理刷新通知
	 */
	boolean processNotify(MagicNotify magicNotify);
}
