package org.ssssssss.magicapi.functions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.ssssssss.magicapi.provider.ResultProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
}
