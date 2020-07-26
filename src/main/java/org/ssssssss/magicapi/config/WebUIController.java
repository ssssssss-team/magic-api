package org.ssssssss.magicapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.functions.DatabaseQuery;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 页面UI对应的Controller
 */
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
	 *
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
			if (success) {    //删除成功时在取消注册
				mappingHandlerMapping.unregisterMapping(id, true);
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
			if (success) {    //删除成功时取消注册
				if (StringUtils.isNotBlank(apiIds)) {
					String[] ids = apiIds.split(",");
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							mappingHandlerMapping.unregisterMapping(id, true);
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
	 * 修改分组
	 *
	 * @param groupName    分组名称
	 * @param oldGroupName 原分组名称
	 * @param prefix       分组前缀
	 */
	@RequestMapping("/group/update")
	@ResponseBody
	public JsonBean<Boolean> groupUpdate(String groupName, String oldGroupName, String prefix, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = magicApiService.updateGroup(oldGroupName, groupName, prefix);
			if (success) {
				mappingHandlerMapping.updateGroupPrefix(oldGroupName, groupName, prefix);
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("修改分组出错", e);
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
	public Object debugContinue(String id, HttpServletResponse response) throws IOException {
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
		return convertResult(context.getReturnValue(), response);
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
	 * 创建控制台输出
	 */
	@RequestMapping("/console")
	public SseEmitter console() throws IOException {
		String sessionId = UUID.randomUUID().toString().replace("-", "");
		SseEmitter emitter = MagicLoggerContext.createEmitter(sessionId);
		emitter.send(SseEmitter.event().data(sessionId).name("create"));
		return emitter;
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
	 * @param request 请求参数
	 */
	@RequestMapping("/test")
	@ResponseBody
	public Object test(HttpServletRequest servletRequest, @RequestBody(required = false) Map<String, Object> request, HttpServletResponse response) {
		if (!allowVisit(servletRequest, RequestInterceptor.Authorization.RUN)) {
			return new JsonBean<>(-10, "无权限执行测试方法");
		}
		Object script = request.get("script");
		if (script != null) {
			request.remove("script");
			Object breakpoints = request.get("breakpoints");
			request.remove("breakpoints");
			Object sessionId = request.remove("sessionId");
			MagicScriptDebugContext context = new MagicScriptDebugContext();
			try {
				context.putMapIntoContext((Map<String, Object>) request.get("request"));
				context.putMapIntoContext((Map<String, Object>) request.get("path"));
				context.set("cookie", request.get("cookie"));
				context.set("session", request.get("session"));
				context.set("header", request.get("header"));
				context.set("body", request.get("body"));
			} catch (Exception e) {
				return new JsonBean<>(0, "请求参数填写错误", resultProvider.buildResult(0, "请求参数填写错误"));
			}
			try {
				context.setBreakpoints((List<Integer>) breakpoints);    //设置断点
				context.setTimeout(this.debugTimeout);    //设置断点超时时间
				if (sessionId != null) {
					context.setId(sessionId.toString());
					context.onComplete(() -> {
						logger.info("Close Console Session : {}", sessionId);
						MagicLoggerContext.remove(sessionId.toString());
					});
					context.onStart(() -> {
						MDC.put(MagicLoggerContext.MAGIC_CONSOLE_SESSION, sessionId.toString());
						logger.info("Create Console Session : {}", sessionId);
					});
				}
				Object result = MagicScriptEngine.execute(MagicScriptCompiler.compile(script.toString()), context);
				if (context.isRunning()) {    //判断是否执行完毕
					return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(1000, context.getId(), result), result);
				} else if (context.isException()) {    //判断是否出现异常
					return resolveThrowable((Throwable) context.getReturnValue());
				}
				return convertResult(result, response);
			} catch (Exception e) {
				return resolveThrowable(e);
			}
		}
		return new JsonBean<>(resultProvider.buildResult(0, "脚本不能为空"));
	}

	private Object convertResult(Object result, HttpServletResponse response) throws IOException {
		if (result instanceof ResponseEntity) {
			ResponseEntity entity = (ResponseEntity) result;
			for (Map.Entry<String, List<String>> entry : entity.getHeaders().entrySet()) {
				String key = entry.getKey();
				for (String value : entry.getValue()) {
					response.addHeader("MA-" + key, value);
				}
			}
			if (entity.getHeaders().isEmpty()) {
				return ResponseEntity.ok(new JsonBean<>(entity.getBody()));
			}
			return ResponseEntity.ok(new JsonBean<>(convertToBase64(entity.getBody())));
		}
		return new JsonBean<>(resultProvider.buildResult(result));
	}

	private String convertToBase64(Object value) throws IOException {
		if (value instanceof String || value instanceof Number) {
			return convertToBase64(value.toString().getBytes());
		} else if (value instanceof byte[]) {
			return Base64.getEncoder().encodeToString((byte[]) value);
		} else if (value instanceof InputStream) {
			return convertToBase64(IOUtils.toByteArray((InputStream) value));
		} else if (value instanceof InputStreamSource) {
			InputStreamSource iss = (InputStreamSource) value;
			return convertToBase64(iss.getInputStream());
		} else {
			return convertToBase64(new ObjectMapper().writeValueAsString(value));
		}
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
				return new JsonBean<>(resultProvider.buildResult(sae.getCode(), sae.getMessage()));
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

	@RequestMapping("/backups")
	@ResponseBody
	public JsonBean<List<Long>> backups(String id) {
		return new JsonBean<>(magicApiService.backupList(id));
	}

	@RequestMapping("/backup/get")
	@ResponseBody
	public JsonBean<ApiInfo> backups(String id, Long timestamp) {
		return new JsonBean<>(magicApiService.backupInfo(id, timestamp));
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
				if (magicApiService.exists(info.getGroupPrefix(), info.getMethod(), info.getPath())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.insert(info);
			} else {
				// 先判断接口是否存在
				if (magicApiService.existsWithoutId(info.getGroupPrefix(), info.getMethod(), info.getPath(), info.getId())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.update(info);
			}
			magicApiService.backup(info.getId());
			// 注册接口
			mappingHandlerMapping.registerMapping(info, true);
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
