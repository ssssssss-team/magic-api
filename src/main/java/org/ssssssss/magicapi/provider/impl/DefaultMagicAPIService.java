package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.exception.MagicServiceException;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.RequestEntity;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Scope;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.util.Map;

public class DefaultMagicAPIService implements MagicAPIService {

	private final MappingHandlerMapping mappingHandlerMapping;

	private final boolean throwException;

	private final ResultProvider resultProvider;

	public DefaultMagicAPIService(MappingHandlerMapping mappingHandlerMapping, ResultProvider resultProvider, boolean throwException) {
		this.mappingHandlerMapping = mappingHandlerMapping;
		this.resultProvider = resultProvider;
		this.throwException = throwException;
		MagicResourceLoader.addFunctionLoader((name) -> {
			int index = name.indexOf(":");
			if (index > -1) {
				String method = name.substring(0, index);
				String path = name.substring(index + 1);
				ApiInfo info = this.mappingHandlerMapping.getApiInfo(method, path);
				if (info != null) {
					return new Expression(new Span("unknown source")) {
						@Override
						public Object evaluate(MagicScriptContext context, Scope scope) {
							return execute(info, scope.getVariables());
						}
					};
				}
			}
			return null;
		});
	}

	private Object execute(ApiInfo info, Map<String, Object> context) {

		// 获取原上下文
		final MagicScriptContext magicScriptContext = MagicScriptContext.get();

		MagicScriptContext scriptContext = new MagicScriptContext();
		scriptContext.putMapIntoContext(context);
		SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
		simpleScriptContext.setAttribute(MagicScript.CONTEXT_ROOT, scriptContext, ScriptContext.ENGINE_SCOPE);
		final Object evalVal;
		try {
			evalVal = ((MagicScript) ScriptManager.compile("MagicScript", info.getScript())).eval(simpleScriptContext);
		} finally {
			// 恢复原接口上下文，修复当前调完其它接口后原接口上下文丢失的问题
			MagicScriptContext.set(magicScriptContext);
		}
		return evalVal;
	}

	@Override
	public Object execute(String method, String path, Map<String, Object> context) {
		ApiInfo info = this.mappingHandlerMapping.getApiInfo(method, path);
		if (info == null) {
			throw new MagicServiceException(String.format("找不到对应接口 [%s:%s]", method, path));
		}
		return execute(info, context);
	}

	@Override
	public Object call(String method, String path, Map<String, Object> context) {
		RequestEntity requestEntity = RequestEntity.empty();
		try {
			return resultProvider.buildResult(requestEntity, execute(method, path, context));
		} catch (MagicServiceException e) {
			return null;    //找不到对应接口
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			return resultProvider.buildResult(requestEntity, root);
		}
	}

	@Override
	public String getModuleName() {
		return "magic";
	}
}
