package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.functions.DatabaseQuery;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.script.*;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.parsing.Span;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebUIController {

	private static Logger logger = LoggerFactory.getLogger(WebUIController.class);

	private int debugTimeout;

	private MappingHandlerMapping mappingHandlerMapping;

	private MagicApiService magicApiService;

	public WebUIController() {
		MagicScriptEngine.addScriptClass(DatabaseQuery.class);
	}

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
			mappingHandlerMapping.unregisterMapping(id);
			return new JsonBean<>(this.magicApiService.delete(id));
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/group/delete")
	@ResponseBody
	public JsonBean<Boolean> deleteGroup(String apiIds, String groupName) {
		try {
			if (StringUtils.isNotBlank(apiIds)) {
				String[] ids = apiIds.split(",");
				if (ids != null && ids.length > 0) {
					for (String id : ids) {
						mappingHandlerMapping.unregisterMapping(id);
					}
				}
			}
			return new JsonBean<>(this.magicApiService.deleteGroup(groupName));
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

	@RequestMapping("/classes")
	@ResponseBody
	public JsonBean<Map<String, ScriptClass>> classes() {
		Map<String, ScriptClass> classMap = MagicScriptEngine.getScriptClassMap();
		classMap.putAll(MagicModuleLoader.getModules());
		return new JsonBean<>(classMap);
	}

	@RequestMapping("/class")
	@ResponseBody
	public JsonBean<List<ScriptClass>> clazz(String className) {
		return new JsonBean<>(MagicScriptEngine.getScriptClass(className));
	}

	@RequestMapping("/test")
	@ResponseBody
	public JsonBean<Object> test(@RequestBody(required = false) Map<String, Object> request) {
		Object script = request.get("script");
		if (script != null) {
			request.remove("script");
			Object breakpoints = request.get("breakpoints");
			request.remove("breakpoints");
			MagicScriptDebugContext context = new MagicScriptDebugContext();
			try {
				putMapIntoContext((Map<String, Object>) request.get("request"), context);
				putMapIntoContext((Map<String, Object>) request.get("path"), context);
				context.set("cookie", request.get("cookie"));
				context.set("session", request.get("session"));
				context.set("header", request.get("header"));
			} catch (Exception e) {
				return new JsonBean<>(0, "请求参数填写错误");
			}
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
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return new JsonBean<>(sae.getCode(), sae.getMessage());
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
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

	private void putMapIntoContext(Map<String, Object> map, MagicScriptContext context) {
		if (map != null && !map.isEmpty()) {
			Set<Map.Entry<String, Object>> entries = map.entrySet();
			for (Map.Entry<String, Object> entry : entries) {
				context.set(entry.getKey(), entry.getValue());
			}
		}
	}
}
