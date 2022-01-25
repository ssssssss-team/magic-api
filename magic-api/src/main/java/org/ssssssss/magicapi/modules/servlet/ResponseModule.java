package org.ssssssss.magicapi.modules.servlet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.context.RequestContext;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.ObjectConvertExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * response模块
 *
 * @author mxd
 */
@MagicModule("response")
public class ResponseModule {

	private final ResultProvider resultProvider;

	public ResponseModule(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	/**
	 * 文件下载
	 *
	 * @param value    文件内容
	 * @param filename 文件名
	 */
	@Comment("文件下载")
	public static ResponseEntity<?> download(@Comment(name = "value", value = "文件内容，如`byte[]`") Object value,
											 @Comment(name = "filename", value = "文件名") String filename) throws UnsupportedEncodingException {
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"))
				.body(value);
	}

	/**
	 * 自行构建分页结果
	 *
	 * @param total  条数
	 * @param values 数据内容
	 */
	@Comment("返回自定义分页结果")
	public Object page(@Comment(name = "total", value = "总条数") long total,
					   @Comment(name = "values", value = "当前结果集") List<Map<String, Object>> values) {
		return resultProvider.buildPageResult(RequestContext.getRequestEntity(), null, total, values);
	}

	/**
	 * 自定义json结果
	 *
	 * @param value json内容
	 */
	@Comment("自定义返回json内容")
	public ResponseEntity<Object> json(@Comment(name = "value", value = "返回对象") Object value) {
		return ResponseEntity.ok(value);
	}

	/**
	 * 添加Header
	 */
	@Comment("添加response header")
	public ResponseModule addHeader(@Comment(name = "key", value = "header名") String key,
									@Comment(name = "value", value = "header值") String value) {
		if (StringUtils.isNotBlank(key)) {
			HttpServletResponse response = getResponse();
			if (response != null) {
				response.addHeader(key, value);
			}
		}
		return this;
	}

	/**
	 * 设置header
	 */
	@Comment("设置response header")
	public ResponseModule setHeader(@Comment(name = "key", value = "header名") String key,
									@Comment(name = "value", value = "header值") String value) {
		if (StringUtils.isNotBlank(key)) {
			HttpServletResponse response = getResponse();
			if (response != null) {
				response.setHeader(key, value);
			}
		}
		return this;
	}

	/**
	 * 添加cookie
	 */
	@Comment("添加Cookie")
	public ResponseModule addCookie(@Comment(name = "name", value = "cookie名") String name,
									@Comment(name = "value", value = "cookie值") String value) {
		if (StringUtils.isNotBlank(name)) {
			addCookie(new Cookie(name, value));
		}
		return this;
	}

	/**
	 * 批量添加cookie
	 */
	@Comment("批量添加Cookie")
	public ResponseModule addCookies(@Comment(name = "cookies", value = "Cookies") Map<String, String> cookies,
									 @Comment(name = "options", value = "Cookie选项，如`path`、`httpOnly`、`domain`、`maxAge`") Map<String, Object> options) {
		if (cookies != null) {
			for (Map.Entry<String, String> entry : cookies.entrySet()) {
				addCookie(entry.getKey(), entry.getValue(), options);
			}
		}
		return this;
	}

	/**
	 * 批量添加cookie
	 */
	@Comment("批量添加Cookie")
	public ResponseModule addCookies(@Comment(name = "cookies", value = "Cookies") Map<String, String> cookies) {
		return addCookies(cookies, null);

	}

	/**
	 * 获取OutputStream
	 *
	 * @since 1.2.3
	 */
	@Comment("获取OutputStream")
	public OutputStream getOutputStream() throws IOException {
		HttpServletResponse response = getResponse();
		return response.getOutputStream();
	}

	/**
	 * 添加cookie
	 */
	@Comment("添加Cookie")
	public ResponseModule addCookie(@Comment(name = "name", value = "Cookie名") String name,
									@Comment(name = "value", value = "Cookie值") String value,
									@Comment(name = "options", value = "Cookie选项，如`path`、`httpOnly`、`domain`、`maxAge`") Map<String, Object> options) {
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
		return this;
	}

	@Comment("终止输出，执行此方法后不会对结果进行任何输出及处理")
	public NullValue end() {
		return NullValue.INSTANCE;
	}

	/**
	 * 添加cookie
	 */
	@Comment("添加Cookie")
	public ResponseModule addCookie(@Comment(name = "cookie", value = "Cookie对象") Cookie cookie) {
		if (cookie != null) {
			HttpServletResponse response = getResponse();
			if (response != null) {
				response.addCookie(cookie);
			}
		}
		return this;
	}

	private HttpServletResponse getResponse() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) requestAttributes).getResponse();
		}
		return RequestContext.getHttpServletResponse();
	}

	/**
	 * 展示图片
	 *
	 * @param value 图片内容
	 * @param mime  图片类型，image/png,image/jpeg,image/gif
	 */
	@Comment("输出图片")
	public ResponseEntity image(@Comment(name = "value", value = "图片内容，如`byte[]`") Object value,
								@Comment(name = "mime", value = "图片类型，如`image/png`、`image/jpeg`、`image/gif`") String mime) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mime).body(value);
	}

	/**
	 * 输出文本
	 *
	 * @param text 文本内容
	 */
	@Comment("输出文本")
	public ResponseEntity text(@Comment(name = "text", value = "文本内容") String text) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE).body(text);
	}

	/**
	 * 重定向
	 *
	 * @param url 目标网址
	 */
	@Comment("重定向")
	public NullValue redirect(@Comment(name = "url", value = "目标网址") String url) throws IOException {
		getResponse().sendRedirect(url);
		return NullValue.INSTANCE;
	}

	public static class NullValue {
		static final NullValue INSTANCE = new NullValue();
	}
}
