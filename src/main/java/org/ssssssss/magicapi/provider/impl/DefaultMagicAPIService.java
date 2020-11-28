package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.exception.MagicServiceException;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.util.Map;

public class DefaultMagicAPIService implements MagicAPIService {

	private MappingHandlerMapping mappingHandlerMapping;

	private boolean throwException;

	private ResultProvider resultProvider;

	public DefaultMagicAPIService(MappingHandlerMapping mappingHandlerMapping, ResultProvider resultProvider, boolean throwException) {
		this.mappingHandlerMapping = mappingHandlerMapping;
		this.resultProvider = resultProvider;
		this.throwException = throwException;
	}

	@Override
	public Object execute(String method, String path, Map<String, Object> context) {
		ApiInfo info = this.mappingHandlerMapping.getApiInfo(method, path);
		if (info == null) {
			throw new MagicServiceException(String.format("找不到对应接口 [%s:%s]", method, path));
		}
		MagicScriptContext scriptContext = new MagicScriptContext();
		scriptContext.putMapIntoContext(context);
		SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
		simpleScriptContext.setAttribute(MagicScript.CONTEXT_ROOT, scriptContext, ScriptContext.ENGINE_SCOPE);
		return ScriptManager.compile("MagicScript", info.getScript()).eval(simpleScriptContext);
	}

	@Override
	public Object call(String method, String path, Map<String, Object> context) {
		try {
			return resultProvider.buildResult(execute(method, path, context));
		} catch (MagicServiceException e) {
			return null;    //找不到对应接口
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			return resultProvider.buildResult(root);
		}
	}

	@Override
	public String getModuleName() {
		return "magic";
	}
}
