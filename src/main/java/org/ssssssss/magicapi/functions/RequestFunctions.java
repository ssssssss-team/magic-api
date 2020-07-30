package org.ssssssss.magicapi.functions;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;
import org.ssssssss.magicapi.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * request 模块
 */
public class RequestFunctions {

	/**
	 * 获取文件信息
	 *
	 * @param name 参数名
	 */
	public MultipartFile getFile(String name) {
		MultipartRequest request = getMultipartHttpServletRequest();
		return request == null ? null : request.getFile(name);
	}

	/**
	 * 获取文件信息
	 *
	 * @param name 参数名
	 */
	public List<MultipartFile> getFiles(String name) {
		MultipartRequest request = getMultipartHttpServletRequest();
		return request == null ? null : request.getFiles(name);
	}

	/**
	 * 根据参数名获取参数值集合
	 *
	 * @param name 参数名
	 */
	public List<String> getValues(String name) {
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
	public List<String> getHeaders(String name) {
		HttpServletRequest request = get();
		if (request != null) {
			Enumeration<String> headers = request.getHeaders(name);
			return headers == null ? null : Collections.list(headers);
		}
		return null;
	}

	/**
	 * 获取原生HttpServletRequest对象
	 */
	public HttpServletRequest get() {
		return RequestContext.getHttpServletRequest();
	}

	private MultipartRequest getMultipartHttpServletRequest() {
		HttpServletRequest request = get();
		if (request != null && request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/")) {
			return WebUtils.getNativeRequest(request, MultipartRequest.class);
		}
		return null;
	}

}
