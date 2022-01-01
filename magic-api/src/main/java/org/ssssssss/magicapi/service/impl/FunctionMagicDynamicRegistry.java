package org.ssssssss.magicapi.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.event.FileEvent;
import org.ssssssss.magicapi.event.GroupEvent;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.Parameter;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.magicapi.service.AbstractMagicDynamicRegistry;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.MagicExitException;
import org.ssssssss.script.runtime.ExitValue;

import java.util.List;
import java.util.function.Function;

public class FunctionMagicDynamicRegistry extends AbstractMagicDynamicRegistry<FunctionInfo> {

	private static final Logger logger = LoggerFactory.getLogger(FunctionMagicDynamicRegistry.class);

	public FunctionMagicDynamicRegistry(MagicResourceStorage<FunctionInfo> magicResourceStorage) {
		super(magicResourceStorage);
		MagicResourceLoader.addFunctionLoader(this::lookupLambdaFunction);
	}

	private Object lookupLambdaFunction(MagicScriptContext context, String path) {
		FunctionInfo functionInfo = getMapping(path);
		if (functionInfo != null) {
			String scriptName = magicResourceStorage.buildScriptName(functionInfo);
			List<Parameter> parameters = functionInfo.getParameters();
			return (Function<Object[], Object>) objects -> {
				MagicScriptContext functionContext = new MagicScriptContext(context.getRootVariables());
				functionContext.setScriptName(scriptName);
				if (objects != null) {
					for (int i = 0, len = objects.length, size = parameters.size(); i < len && i < size; i++) {
						functionContext.set(parameters.get(i).getName(), objects[i]);
					}
				}
				Object value = ScriptManager.executeScript(functionInfo.getScript(), functionContext);
				if (value instanceof ExitValue) {
					throw new MagicExitException((ExitValue) value);
				}
				return value;
			};
		}
		return null;
	}

	@EventListener(condition = "#event.type == 'function'")
	public void onFileEvent(FileEvent event) {
		processEvent(event);
	}

	@EventListener(condition = "#event.type == 'function'")
	public void onGroupEvent(GroupEvent event) {
		processEvent(event);
	}

	@Override
	protected boolean register(MappingNode<FunctionInfo> mappingNode) {
		logger.debug("注册函数：{}", mappingNode.getMappingKey());
		return true;
	}

	@Override
	protected void unregister(MappingNode<FunctionInfo> mappingNode) {
		logger.debug("取消注册函数：{}", mappingNode.getMappingKey());
	}
}
