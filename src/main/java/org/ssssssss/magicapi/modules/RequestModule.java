package org.ssssssss.magicapi.modules;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.script.annotation.Comment;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * request 模块
 */
public class RequestModule {

	/**
	 * 获取文件信息
	 *
	 * @param name 参数名
	 */
	@Comment("获取文件")
	public MultipartFile getFile(@Comment("参数名") String name) {
		MultipartRequest request = getMultipartHttpServletRequest();
		return request == null ? null : request.getFile(name);
	}

	/**
	 * 获取文件信息
	 *
	 * @param name 参数名
	 */
	@Comment("获取多个文件")
	public List<MultipartFile> getFiles(@Comment("参数名") String name) {
		MultipartRequest request = getMultipartHttpServletRequest();
		return request == null ? null : request.getFiles(name);
	}

	/**
	 * 根据参数名获取参数值集合
	 *
	 * @param name 参数名
	 */
	@Comment("根据请求参数名获取值")
	public List<String> getValues(@Comment("参数名") String name) {
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
	public List<String> getHeaders(@Comment("header名") String name) {
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
