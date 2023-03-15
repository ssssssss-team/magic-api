package org.ssssssss.magicapi.modules.servlet;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.servlet.MagicRequestContextHolder;
import org.ssssssss.magicapi.utils.IpUtils;
import org.ssssssss.script.annotation.Comment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * request 模块
 *
 * @author mxd
 */
@MagicModule("request")
public class RequestModule {

	private static MagicRequestContextHolder magicRequestContextHolder;


	public RequestModule(MagicRequestContextHolder magicRequestContextHolder) {
		RequestModule.magicRequestContextHolder = magicRequestContextHolder;
	}

	/**
	 * 获取文件信息
	 *
	 * @param name 参数名
	 */
	@Comment("获取文件")
	public static MultipartFile getFile(@Comment(name = "name", value = "参数名") String name) {
		MultipartRequest request = getMultipartHttpServletRequest();
		if (request == null) {
			return null;
		}
		MultipartFile file = request.getFile(name);
		return file == null || file.isEmpty() ? null : file;
	}

	/**
	 * 获取文件信息
	 *
	 * @param name 参数名
	 */
	@Comment("获取多个文件")
	public static List<MultipartFile> getFiles(@Comment(name = "name", value = "参数名") String name) {
		MultipartRequest request = getMultipartHttpServletRequest();
		if (request == null) {
			return null;
		}
		return request.getFiles(name).stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());
	}

	/**
	 * 获取原生HttpServletRequest对象
	 */
	@Comment("获取原生HttpServletRequest对象")
	public static MagicHttpServletRequest get() {
		return magicRequestContextHolder.getRequest();
	}

	private static MultipartRequest getMultipartHttpServletRequest() {
		MagicHttpServletRequest request = get();
		if (request != null && request.isMultipart()) {
			return request.resolveMultipart();
		}
		return null;
	}

	/**
	 * 根据参数名获取参数值集合
	 *
	 * @param name 参数名
	 */
	@Comment("根据请求参数名获取值")
	public List<String> getValues(@Comment(name = "name", value = "参数名") String name) {
		MagicHttpServletRequest request = get();
		if (request != null) {
			String[] values = request.getParameterValues(name);
			return values == null ? null : Arrays.asList(values);
		}
		return null;
	}

	/**
	 * 根据header名获取header集合
	 *
	 * @param name 参数名
	 */
	@Comment("根据header名获取值")
	public List<String> getHeaders(@Comment(name = "name", value = "header名") String name) {
		MagicHttpServletRequest request = get();
		if (request != null) {
			Enumeration<String> headers = request.getHeaders(name);
			return headers == null ? null : Collections.list(headers);
		}
		return null;
	}

	@Comment("获取客户端IP")
	public String getClientIP(String... otherHeaderNames) {
		MagicHttpServletRequest request = get();
		if (request == null) {
			return null;
		}
		return IpUtils.getRealIP(request.getRemoteAddr(), request::getHeader, otherHeaderNames);
	}
}
