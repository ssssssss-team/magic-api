package org.ssssssss.magicapi.modules;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.script.annotation.Comment;
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
public class ResponseModule {

	private final ResultProvider resultProvider;

	public ResponseModule(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	/**
	 * 自行构建分页结果
	 *
	 * @param total  条数
	 * @param values 数据内容
	 */
	@Comment("返回自定义分页结果")
	public Object page(@Comment("总条数") long total, @Comment("当前结果集") List<Map<String, Object>> values) {
		return resultProvider.buildPageResult(total, values);
	}

	/**
	 * 自定义json结果
	 *
	 * @param value json内容
	 */
	@Comment("自定义返回json内容")
	public ResponseEntity<Object> json(@Comment("返回对象") Object value) {
		return ResponseEntity.ok(value);
	}

	/**
	 * 添加Header
	 */
	@Comment("添加response header")
	public ResponseModule addHeader(@Comment("header名") String key, @Comment("header值") String value) {
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
	public ResponseModule setHeader(@Comment("header名") String key, @Comment("header值") String value) {
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
	public ResponseModule addCookie(@Comment("cookie名") String name, @Comment("cookie值") String value) {
		if (StringUtils.isNotBlank(name)) {
			addCookie(new Cookie(name, value));
		}
		return this;
	}

	/**
	 * 批量添加cookie
	 */
	@Comment("批量添加Cookie")
	public ResponseModule addCookies(@Comment("Cookies") Map<String, String> cookies, @Comment("Cookie选项，如`path`、`httpOnly`、`domain`、`maxAge`") Map<String, Object> options) {
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
	public ResponseModule addCookies(@Comment("Cookies") Map<String, String> cookies) {
		return addCookies(cookies, null);

	}

	/**
	 * 添加cookie
	 */
	@Comment("添加Cookie")
	public ResponseModule addCookie(@Comment("Cookie名") String name, @Comment("Cookie值") String value,
									@Comment("Cookie选项，如`path`、`httpOnly`、`domain`、`maxAge`") Map<String, Object> options) {
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
	public ResponseModule addCookie(@Comment("Cookie对象") Cookie cookie) {
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
		return null;
	}

	/**
	 * 展示图片
	 *
	 * @param value 图片内容
	 * @param mime  图片类型，image/png,image/jpeg,image/gif
	 */
	@Comment("输出图片")
	public ResponseEntity image(@Comment("图片内容，如`byte[]`") Object value, @Comment("图片类型，如`image/png`、`image/jpeg`、`image/gif`") String mime) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mime).body(value);
	}

	/**
	 * 文件下载
	 *
	 * @param value    文件内容
	 * @param filename 文件名
	 */
	@Comment("文件下载")
	public static ResponseEntity<?> download(@Comment("文件内容，如`byte[]`") Object value, @Comment("文件名") String filename) throws UnsupportedEncodingException {
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"))
				.body(value);
	}

	public static class NullValue {
		static final NullValue INSTANCE = new NullValue();
	}
}
