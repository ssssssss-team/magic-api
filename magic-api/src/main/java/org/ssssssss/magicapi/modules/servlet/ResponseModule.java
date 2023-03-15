package org.ssssssss.magicapi.modules.servlet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.context.RequestContext;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletResponse;
import org.ssssssss.script.annotation.Comment;

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
			MagicHttpServletResponse response = getResponse();
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
			MagicHttpServletResponse response = getResponse();
			if (response != null) {
				response.setHeader(key, value);
			}
		}
		return this;
	}

	/**
	 * 获取OutputStream
	 *
	 * @since 1.2.3
	 */
	@Comment("获取OutputStream")
	public OutputStream getOutputStream() throws IOException {
		MagicHttpServletResponse response = getResponse();
		return response.getOutputStream();
	}


	@Comment("终止输出，执行此方法后不会对结果进行任何输出及处理")
	public NullValue end() {
		return NullValue.INSTANCE;
	}

	private MagicHttpServletResponse getResponse() {
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
