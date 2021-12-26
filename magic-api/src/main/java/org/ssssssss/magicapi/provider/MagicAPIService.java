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
 *
 * @author mxd
 */
public interface MagicAPIService extends MagicModule {

	/**
	 * 执行MagicAPI中的接口,原始内容，不包含code以及message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 变量信息
	 * @return 返回执行结果
	 */
	<T> T execute(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的接口,带code和message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 变量信息
	 * @return 返回执行结果，包装JsonBena处理
	 */
	<T> T call(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的函数
	 *
	 * @param path    函数路径
	 * @param context 变量信息
	 * @return 返回函数执行结果
	 */
	<T> T invoke(String path, Map<String, Object> context);

	/**
	 * 保存接口
	 *
	 * @param apiInfo 接口信息
	 * @return 保存成功后，返回Id
	 */
	String saveApi(ApiInfo apiInfo);

	/**
	 * 锁定接口
	 *
	 * @param id 接口ID
	 * @return 是否锁定成功
	 */
	boolean lockApi(String id);

	/**
	 * 解锁接口
	 *
	 * @param id 接口ID
	 * @return 是否解锁成功
	 */
	boolean unlockApi(String id);

	/**
	 * 锁定函数
	 *
	 * @param id 函数ID
	 * @return 是否锁定成功
	 */
	boolean lockFunction(String id);

	/**
	 * 解锁函数
	 *
	 * @param id 接口ID
	 * @return 是否解锁成功
	 */
	boolean unlockFunction(String id);

	/**
	 * 获取接口详情
	 *
	 * @param id 接口id
	 * @return 返回接口信息
	 */
	ApiInfo getApiInfo(String id);

	/**
	 * 获取接口列表
	 *
	 * @return 返回接口信息列表
	 */
	List<ApiInfo> apiList();

	/**
	 * 删除接口
	 *
	 * @param id 接口id
	 * @return 是否删除成功
	 */
	boolean deleteApi(String id);

	/**
	 * 移动接口
	 *
	 * @param id      接口id
	 * @param groupId 分组id
	 * @return 是否移动成功
	 */
	boolean moveApi(String id, String groupId);

	/**
	 * 保存函数
	 *
	 * @param functionInfo 函数信息
	 * @return 保存成功后，返回Id
	 */
	String saveFunction(FunctionInfo functionInfo);

	/**
	 * 获取函数详情
	 *
	 * @param id 函数id
	 * @return 返回函数信息
	 */
	FunctionInfo getFunctionInfo(String id);

	/**
	 * 获取函数列表
	 *
	 * @return 返回函数信息列表
	 */
	List<FunctionInfo> functionList();

	/**
	 * 删除函数
	 *
	 * @param id 函数id
	 * @return 是否删除成功
	 */
	boolean deleteFunction(String id);

	/**
	 * 移动函数
	 *
	 * @param id      函数id
	 * @param groupId 分组id
	 * @return 是否移动成功
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
	 * @return 是否修改成功
	 */
	boolean updateGroup(Group group);

	/**
	 * 删除分组
	 *
	 * @param id 分组id
	 * @return 是否删除成功
	 */
	boolean deleteGroup(String id);

	/**
	 * 查询分组列表
	 *
	 * @param type 分组类型，1 接口分组，2 函数分组
	 * @return 返回分组信息列表
	 */
	List<Group> groupList(String type);

	/**
	 * 查询分组详情
	 *
	 * @param id 分组ID
	 * @return 返回分组对象
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
	 * @return 返回数据源信息
	 */
	DataSourceInfo getDataSource(String id);

	/**
	 * 数据源列表
	 *
	 * @return 返回数据源信息列表
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
	 * @return 是否删除成功
	 */
	boolean deleteDataSource(String id);


	/**
	 * 上传
	 *
	 * @param inputStream 输入流
	 * @param mode        模式，全量和增量
	 * @throws IOException 读取失败抛出的异常
	 */
	void upload(InputStream inputStream, String mode) throws IOException;

	/**
	 * 下载
	 *
	 * @param groupId   分组ID
	 * @param resources 选择的资源对象
	 * @param os        输出流
	 * @throws IOException 下载失败时抛出的异常
	 */
	void download(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException;

	/**
	 * 远程推送
	 *
	 * @param target    推送目标
	 * @param secretKey 秘钥
	 * @param mode      推送模式
	 * @param resources 选择的资源
	 * @return 推送结果
	 */
	JsonBean<?> push(String target, String secretKey, String mode, List<SelectedResource> resources);

	/**
	 * 处理刷新通知
	 *
	 * @param magicNotify 通知对象
	 * @return 是否处理成功
	 */
	boolean processNotify(MagicNotify magicNotify);

	/**
	 * 复制分组
	 *
	 * @param src    源分组ID
	 * @param target 目标分组ID
	 * @return 新分组的ID
	 */
	String copyGroup(String src, String target);
}
