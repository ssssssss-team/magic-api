package org.ssssssss.magicapi.functions;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.script.functions.ObjectConvertExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * response模块
 */
public class ResponseFunctions {

	private ResultProvider resultProvider;

	public ResponseFunctions(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	/**
	 * 自行构建分页结果
	 *
	 * @param total  条数
	 * @param values 数据内容
	 */
	public Object page(long total, List<Object> values) {
		return resultProvider.buildPageResult(total, values);
	}

	/**
	 * 自定义json结果
	 *
	 * @param value json内容
	 */
	public ResponseEntity json(Object value) {
		return ResponseEntity.ok(value);
	}

	/**
	 * 添加Header
	 */
	public void addHeader(String key, String value) {
		if (StringUtils.isNotBlank(key)) {
			HttpServletResponse response = getResponse();
			if (response != null) {
				response.addHeader(key, value);
			}
		}
	}

	/**
	 * 设置header
	 */
	public void setHeader(String key, String value) {
		if (StringUtils.isNotBlank(key)) {
			HttpServletResponse response = getResponse();
			if (response != null) {
				response.setHeader(key, value);
			}
		}
	}

	/**
	 * 添加cookie
	 */
	public void addCookie(String name, String value) {
		if (StringUtils.isNotBlank(name)) {
			addCookie(new Cookie(name, value));
		}
	}

	/**
	 * 批量添加cookie
	 */
	public void addCookies(Map<String, String> cookies, Map<String, Object> options) {
		if (cookies != null) {
			for (Map.Entry<String, String> entry : cookies.entrySet()) {
				addCookie(entry.getKey(), entry.getValue(), options);
			}
		}
	}

	/**
	 * 批量添加cookie
	 */
	public void addCookies(Map<String, String> cookies) {
		addCookies(cookies, null);
	}

	/**
	 * 添加cookie
	 */
	public void addCookie(String name, String value, Map<String, Object> options) {
		if (StringUtils.isNotBlank(name)) {
			Cookie cookie = new Cookie(name, value);
			if (options != null) {
				Object path = options.get("path");
				if (path != null) {
					cookie.setPath(path.toString());
				}
				Object httpOnly = options.get("httpOnly");
				if (httpOnly != null) {
					cookie.setHttpOnly("true".equalsIgnoreCase(httpOnly.toString()));
				}
				Object domain = options.get("domain");
				if (domain != null) {
					cookie.setDomain(domain.toString());
				}
				Object maxAge = options.get("maxAge");
				int age;
				if (maxAge != null && (age = ObjectConvertExtension.asInt(maxAge, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
					cookie.setMaxAge(age);
				}
			}
			addCookie(cookie);
		}
	}

	public NullValue end(){
		return NullValue.INSTANCE;
	}

	/**
	 * 添加cookie
	 */
	public void addCookie(Cookie cookie) {
		if (cookie != null) {
			HttpServletResponse response = getResponse();
			if (response != null) {
				response.addCookie(cookie);
			}
		}
	}

	private HttpServletResponse getResponse() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) requestAttributes).getResponse();
		}
		return null;
	}

	/**
	 * 展示图片
	 *
	 * @param value 图片内容
	 * @param mime  图片类型，image/png,image/jpeg,image/gif
	 */
	public ResponseEntity image(Object value, String mime) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mime).body(value);
	}

	/**
	 * 文件下载
	 *
	 * @param value    文件内容
	 * @param filename 文件名
	 */
	public ResponseEntity download(Object value, String filename) throws UnsupportedEncodingException {
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"))
				.body(value);
	}

	public static class NullValue{
		static final NullValue INSTANCE = new NullValue();
	}
}
