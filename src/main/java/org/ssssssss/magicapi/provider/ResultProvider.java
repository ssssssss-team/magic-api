package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.PageResult;

import java.util.List;

public interface ResultProvider {

	/**
	 * 构建JSON返回结果(默认状态码和状态说明)
	 * @param data	数据内容，状态码和状态说明默认为1 "success"
	 * @return
	 */
	default Object buildResult(Object data) {
		return buildResult(1, "success", data);
	}

	/**
	 * 构建JSON返回结果（无数据内容）
	 * @param code	状态码
	 * @param message	状态说明
	 * @return
	 */
	default Object buildResult(int code, String message) {
		return buildResult(code, message, null);
	}

	/**
	 * 构建JSON返回结果
	 * @param code	状态码
	 * @param message	状态说明
	 * @param data	数据内容，可以通过data的类型判断是否是分页结果进行区分普通结果集和分页结果集
	 * @return
	 */
	Object buildResult(int code, String message, Object data);

	/**
	 *
	 * @param total	总数
	 * @param data	数据内容
	 * @return
	 */
	default Object buildPageResult(long total, List<Object> data) {
		return new PageResult<>(total, data);
	}
}
