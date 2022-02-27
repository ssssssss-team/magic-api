package org.ssssssss.magicapi.modules.servlet;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.utils.IpUtils;
import org.ssssssss.script.annotation.Comment;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * request 模块
 *
 * @author mxd
 */
@MagicModule("request")
public class RequestModule {

	private static MultipartResolver resolver;

	private static final String[] DEFAULT_IP_HEADER = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

	public RequestModule(MultipartResolver resolver) {
		RequestModule.resolver = resolver;
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
	public static HttpServletRequest get() {
		return org.ssssssss.magicapi.utils.WebUtils.getRequest().orElse(null);
	}

	private static MultipartRequest getMultipartHttpServletRequest() {
		HttpServletRequest request = get();
		if (request != null && resolver.isMultipart(request)) {
			return resolver.resolveMultipart(request);
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
		HttpServletRequest request = get();
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
		HttpServletRequest request = get();
		if (request != null) {
			Enumeration<String> headers = request.getHeaders(name);
			return headers == null ? null : Collections.list(headers);
		}
		return null;
	}

	@Comment("获取客户端IP")
	public String getClientIP(String... otherHeaderNames) {
		HttpServletRequest request = get();
		if (request == null) {
			return null;
		}
		return IpUtils.getRealIP(request.getRemoteAddr(), request::getHeader, otherHeaderNames);
	}
}
