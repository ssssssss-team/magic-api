package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.functions.DatabaseQuery;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.JsonBodyBean;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.ScriptClass;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.parsing.Span;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WebUIController {

	private static Logger logger = LoggerFactory.getLogger(WebUIController.class);

	/**
	 * debug 超时时间
	 */
	private int debugTimeout;

	/**
	 * 接口映射
	 */
	private MappingHandlerMapping mappingHandlerMapping;

	/**
	 * 接口查询service
	 */
	private ApiServiceProvider magicApiService;

	/**
	 * 自定义结果
	 */
	private ResultProvider resultProvider;

	/**
	 * 拦截器
	 */
	private List<RequestInterceptor> requestInterceptors = new ArrayList<>();

	public WebUIController() {
		// 给前端添加代码提示
		MagicScriptEngine.addScriptClass(DatabaseQuery.class);
		MagicScriptEngine.addScriptClass(MagicAPIService.class);
	}

	public void addRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.requestInterceptors.add(requestInterceptor);
	}

	public void setResultProvider(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	public void setDebugTimeout(int debugTimeout) {
		this.debugTimeout = debugTimeout;
	}

	public void setMappingHandlerMapping(MappingHandlerMapping mappingHandlerMapping) {
		this.mappingHandlerMapping = mappingHandlerMapping;
	}

	public void setMagicApiService(ApiServiceProvider magicApiService) {
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

	/**
	 * 删除接口
	 * @param request
	 * @param id      接口ID
	 * @return
	 */
	@RequestMapping("/delete")
	@ResponseBody
	public JsonBean<Boolean> delete(HttpServletRequest request, String id) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = this.magicApiService.delete(id);
			if (success) {
				mappingHandlerMapping.unregisterMapping(id);
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 删除接口分组
	 *
	 * @param request
	 * @param apiIds    接口ID列表，逗号分隔
	 * @param groupName 分组名称
	 * @return
	 */
	@RequestMapping("/group/delete")
	@ResponseBody
	public JsonBean<Boolean> deleteGroup(HttpServletRequest request, String apiIds, String groupName) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = this.magicApiService.deleteGroup(groupName);
			if (success) {
				if (StringUtils.isNotBlank(apiIds)) {
					String[] ids = apiIds.split(",");
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							mappingHandlerMapping.unregisterMapping(id);
						}
					}
				}
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询所有接口
	 *
	 * @return
	 */
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

	/**
	 * debug 恢复断点
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping("/continue")
	@ResponseBody
	public JsonBean<Object> debugContinue(String id) {
		MagicScriptDebugContext context = MagicScriptDebugContext.getDebugContext(id);
		if (context == null) {
			return new JsonBean<>(0, "debug session not found!", resultProvider.buildResult(0, "debug session not found!"));
		}
		try {
			context.singal();    //等待语句执行到断点或执行完毕
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (context.isRunning()) {    //判断是否执行完毕
			return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(1000, context.getId()), context.getDebugInfo());
		} else if (context.isException()) {
			return resolveThrowable((Throwable) context.getReturnValue());
		}
		return new JsonBodyBean<>(resultProvider.buildResult(context.getReturnValue()), context.getReturnValue());
	}

	/**
	 * 获取所有class
	 */
	@RequestMapping("/classes")
	@ResponseBody
	public JsonBean<Map<String, ScriptClass>> classes() {
		Map<String, ScriptClass> classMap = MagicScriptEngine.getScriptClassMap();
		classMap.putAll(MagicModuleLoader.getModules());
		return new JsonBean<>(classMap);
	}

	/**
	 * 获取单个class
	 *
	 * @param className 类名
	 * @return
	 */
	@RequestMapping("/class")
	@ResponseBody
	public JsonBean<List<ScriptClass>> clazz(String className) {
		return new JsonBean<>(MagicScriptEngine.getScriptClass(className));
	}

	/**
	 * 测试运行
	 *
	 * @param servletRequest
	 * @param request        请求参数
	 * @return
	 */
	@RequestMapping("/test")
	@ResponseBody
	public JsonBean<Object> test(HttpServletRequest servletRequest, @RequestBody(required = false) Map<String, Object> request) {
		if (!allowVisit(servletRequest, RequestInterceptor.Authorization.RUN)) {
			return new JsonBean<>(-10, "无权限执行测试方法");
		}
		Object script = request.get("script");
		if (script != null) {
			request.remove("script");
			Object breakpoints = request.get("breakpoints");
			request.remove("breakpoints");
			MagicScriptDebugContext context = new MagicScriptDebugContext();
			try {
				context.putMapIntoContext((Map<String, Object>) request.get("request"));
				context.putMapIntoContext((Map<String, Object>) request.get("path"));
				context.set("cookie", request.get("cookie"));
				context.set("session", request.get("session"));
				context.set("header", request.get("header"));
			} catch (Exception e) {
				return new JsonBean<>(0, "请求参数填写错误", resultProvider.buildResult(0, "请求参数填写错误"));
			}
			try {
				context.setBreakpoints((List<Integer>) breakpoints);    //设置断点
				context.setTimeout(this.debugTimeout);    //设置断点超时时间
				Object result = MagicScriptEngine.execute(MagicScriptCompiler.compile(script.toString()), context);
				if (context.isRunning()) {    //判断是否执行完毕
					return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(1000, context.getId(), result), result);
				} else if (context.isException()) {    //判断是否出现异常
					return resolveThrowable((Throwable) context.getReturnValue());
				}
				return new JsonBean<>(resultProvider.buildResult(result));
			} catch (Exception e) {
				return resolveThrowable(e);
			}
		}
		return new JsonBean<>(resultProvider.buildResult(0, "脚本不能为空"));
	}

	/**
	 * 解决异常
	 */
	private JsonBean<Object> resolveThrowable(Throwable root) {
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return new JsonBean<>(sae.getCode(), sae.getMessage(), resultProvider.buildResult(sae.getCode(), sae.getMessage()));
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		logger.error("测试脚本出错", root);
		if (se != null) {
			Span.Line line = se.getLine();
			return new JsonBodyBean<>(-1000, se.getSimpleMessage(), resultProvider.buildResult(-1000, se.getSimpleMessage()), line == null ? null : Arrays.asList(line.getLineNumber(), line.getEndLineNumber(), line.getStartCol(), line.getEndCol()));
		}
		return new JsonBean<>(-1, root.getMessage(), resultProvider.buildResult(-1, root.getMessage()));
	}

	/**
	 * 查询接口详情
	 *
	 * @param request
	 * @param id      接口ID
	 * @return
	 */
	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<ApiInfo> get(HttpServletRequest request, String id) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DETAIL)) {
			return new JsonBean<>(-10, "无权限执行查看详情方法");
		}
		try {
			return new JsonBean<>(this.magicApiService.get(id));
		} catch (Exception e) {
			logger.error("查询接口出错");
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 保存接口
	 *
	 * @param request
	 * @param info    接口信息
	 * @return
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonBean<String> save(HttpServletRequest request, ApiInfo info) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行保存方法");
		}
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
				// 先判断接口是否存在
				if (magicApiService.exists(info.getMethod(), info.getPath())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.insert(info);
			} else {
				// 先判断接口是否存在
				if (magicApiService.existsWithoutId(info.getMethod(), info.getPath(), info.getId())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.update(info);
			}
			// 注册接口
			mappingHandlerMapping.registerMapping(info);
			return new JsonBean<>(info.getId());
		} catch (Exception e) {
			logger.error("保存接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 判断是否有权限访问按钮
	 *
	 * @param request
	 * @param authorization
	 * @return
	 */
	private boolean allowVisit(HttpServletRequest request, RequestInterceptor.Authorization authorization) {
		for (RequestInterceptor requestInterceptor : requestInterceptors) {
			if (!requestInterceptor.allowVisit(request, authorization)) {
				return false;
			}
		}
		return true;
	}
}