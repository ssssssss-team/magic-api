package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WebUIController {

	private static Logger logger = LoggerFactory.getLogger(WebUIController.class);

	private int debugTimeout;

	private MappingHandlerMapping mappingHandlerMapping;

	private MagicApiService magicApiService;

	public void setDebugTimeout(int debugTimeout) {
		this.debugTimeout = debugTimeout;
	}

	public void setMappingHandlerMapping(MappingHandlerMapping mappingHandlerMapping) {
		this.mappingHandlerMapping = mappingHandlerMapping;
	}

	public void setMagicApiService(MagicApiService magicApiService) {
		this.magicApiService = magicApiService;
	}

	public void printBanner() {
		System.out.println("  __  __                _           _     ____  ___ ");
		System.out.println(" |  \\/  |  __ _   __ _ (_)  ___    / \\   |  _ \\|_ _|");
		System.out.println(" | |\\/| | / _` | / _` || | / __|  / _ \\  | |_) || | ");
		System.out.println(" | |  | || (_| || (_| || || (__  / ___ \\ |  __/ | | ");
		System.out.println(" |_|  |_| \\__,_| \\__, ||_| \\___|/_/   \\_\\|_|   |___|");
		System.out.println("                  |___/                        " + WebUIController.class.getPackage().getImplementationVersion());
	}

	@RequestMapping("/delete")
	@ResponseBody
	public JsonBean<Boolean> delete(String id) {
		try {
			return new JsonBean<>(this.magicApiService.delete(id));
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/list")
	@ResponseBody
	public JsonBean<List<ApiInfo>> list() {
		try {
			return new JsonBean<>(magicApiService.list());
		} catch (Exception e) {
			logger.error("查询接口列表失败", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/continue")
	@ResponseBody
	public JsonBean<Object> debugContinue(String id) {
		MagicScriptDebugContext context = MagicScriptDebugContext.getDebugContext(id);
		if (context == null) {
			return new JsonBean<>(0, "debug session not found!");
		}
		try {
			context.singal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (context.isRunning()) {
			return new JsonBean<>(1000, context.getId(), context.getDebugInfo());
		} else if (context.isException()) {
			return resolveThrowable((Throwable) context.getReturnValue());
		}
		return new JsonBean<>(context.getReturnValue());
	}

	@RequestMapping("/test")
	@ResponseBody
	public JsonBean<Object> test(@RequestBody(required = false) Map<String, Object> request) {
		Object script = request.get("script");
		if (script != null) {
			request.remove("script");
			Object breakpoints = request.get("breakpoints");
			request.remove("breakpoints");
			MagicScriptDebugContext context = new MagicScriptDebugContext(request);
			try {
				context.setBreakpoints((List<Integer>) breakpoints);
				context.setTimeout(this.debugTimeout);
				Object result = MagicScriptEngine.execute(script.toString(), context);
				if (context.isRunning()) {
					return new JsonBean<>(1000, context.getId(), result);
				} else if (context.isException()) {
					return resolveThrowable((Throwable) context.getReturnValue());
				}
				return new JsonBean<>(result);
			} catch (Exception e) {
				return resolveThrowable(e);
			}
		}
		return new JsonBean<>(0, "脚本不能为空");
	}

	private JsonBean<Object> resolveThrowable(Throwable root) {
		MagicScriptError.ScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptError.ScriptException) {
				se = (MagicScriptError.ScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		logger.error("测试脚本出错", root);
		if (se != null) {
			Span.Line line = se.getLine();
			return new JsonBean<>(-1000, se.getSimpleMessage(), line == null ? null : Arrays.asList(line.getLineNumber(), line.getEndLineNumber(), line.getStartCol(), line.getEndCol()));
		}
		return new JsonBean<>(-1, root.getMessage());
	}

	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<ApiInfo> get(String id) {
		try {
			return new JsonBean<>(this.magicApiService.get(id));
		} catch (Exception e) {
			logger.error("查询接口出错");
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/save")
	@ResponseBody
	public JsonBean<String> save(ApiInfo info) {
		try {
			if (StringUtils.isBlank(info.getMethod())) {
				return new JsonBean<>(0, "请求方法不能为空");
			}
			if (StringUtils.isBlank(info.getPath())) {
				return new JsonBean<>(0, "请求路径不能为空");
			}
			if (StringUtils.isBlank(info.getName())) {
				return new JsonBean<>(0, "接口名称不能为空");
			}
			if (StringUtils.isBlank(info.getScript())) {
				return new JsonBean<>(0, "脚本内容不能为空");
			}
			if (StringUtils.isBlank(info.getId())) {
				if (magicApiService.exists(info.getMethod(), info.getPath())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.insert(info);
			} else {
				if (magicApiService.existsWithoutId(info.getMethod(), info.getPath(), info.getId())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.update(info);
			}
			mappingHandlerMapping.registerMapping(info);
			return new JsonBean<>(info.getId());
		} catch (Exception e) {
			logger.error("保存接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}
}
