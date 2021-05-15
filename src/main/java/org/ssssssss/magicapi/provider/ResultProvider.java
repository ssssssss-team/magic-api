package org.ssssssss.magicapi.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.model.PageResult;
import org.ssssssss.magicapi.model.RequestEntity;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.ast.statement.Exit;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 结果构建接口
 */
public interface ResultProvider {

	Logger logger = LoggerFactory.getLogger(ResultProvider.class);

	/**
	 * 根据异常内容构建结果
	 *
	 * @param requestEntity 请求信息
	 * @param root          异常对象
	 */
	default Object buildResult(RequestEntity requestEntity, Throwable root) {
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return buildResult(requestEntity, sae.getCode(), sae.getMessage());
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		logger.error("调用接口出错", root);
		if (se != null) {
			return buildResult(requestEntity, Constants.RESPONSE_CODE_EXCEPTION, se.getSimpleMessage());
		}
		return buildResult(requestEntity, Constants.RESPONSE_CODE_EXCEPTION, root.getMessage());
	}

	/**
	 * 构建JSON返回结果，code和message 默认为 1 success
	 *
	 * @param requestEntity 请求相关信息
	 * @param data          返回内容
	 */
	default Object buildResult(RequestEntity requestEntity, Object data) {
		if (data instanceof Exit.Value) {
			Exit.Value exitValue = (Exit.Value) data;
			Object[] values = exitValue.getValues();
			int code = values.length > 0 ? ObjectConvertExtension.asInt(values[0], 1) : 1;
			String message = values.length > 1 ? Objects.toString(values[1], "success") : "success";
			return buildResult(requestEntity, code, message, values.length > 2 ? values[2] : null);
		}
		return buildResult(requestEntity, Constants.RESPONSE_CODE_SUCCESS, "success", data);
	}

	/**
	 * 构建JSON返回结果
	 *
	 * @param requestEntity 请求相关信息
	 * @param code          状态码
	 * @param message       状态说明
	 */
	default Object buildResult(RequestEntity requestEntity, int code, String message) {
		return buildResult(requestEntity, code, message, null);
	}

	/**
	 * 构建JSON返回结果
	 *
	 * @param requestEntity 请求相关信息
	 * @param code          状态码
	 * @param message       状态说明
	 * @param data          数据内容，可以通过data的类型判断是否是分页结果进行区分普通结果集和分页结果集
	 */
	Object buildResult(RequestEntity requestEntity, int code, String message, Object data);

	/**
	 * @param requestEntity 请求相关信息
	 * @param page          分页对象
	 * @param total         总数
	 * @param data          数据内容
	 */
	default Object buildPageResult(RequestEntity requestEntity, Page page, long total, List<Map<String, Object>> data) {
		return buildPageResult(total, data);
	}

	/**
	 * @param total 总数
	 * @param data  数据内容
	 */
	@Deprecated
	default Object buildPageResult(long total, List<Map<String, Object>> data) {
		return new PageResult<>(total, data);
	}
}
