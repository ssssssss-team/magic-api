package org.ssssssss.magicapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.ssssssss.magicapi.context.CookieContext;
import org.ssssssss.magicapi.context.HeaderContext;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.context.SessionContext;
import org.ssssssss.magicapi.functions.ResponseFunctions;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.JsonBodyBean;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Span;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class RequestHandler {

	private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	/**
	 * 请求拦截器
	 */
	private List<RequestInterceptor> requestInterceptors = new ArrayList<>();

	/**
	 * 请求出错时，是否抛出异常
	 */
	private boolean throwException = false;

	/**
	 * 结果处理器
	 */
	private ResultProvider resultProvider;

	private final String HEADER_REQUEST_SESSION = "Magic-Request-Session";
	private final String HEADER_REQUEST_BREAKPOINTS = "Magic-Request-Breakpoints";
	private final String HEADER_REQUEST_CONTINUE = "Magic-Request-Continue";
	private final String HEADER_REQUEST_STEP_INTO = "Magic-Request-Step-Into";
	private final String HEADER_RESPONSE_WITH_MAGIC_API = "Response-With-Magic-API";
	private WebUIController webUIController;
	/**
	 * debug 超时时间
	 */
	private int debugTimeout;

	public void setDebugTimeout(int debugTimeout) {
		this.debugTimeout = debugTimeout;
	}

	public void setWebUIController(WebUIController webUIController) {
		this.webUIController = webUIController;
	}

	public void setResultProvider(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	public void addRequestInterceptor(RequestInterceptor requestInterceptor) {
		requestInterceptors.add(requestInterceptor);
	}

	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

	private List<HttpMessageConverter<?>> httpMessageConverters;

	public void setHttpMessageConverters(List<HttpMessageConverter<?>> httpMessageConverters) {
		this.httpMessageConverters = httpMessageConverters;
	}

	/**
	 * 打印banner
	 */
	public void printBanner() {
		System.out.println("  __  __                _           _     ____  ___ ");
		System.out.println(" |  \\/  |  __ _   __ _ (_)  ___    / \\   |  _ \\|_ _|");
		System.out.println(" | |\\/| | / _` | / _` || | / __|  / _ \\  | |_) || | ");
		System.out.println(" | |  | || (_| || (_| || || (__  / ___ \\ |  __/ | | ");
		System.out.println(" |_|  |_| \\__,_| \\__, ||_| \\___|/_/   \\_\\|_|   |___|");
		System.out.println("                  |___/                        " + RequestHandler.class.getPackage().getImplementationVersion());
	}

	@ResponseBody
	public Object invoke(HttpServletRequest request, HttpServletResponse response,
						 @PathVariable(required = false) Map<String, Object> pathVariables,
						 @RequestParam(required = false) Map<String, Object> parameters) throws Throwable {
		boolean requestedFromTest = isRequestedFromTest(request);
		ApiInfo info = MappingHandlerMapping.getMappingApiInfo(request);
		if (requestedFromTest) {
			if (!webUIController.allowVisit(request, RequestInterceptor.Authorization.RUN)) {
				return new JsonBean<>(-10, "无权限执行测试方法");
			}
		}
		if (info == null) {
			logger.error("接口不存在");
			return resultProvider.buildResult(1001, "fail", "接口不存在");
		}
		MagicScriptContext context = createMagicScriptContext(info, request, pathVariables, parameters);

		Object value;
		// 执行前置拦截器
		if ((value = doPreHandle(info, context)) != null) {
			return value;
		}
		if (requestedFromTest) {
			if (isRequestedFromContinue(request)) {
				return invokeContinueRequest(request, response);
			}
			return invokeTestRequest(info, (MagicScriptDebugContext) context, request, response);
		}
		return invokeRequest(info, context, request, response);
	}

	private Object invokeContinueRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String sessionId = getRequestedSessionId(request);
		MagicScriptDebugContext context = MagicScriptDebugContext.getDebugContext(sessionId);
		if (context == null) {
			return new JsonBean<>(0, "debug session not found!", resultProvider.buildResult(0, "debug session not found!"));
		}
		// 重置断点
		context.setBreakpoints(getRequestedBreakpoints(request));
		// 步进
		context.setStepInto("1".equalsIgnoreCase(request.getHeader(HEADER_REQUEST_STEP_INTO)));
		try {
			context.singal();    //等待语句执行到断点或执行完毕
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (context.isRunning()) {    //判断是否执行完毕
			return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(1000, context.getId()), context.getDebugInfo());
		} else if (context.isException()) {
			return resolveThrowable((Throwable) context.getReturnValue(), response);
		}
		return convertResult(context.getReturnValue(), response);
	}

	private Object invokeTestRequest(ApiInfo info, MagicScriptDebugContext context, HttpServletRequest request, HttpServletResponse response) {
		try {
			// 初始化debug操作
			initializeDebug(context, request, response);
			Object result = executeScript(info.getScript(), context);
			if (context.isRunning()) {
				return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(1000, context.getId(), result), result);
			} else if (context.isException()) {    //判断是否出现异常
				return resolveThrowable((Throwable) context.getReturnValue(), response);
			}
			if (context.isRunning()) {    //判断是否执行完毕
				return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(1000, context.getId(), result), result);
			} else if (context.isException()) {    //判断是否出现异常
				return resolveThrowable((Throwable) context.getReturnValue(), response);
			}
			Object value = result;
			// 执行后置拦截器
			if ((value = doPostHandle(info, context, value)) != null) {
				return convertResult(value, response);
			}
			return convertResult(result, response);
		} catch (Exception e) {
			return resolveThrowable(e, response);
		}
	}

	private Object invokeRequest(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		try {
			RequestContext.setRequestAttribute(request, response);
			Object result = executeScript(info.getScript(), context);
			Object value = result;
			// 执行后置拦截器
			if ((value = doPostHandle(info, context, value)) != null) {
				return value;
			}
			// 对返回结果包装处理
			return response(result);
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			logger.error("接口请求出错", root);
			return response(root);
		} finally {
			RequestContext.remove();
		}
	}

	/**
	 * 执行脚本
	 */
	private Object executeScript(String script, MagicScriptContext context) {
		SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
		simpleScriptContext.setAttribute(MagicScript.CONTEXT_ROOT, context, ScriptContext.ENGINE_SCOPE);
		// 执行脚本
		return ScriptManager.compile("MagicScript", script).eval(simpleScriptContext);
	}

	/**
	 * 转换请求结果
	 */
	private Object convertResult(Object result, HttpServletResponse response) throws IOException {
		response.addHeader(HEADER_RESPONSE_WITH_MAGIC_API, "true");
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
		} else if (result instanceof ResponseFunctions.NullValue) {
			return new JsonBean<>(1, "empty.");
		}
		return new JsonBean<>(resultProvider.buildResult(result));
	}

	/**
	 * 将结果转为base64
	 */
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
	private JsonBean<Object> resolveThrowable(Throwable root, HttpServletResponse response) {
		response.addHeader(HEADER_RESPONSE_WITH_MAGIC_API, "true");
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

	private void initializeDebug(MagicScriptDebugContext context, HttpServletRequest request, HttpServletResponse response) {

		RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes(), true);

		String sessionId = getRequestedSessionId(request);
		// 设置断点
		context.setBreakpoints(getRequestedBreakpoints(request));
		context.setTimeout(this.debugTimeout);
		context.setId(sessionId);
		context.onComplete(() -> {
			logger.info("Close Console Session : {}", sessionId);
			RequestContext.remove();
			MagicLoggerContext.remove(sessionId);
		});
		context.onStart(() -> {
			RequestContext.setRequestAttribute(request, response);
			MagicLoggerContext.SESSION.set(sessionId);
			logger.info("Create Console Session : {}", sessionId);
		});
	}

	private boolean isRequestedFromTest(HttpServletRequest request) {
		return webUIController != null && request.getHeader(HEADER_REQUEST_SESSION) != null;
	}

	private boolean isRequestedFromContinue(HttpServletRequest request) {
		return request.getHeader(HEADER_REQUEST_CONTINUE) != null;
	}

	private String getRequestedSessionId(HttpServletRequest request) {
		return request.getHeader(HEADER_REQUEST_SESSION);
	}

	private List<Integer> getRequestedBreakpoints(HttpServletRequest request) {
		String breakpoints = request.getHeader(HEADER_REQUEST_BREAKPOINTS);
		if (breakpoints != null) {
			return Arrays.stream(breakpoints.split(","))
					.map(val -> ObjectConvertExtension.asInt(val, -1))
					.collect(Collectors.toList());
		}
		return null;
	}

	private Object readRequestBody(HttpServletRequest request) throws IOException {
		if (httpMessageConverters != null && request.getContentType() != null) {
			MediaType mediaType = MediaType.valueOf(request.getContentType());
			Class clazz = Map.class;
			for (HttpMessageConverter<?> converter : httpMessageConverters) {
				if (converter.canRead(clazz, mediaType)) {
					return converter.read(clazz, new ServletServerHttpRequest(request));
				}
			}
		}
		return null;
	}

	/**
	 * 构建 MagicScriptContext
	 */
	private MagicScriptContext createMagicScriptContext(ApiInfo info, HttpServletRequest request, Map<String, Object> pathVariables, Map<String, Object> parameters) throws IOException {
		// 构建脚本上下文
		MagicScriptContext context = isRequestedFromTest(request) ? new MagicScriptDebugContext() : new MagicScriptContext();
		Object wrap = info.getOptionValue(ApiInfo.WRAP_REQUEST_PARAMETER);
		if (wrap != null && StringUtils.isNotBlank(wrap.toString())) {
			context.set(wrap.toString(), parameters);
		}
		context.putMapIntoContext(parameters);
		context.putMapIntoContext(pathVariables);
		context.set("cookie", new CookieContext(request));
		context.set("header", new HeaderContext(request));
		context.set("session", new SessionContext(request.getSession()));
		context.set("path", pathVariables);
		Object requestBody = readRequestBody(request);
		if (requestBody != null) {
			context.set("body", requestBody);
		}
		return context;
	}

	/**
	 * 包装返回结果
	 */
	private Object response(Object value) {
		if (value instanceof ResponseEntity) {
			return value;
		} else if (value instanceof ResponseFunctions.NullValue) {
			return null;
		}
		return resultProvider.buildResult(value);
	}

	/**
	 * 执行后置拦截器
	 */
	private Object doPostHandle(ApiInfo info, MagicScriptContext context, Object value) throws Exception {
		for (RequestInterceptor requestInterceptor : requestInterceptors) {
			Object target = requestInterceptor.postHandle(info, context, value);
			if (target != null) {
				return target;
			}
		}
		return null;
	}

	/**
	 * 执行前置拦截器
	 */
	private Object doPreHandle(ApiInfo info, MagicScriptContext context) throws Exception {
		for (RequestInterceptor requestInterceptor : requestInterceptors) {
			Object value = requestInterceptor.preHandle(info, context);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
