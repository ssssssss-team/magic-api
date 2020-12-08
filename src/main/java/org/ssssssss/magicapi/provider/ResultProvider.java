package org.ssssssss.magicapi.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.model.PageResult;
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
	 */
	default Object buildResult(Throwable root) {
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return buildResult(sae.getCode(), sae.getMessage());
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		logger.error("调用接口出错", root);
		if (se != null) {
			return buildResult(-1, se.getSimpleMessage());
		}
		return buildResult(-1, root.getMessage());
	}

	/**
	 * 构建JSON返回结果(默认状态码和状态说明)
	 *
	 * @param data 数据内容，状态码和状态说明默认为1 "success"
	 */
	default Object buildResult(Object data) {
		if (data instanceof Exit.Value) {
			Exit.Value exitValue = (Exit.Value) data;
			Object[] values = exitValue.getValues();
			int code = values.length > 0 ? ObjectConvertExtension.asInt(values[0], 1) : 1;
			String message = values.length > 1 ? Objects.toString(values[1], "success") : "success";
			return buildResult(code, message, values.length > 2 ? values[2] : null);
		}
		return buildResult(1, "success", data);
	}

	/**
	 * 构建JSON返回结果（无数据内容）
	 *
	 * @param code    状态码
	 * @param message 状态说明
	 */
	default Object buildResult(int code, String message) {
		return buildResult(code, message, null);
	}

	/**
	 * 构建JSON返回结果
	 *
	 * @param code    状态码
	 * @param message 状态说明
	 * @param data    数据内容，可以通过data的类型判断是否是分页结果进行区分普通结果集和分页结果集
	 */
	Object buildResult(int code, String message, Object data);

	/**
	 * @param total 总数
	 * @param data  数据内容
	 */
	default Object buildPageResult(long total, List<Map<String, Object>> data) {
		return new PageResult<>(total, data);
	}
}
