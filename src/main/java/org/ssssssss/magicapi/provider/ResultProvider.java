package org.ssssssss.magicapi.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.PageResult;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.ast.statement.Exit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	 */
	default Object buildResult(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, Throwable root, long requestTime) {
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return buildResult(apiInfo, request, response, sae.getCode(), sae.getMessage(), requestTime);
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		logger.error("调用接口出错", root);
		if (se != null) {
			return buildResult(apiInfo, request, response, -1, se.getSimpleMessage(), requestTime);
		}
		return buildResult(apiInfo, request, response, -1, root.getMessage(), requestTime);
	}

	/**
	 * 构建JSON返回结果(默认状态码和状态说明)
	 *
	 * @param apiInfo  接口信息，可能为NULL
	 * @param request  可能为NULL
	 * @param response 可能为NULL
	 * @param data     数据内容，状态码和状态说明默认为1 "success"
	 */
	default Object buildResult(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, Object data, long requestTime) {
		if (data instanceof Exit.Value) {
			Exit.Value exitValue = (Exit.Value) data;
			Object[] values = exitValue.getValues();
			int code = values.length > 0 ? ObjectConvertExtension.asInt(values[0], 1) : 1;
			String message = values.length > 1 ? Objects.toString(values[1], "success") : "success";
			return buildResult(apiInfo, request, response, code, message, values.length > 2 ? values[2] : null, requestTime);
		}
		return buildResult(apiInfo, request, response, 1, "success", data, requestTime);
	}

	/**
	 * 构建JSON返回结果（无数据内容）
	 *
	 * @param code    状态码
	 * @param message 状态说明
	 */
	default Object buildResult(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, int code, String message, long requestTime) {
		return buildResult(apiInfo, request, response, code, message, null, requestTime);
	}

	/**
	 * 构建JSON返回结果
	 *
	 * @param code    状态码
	 * @param message 状态说明
	 * @param data    数据内容，可以通过data的类型判断是否是分页结果进行区分普通结果集和分页结果集
	 */
	Object buildResult(ApiInfo apiInfo, HttpServletRequest request, HttpServletResponse response, int code, String message, Object data, long requestTime);

	/**
	 * @param total 总数
	 * @param data  数据内容
	 */
	default Object buildPageResult(long total, List<Map<String, Object>> data) {
		return new PageResult<>(total, data);
	}
}
