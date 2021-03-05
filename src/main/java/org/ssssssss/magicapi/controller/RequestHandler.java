package org.ssssssss.magicapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.context.CookieContext;
import org.ssssssss.magicapi.context.HeaderContext;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.context.SessionContext;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.logging.LogInfo;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.modules.ResponseModule;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.magicapi.utils.PatternUtils;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;
import org.ssssssss.script.reflection.JavaInvoker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class RequestHandler extends MagicController {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	private final ResultProvider resultProvider;

	public RequestHandler(MagicConfiguration configuration) {
		super(configuration);
		this.resultProvider = configuration.getResultProvider();
	}

	@ResponseBody
	public Object invoke(HttpServletRequest request, HttpServletResponse response,
						 @PathVariable(required = false) Map<String, Object> pathVariables,
						 @RequestParam(required = false) Map<String, Object> parameters) throws Throwable {
		long requestTime = System.currentTimeMillis();
		boolean requestedFromTest = isRequestedFromTest(request);
		ApiInfo info = MappingHandlerMapping.getMappingApiInfo(request);
		if (requestedFromTest) {
			response.setHeader(HEADER_RESPONSE_WITH_MAGIC_API, "true");
			response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HEADER_RESPONSE_WITH_MAGIC_API);
			if (!allowVisit(request, RequestInterceptor.Authorization.RUN)) {
				return new JsonBean<>(-10, "无权限执行测试方法");
			}
		}
		if (info == null) {
			logger.error("接口不存在");
			return resultProvider.buildResult(null, request, response, 1001, "fail", "接口不存在", requestTime);
		}
		// 验证
		Object value = doValidate(info, request, response, requestTime, parameters);
		if (value != null) {
			if (requestedFromTest) {
				return new JsonBean<>(0, "参数验证失败", value);
			}
			return value;
		}
		MagicScriptContext context = createMagicScriptContext(info, request, pathVariables, parameters);
		// 执行前置拦截器
		if ((value = doPreHandle(info, context, request, response)) != null) {
			if (requestedFromTest) {
				// 修正前端显示，当拦截器返回时，原样输出显示
				response.setHeader(HEADER_RESPONSE_WITH_MAGIC_API, "false");
			}
			return value;
		}
		if (requestedFromTest) {
			if (isRequestedFromContinue(request)) {
				return invokeContinueRequest(info, requestTime, request, response);
			}
			return invokeTestRequest(info, requestTime, (MagicScriptDebugContext) context, request, response);
		}
		return invokeRequest(info, requestTime, context, request, response);
	}

	private Object doValidate(ApiInfo info, HttpServletRequest request, HttpServletResponse response, Long requestTime, Map<String, Object> parameters) {
		List<Parameter> parameterList = info.getParameters();
		for (Parameter parameter : parameterList) {
			String requestValue = StringUtils.defaultIfBlank(Objects.toString(parameters.get(parameter.getName())), Objects.toString(parameter.getDefaultValue(), ""));
			if (parameter.isRequired()) {
				if (StringUtils.isBlank(requestValue)) {
					return resultProvider.buildResult(info, request, response, 0, StringUtils.defaultIfBlank(parameter.getError(), String.format("参数[%s]为必填项", parameter.getName())), null, requestTime);
				}
			}
			try {
				Object value = convertValue(parameter.getDataType(), parameter.getName(), requestValue);
				String validateType = parameter.getValidateType();
				if ("pattern".equals(validateType)) {    // 正则验证
					String expression = parameter.getExpression();
					if (StringUtils.isNotBlank(expression) && !PatternUtils.match(Objects.toString(value, ""), expression)) {
						return resultProvider.buildResult(info, request, response, 0, StringUtils.defaultIfBlank(parameter.getError(), String.format("参数[%s]不满足正则表达式", parameter.getName())), null, requestTime);
					}
				}
				parameters.put(parameter.getName(), value);

			} catch (Exception e) {
				return resultProvider.buildResult(info, request, response, 0, StringUtils.defaultIfBlank(parameter.getError(), String.format("参数[%s]不合法", parameter.getName())), null, requestTime);
			}
		}
		// 取出表达式验证的参数
		List<Parameter> validates = parameterList.stream().filter(it -> "expression".equals(it.getValidateType()) && StringUtils.isNotBlank(it.getExpression())).collect(Collectors.toList());
		for (Parameter parameter : validates) {
			MagicScriptContext context = new MagicScriptContext();
			context.putMapIntoContext(parameters);
			context.set("value", parameters.get(parameter.getName()));
			if (!BooleanLiteral.isTrue(ScriptManager.executeExpression(parameter.getExpression(), context))) {
				return resultProvider.buildResult(info, request, response, 0, StringUtils.defaultIfBlank(parameter.getError(), String.format("参数[%s]不满足表达式", parameter.getName())), null, requestTime);
			}
		}
		return null;
	}

	private Object convertValue(DataType dataType, String name, String value) {
		if (dataType == null) {
			return value;
		}
		try {
			if (dataType.isNumber()) {
				BigDecimal decimal = ObjectConvertExtension.asDecimal(value, null);
				if (decimal == null) {
					throw new IllegalArgumentException();
				}
				return dataType.getInvoker().invoke0(decimal, null);
			} else {
				JavaInvoker<Method> invoker = dataType.getInvoker();
				if (invoker != null) {
					List<Object> params = new ArrayList<>();
					if (dataType.isNeedName()) {
						params.add(name);
					}
					if (dataType.isNeedValue()) {
						params.add(value);
					}
					return invoker.invoke0(null, null, params.toArray());
				}
			}
			return value;
		} catch (Throwable throwable) {
			throw new IllegalArgumentException();
		}
	}

	private Object invokeContinueRequest(ApiInfo info, long requestTime, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String sessionId = getRequestedSessionId(request);
		MagicScriptDebugContext context = MagicScriptDebugContext.getDebugContext(sessionId);
		if (context == null) {
			return new JsonBean<>(0, "debug session not found!", resultProvider.buildResult(info, request, response, 0, "debug session not found!", requestTime));
		}
		// 重置断点
		context.setBreakpoints(getRequestedBreakpoints(request));
		// 步进
		context.setStepInto("true".equalsIgnoreCase(request.getHeader(HEADER_REQUEST_STEP_INTO)));
		try {
			context.singal();    //等待语句执行到断点或执行完毕
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (context.isRunning()) {    //判断是否执行完毕
			return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(info, request, response, 1000, context.getId(), requestTime), context.getDebugInfo());
		} else if (context.isException()) {
			return resolveThrowable(info, request, response, (Throwable) context.getReturnValue(), requestTime);
		}
		Object value = context.getReturnValue();
		// 执行后置拦截器
		if ((value = doPostHandle(info, context, value, request, response)) != null) {
			// 修正前端显示，当拦截器返回时，原样输出显示
			response.setHeader(HEADER_RESPONSE_WITH_MAGIC_API, "false");
			// 后置拦截器不包裹
			return value;
		}
		return convertResult(info, request, context.getReturnValue(), requestTime, response);
	}

	private Object invokeTestRequest(ApiInfo info, long requestTime, MagicScriptDebugContext context, HttpServletRequest request, HttpServletResponse response) {
		try {
			// 初始化debug操作
			initializeDebug(context, request, response);
			Object result = ScriptManager.executeScript(info.getScript(), context);
			if (context.isRunning()) {
				return new JsonBodyBean<>(1000, context.getId(), resultProvider.buildResult(info, request, response, 1000, context.getId(), result, requestTime), result);
			} else if (context.isException()) {    //判断是否出现异常
				return resolveThrowable(info, request, response, (Throwable) context.getReturnValue(), requestTime);
			}
			Object value = result;
			// 执行后置拦截器
			if ((value = doPostHandle(info, context, value, request, response)) != null) {
				// 修正前端显示，当拦截器返回时，原样输出显示
				response.setHeader(HEADER_RESPONSE_WITH_MAGIC_API, "false");
				// 后置拦截器不包裹
				return value;
			}
			return convertResult(info, request, result, requestTime, response);
		} catch (Exception e) {
			return resolveThrowable(info, request, response, e, requestTime);
		}
	}

	private Object invokeRequest(ApiInfo info, long requestTime, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		try {
			RequestContext.setRequestAttribute(request, response);
			Object result = ScriptManager.executeScript(info.getScript(), context);
			Object value = result;
			// 执行后置拦截器
			if ((value = doPostHandle(info, context, value, request, response)) != null) {
				return value;
			}
			// 对返回结果包装处理
			return response(info, request, response, result, requestTime);
		} catch (Throwable root) {
			Throwable parent = root;
			do {
				if (parent instanceof MagicScriptAssertException) {
					MagicScriptAssertException sae = (MagicScriptAssertException) parent;
					return resultProvider.buildResult(info, request, response, sae.getCode(), sae.getMessage(), requestTime);
				}
			} while ((parent = parent.getCause()) != null);
			if (configuration.isThrowException()) {
				throw root;
			}
			logger.error("接口{}请求出错", request.getRequestURI(), root);
			return resultProvider.buildResult(info, request, response, -1, "系统内部出现错误", requestTime);
		} finally {
			RequestContext.remove();
		}
	}

	/**
	 * 转换请求结果
	 */
	private Object convertResult(ApiInfo info, HttpServletRequest request, Object result, long requestTime, HttpServletResponse response) throws IOException {
		if (result instanceof ResponseEntity) {
			ResponseEntity<?> entity = (ResponseEntity<?>) result;
			List<String> headers = new ArrayList<>();
			for (Map.Entry<String, List<String>> entry : entity.getHeaders().entrySet()) {
				String key = entry.getKey();
				for (String value : entry.getValue()) {
					headers.add("MA-" + key);
					response.addHeader("MA-" + key, value);
				}
			}
			headers.add(HEADER_RESPONSE_WITH_MAGIC_API);
			response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(",", headers));
			if (entity.getHeaders().isEmpty()) {
				return ResponseEntity.ok(new JsonBean<>(entity.getBody()));
			}
			return ResponseEntity.ok(new JsonBean<>(convertToBase64(entity.getBody())));
		} else if (result instanceof ResponseModule.NullValue) {
			return new JsonBean<>(1, "empty.");
		}
		return new JsonBean<>(resultProvider.buildResult(info, request, response, result, requestTime));
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
	private JsonBean<Object> resolveThrowable(ApiInfo info, HttpServletRequest request, HttpServletResponse response, Throwable root, long requestTime) {
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return new JsonBean<>(resultProvider.buildResult(info, request, response, sae.getCode(), sae.getMessage(), requestTime));
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		logger.error("测试脚本出错", root);
		if (se != null) {
			Span.Line line = se.getLine();
			return new JsonBodyBean<>(-1000, se.getSimpleMessage(), resultProvider.buildResult(info, request, response, -1000, se.getSimpleMessage(), requestTime), line == null ? null : Arrays.asList(line.getLineNumber(), line.getEndLineNumber(), line.getStartCol(), line.getEndCol()));
		}
		return new JsonBean<>(-1, root.getMessage(), resultProvider.buildResult(info, request, response, -1, root.getMessage(), requestTime));
	}

	private void initializeDebug(MagicScriptDebugContext context, HttpServletRequest request, HttpServletResponse response) {

		RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes(), true);

		String sessionId = getRequestedSessionId(request);
		// 设置断点
		context.setBreakpoints(getRequestedBreakpoints(request));
		context.setTimeout(configuration.getDebugTimeout());
		context.setId(sessionId);
		context.onComplete(() -> {
			if (context.isException()) {
				MagicLoggerContext.println(new LogInfo(Level.ERROR.name().toLowerCase(), "执行脚本出错", (Throwable) context.getReturnValue()));
			}
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
		return configuration.isEnableWeb() && request.getHeader(HEADER_REQUEST_SESSION) != null;
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
		if (configuration.getHttpMessageConverters() != null && request.getContentType() != null) {
			MediaType mediaType = MediaType.valueOf(request.getContentType());
			Class clazz = Object.class;
			try {
				for (HttpMessageConverter<?> converter : configuration.getHttpMessageConverters()) {
					if (converter.canRead(clazz, mediaType)) {
						return converter.read(clazz, new ServletServerHttpRequest(request));
					}
				}
			} catch (HttpMessageNotReadableException ignored) {
				return null;
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
		Object wrap = info.getOptionValue(Options.WRAP_REQUEST_PARAMETERS.getValue());
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
	private Object response(ApiInfo info, HttpServletRequest request, HttpServletResponse response, Object value, long requestTime) {
		if (value instanceof ResponseEntity) {
			return value;
		} else if (value instanceof ResponseModule.NullValue) {
			return null;
		}
		return resultProvider.buildResult(info, request, response, value, requestTime);
	}

	/**
	 * 执行后置拦截器
	 */
	private Object doPostHandle(ApiInfo info, MagicScriptContext context, Object value, HttpServletRequest request, HttpServletResponse response) throws Exception {
		for (RequestInterceptor requestInterceptor : configuration.getRequestInterceptors()) {
			Object target = requestInterceptor.postHandle(info, context, value, request, response);
			if (target != null) {
				return target;
			}
		}
		return null;
	}

	/**
	 * 执行前置拦截器
	 */
	private Object doPreHandle(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
		for (RequestInterceptor requestInterceptor : configuration.getRequestInterceptors()) {
			Object value = requestInterceptor.preHandle(info, context, request, response);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

}
