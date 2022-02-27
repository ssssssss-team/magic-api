package org.ssssssss.magicapi.core.service;

import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * API调用接口
 */
public interface MagicAPIService {

	/**
	 * 执行MagicAPI中的接口,原始内容，不包含code以及message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 变量信息
	 */
	<T> T execute(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的接口,带code和message信息
	 *
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param context 变量信息
	 */
	<T> T call(String method, String path, Map<String, Object> context);

	/**
	 * 执行MagicAPI中的函数
	 *
	 * @param path    函数路径
	 * @param context 变量信息
	 */
	<T> T invoke(String path, Map<String, Object> context);


	/**
	 * 上传
	 */
	boolean upload(InputStream inputStream, String mode) throws IOException;

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
