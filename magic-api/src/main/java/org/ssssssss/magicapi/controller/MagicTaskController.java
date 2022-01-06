package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.model.DebugRequest;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.MagicEntity;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.MagicScriptDebugContext;

import javax.servlet.http.HttpServletRequest;

public class MagicTaskController extends MagicController implements MagicExceptionHandler{

	public MagicTaskController(MagicConfiguration configuration) {
		super(configuration);
	}

	@PostMapping("/task/execute")
	@ResponseBody
	public JsonBean<Object> execute(String id, HttpServletRequest request){
		MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
		notNull(entity, FILE_NOT_FOUND);
		String script = entity.getScript();
		DebugRequest debugRequest = DebugRequest.create(request);
		MagicScriptDebugContext magicScriptContext = debugRequest.createMagicScriptContext(configuration.getDebugTimeout());
		return new JsonBean<>(ScriptManager.executeScript(script, magicScriptContext));
	}
}
