package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.MappingHandlerMapping;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.context.CookieContext;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.context.SessionContext;
import org.ssssssss.magicapi.exception.ValidateException;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.modules.ResponseModule;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.magicapi.utils.Invoker;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.PatternUtils;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.exception.MagicScriptAssertException;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.functions.ObjectConvertExtension;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.ssssssss.magicapi.config.MessageType.BREAKPOINT;
import static org.ssssssss.magicapi.config.MessageType.EXCEPTION;
import static org.ssssssss.magicapi.model.Constants.*;

public class RequestHandler extends MagicController {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	private final ResultProvider resultProvider;

	private static final Map<String, Object> EMPTY_MAP = new HashMap<>();

	public RequestHandler(MagicConfiguration configuration) {
		super(configuration);
		this.resultProvider = configuration.getResultProvider();
	}

	/**
	 * 测试入口、实际请求入口
	 */
	@ResponseBody
	@Valid(requireLogin = false)    // 无需验证是否要登录
	public Object invoke(HttpServletRequest request, HttpServletResponse response,
						 @PathVariable(required = false) Map<String, Object> pathVariables,
						 @RequestParam(required = false) Map<String, Object> parameters) throws Throwable {
		String sessionId = null;
		boolean requestedFromTest = configuration.isEnableWeb() && (sessionId = request.getHeader(HEADER_REQUEST_SESSION)) != null;
		RequestEntity requestEntity = new RequestEntity(request, response, requestedFromTest, parameters, pathVariables);
		if (requestEntity.getApiInfo() == null) {
			logger.error("{}找不到对应接口", request.getRequestURI());
			return buildResult(requestEntity, API_NOT_FOUND, "接口不存在");
		}
		Map<String, Object> headers = new HashMap<String, Object>() {
			@Override
			public Object get(Object key) {
				return getOrDefault(key, request.getHeader(key.toString()));
			}
		};
		requestEntity.setHeaders(headers);
		List<Path> paths = new ArrayList<>(requestEntity.getApiInfo().getPaths());
		MappingHandlerMapping.findGroups(requestEntity.getApiInfo().getGroupId())
				.stream()
				.flatMap(it -> it.getPaths().stream())
				.filter(it -> !paths.contains(it))
				.forEach(paths::add);
		Object bodyValue = readRequestBody(requestEntity.getRequest());
		try {
			// 验证参数
			doValidate("参数", requestEntity.getApiInfo().getParameters(), parameters, PARAMETER_INVALID);
			// 验证 header
			doValidate("header", requestEntity.getApiInfo().getHeaders(), headers, HEADER_INVALID);
			// 验证 path
			doValidate("path", paths, requestEntity.getPathVariables(), PATH_VARIABLE_INVALID);
			BaseDefinition requestBody = requestEntity.getApiInfo().getRequestBodyDefinition();
			if (requestBody != null && !CollectionUtils.isEmpty(requestBody.getChildren())) {
				requestBody.setName(StringUtils.defaultIfBlank(requestBody.getName(), "root"));
				doValidate(VAR_NAME_REQUEST_BODY, Collections.singletonList(requestBody), new HashMap<String, Object>() {{
					put(requestBody.getName(), bodyValue);
				}}, BODY_INVALID);
			}
		} catch (ValidateException e) {
			return resultProvider.buildResult(requestEntity, RESPONSE_CODE_INVALID, e.getMessage());
		} catch (Throwable root) {
			return processException(requestEntity, root);
		}
		MagicScriptContext context = createMagicScriptContext(requestEntity, bodyValue);
		requestEntity.setMagicScriptContext(context);
		RequestContext.setRequestEntity(requestEntity);
		Object value;
		// 执行前置拦截器
		if ((value = doPreHandle(requestEntity)) != null) {
			return value;
		}
		if (requestedFromTest) {
			try {
				MagicLoggerContext.SESSION.set(sessionId);
				return invokeRequest(requestEntity);
			} finally {
				MagicLoggerContext.SESSION.remove();
				WebSocketSessionManager.remove(sessionId);
			}
		} else {
			return invokeRequest(requestEntity);
		}
	}

	private Object buildResult(RequestEntity requestEntity, JsonCode code, Object data) {
		return resultProvider.buildResult(requestEntity, code.getCode(), code.getMessage(), data);
	}


	private boolean doValidateBody(String comment, BaseDefinition parameter, Map<String, Object> parameters, JsonCode jsonCode, Class<?> target) {
		if (!parameter.isRequired() && parameters.isEmpty()) {
			return true;
		}
		if (parameter.isRequired() && !BooleanLiteral.isTrue(parameters.get(parameter.getName()))) {
			throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]为必填项", comment, parameter.getName())));
		}
		Object value = parameters.get(parameter.getName());
		if (value != null && !target.isAssignableFrom(value.getClass())) {
			throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]数据类型错误", comment, parameter.getName())));
		}
		return false;
	}

	private <T extends BaseDefinition> Map<String, Object> doValidate(String comment, List<T> validateParameters, Map<String, Object> parameters, JsonCode jsonCode) {
		parameters = parameters != null ? parameters : EMPTY_MAP;
		if(CollectionUtils.isEmpty(validateParameters)){
			return parameters;
		}
		for (BaseDefinition parameter : validateParameters) {
			// 针对requestBody多层级的情况
			if (DataType.Object == parameter.getDataType()) {
				if (doValidateBody(comment, parameter, parameters, jsonCode, Map.class)) {
					continue;
				}
				doValidate(VAR_NAME_REQUEST_BODY, parameter.getChildren(), (Map) parameters.get(parameter.getName()), jsonCode);
			} else if (DataType.Array == parameter.getDataType()) {
				if (doValidateBody(comment, parameter, parameters, jsonCode, List.class)) {
					continue;
				}
				List<Object> list = (List) parameters.get(parameter.getName());
				if (list != null) {
					List<Map<String, Object>> newList = list.stream().map(it -> doValidate(VAR_NAME_REQUEST_BODY, parameter.getChildren(), new HashMap<String, Object>() {{    // 使用 hashmap
						put(EMPTY, it);
					}}, jsonCode)).collect(Collectors.toList());
					for (int i = 0, size = newList.size(); i < size; i++) {
						list.set(i, newList.get(i).get(EMPTY));
					}
				}

			} else if (StringUtils.isNotBlank(parameter.getName()) || parameters.containsKey(parameter.getName())) {
				boolean isFile = parameter.getDataType() == DataType.MultipartFile || parameter.getDataType() == DataType.MultipartFiles;
				String requestValue = StringUtils.defaultIfBlank(Objects.toString(parameters.get(parameter.getName()), EMPTY), Objects.toString(parameter.getDefaultValue(), EMPTY));
				if (StringUtils.isBlank(requestValue) && !isFile) {
					if (!parameter.isRequired()) {
						continue;
					}
					throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]为必填项", comment, parameter.getName())));
				}
				try {
					Object value = convertValue(parameter.getDataType(), parameter.getName(), requestValue);
					if (isFile && parameter.isRequired()) {
						if (value == null || (parameter.getDataType() == DataType.MultipartFiles && ((List<?>) value).isEmpty())) {
							throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]为必填项", comment, parameter.getName())));
						}
					}
					if (VALIDATE_TYPE_PATTERN.equals(parameter.getValidateType())) {    // 正则验证
						String expression = parameter.getExpression();
						if (StringUtils.isNotBlank(expression) && !PatternUtils.match(Objects.toString(value, EMPTY), expression)) {
							throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]不满足正则表达式", comment, parameter.getName())));
						}
					}
					parameters.put(parameter.getName(), value);
				} catch (ValidateException ve) {
					throw ve;
				} catch (Exception e) {
					throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]不合法", comment, parameter.getName())));
				}
			}
		}
		// 取出表达式验证的参数
		List<BaseDefinition> validates = validateParameters.stream().filter(it -> VALIDATE_TYPE_EXPRESSION.equals(it.getValidateType()) && StringUtils.isNotBlank(it.getExpression())).collect(Collectors.toList());
		for (BaseDefinition parameter : validates) {
			MagicScriptContext context = new MagicScriptContext();
			// 将其他参数也放置脚本中，以实现“依赖”的情况
			context.putMapIntoContext(parameters);
			Object value = parameters.get(parameter.getName());
			if (value != null) {
				// 设置自身变量
				context.set(EXPRESSION_DEFAULT_VAR_NAME, value);
				if (!BooleanLiteral.isTrue(ScriptManager.executeExpression(parameter.getExpression(), context))) {
					throw new ValidateException(jsonCode, StringUtils.defaultIfBlank(parameter.getError(), String.format("%s[%s]不满足表达式", comment, parameter.getName())));
				}
			}
		}
		return parameters;
	}

	/**
	 * 转换参数类型
	 */
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
				return dataType.getInvoker().invoke(decimal, null, EMPTY_OBJECT_ARRAY);
			} else {
				Invoker invoker = dataType.getInvoker();
				if (invoker != null) {
					List<Object> params = new ArrayList<>();
					if (dataType.isNeedName()) {
						params.add(name);
					}
					if (dataType.isNeedValue()) {
						params.add(value);
					}
					return invoker.invoke(null, null, params.toArray());
				}
			}
			return value;
		} catch (Throwable throwable) {
			throw new IllegalArgumentException();
		}
	}

	private Object invokeRequest(RequestEntity requestEntity) throws Throwable {
		try {
			MagicScriptContext context = requestEntity.getMagicScriptContext();
			MagicScriptContext.set(context);
			Object result = ScriptManager.executeScript(requestEntity.getApiInfo().getScript(), context);
			Object value = result;
			// 执行后置拦截器
			if ((value = doPostHandle(requestEntity, value)) != null) {
				return value;
			}
			// 对返回结果包装处理
			return response(requestEntity, result);
		} catch (Throwable root) {
			return processException(requestEntity, root);
		} finally {
			RequestContext.remove();
			MagicScriptContext.remove();
		}
	}

	private Object processException(RequestEntity requestEntity, Throwable root) throws Throwable {
		MagicScriptException se = null;
		Throwable parent = root;
		do {
			if (parent instanceof MagicScriptAssertException) {
				MagicScriptAssertException sae = (MagicScriptAssertException) parent;
				return resultProvider.buildResult(requestEntity, sae.getCode(), sae.getMessage());
			}
			if (parent instanceof MagicScriptException) {
				se = (MagicScriptException) parent;
			}
		} while ((parent = parent.getCause()) != null);
		if(se != null && requestEntity.isRequestedFromTest()){
			Span.Line line = se.getLine();
			WebSocketSessionManager.sendBySessionId(requestEntity.getRequestedSessionId(), EXCEPTION, Arrays.asList(
					requestEntity.getRequestedSessionId(),
					se.getSimpleMessage(),
					line == null ? null : Arrays.asList(line.getLineNumber(), line.getEndLineNumber(), line.getStartCol(), line.getEndCol())
			));
		}
		if (configuration.isThrowException()) {
			throw root;
		}
		logger.error("接口{}请求出错", requestEntity.getRequest().getRequestURI(), root);
		return resultProvider.buildException(requestEntity, root);
	}

	/**
	 * 读取RequestBody
	 */
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
	private MagicScriptContext createMagicScriptContext(RequestEntity requestEntity, Object requestBody) {
		List<Integer> breakpoints = requestEntity.getRequestedBreakpoints();
		// 构建脚本上下文
		MagicScriptContext context;
		// TODO 安全校验
		if (requestEntity.isRequestedFromDebug() && breakpoints.size() > 0) {
			MagicScriptDebugContext debugContext = new MagicScriptDebugContext(breakpoints);
			String sessionId = requestEntity.getRequestedSessionId();
			debugContext.setTimeout(configuration.getDebugTimeout());
			debugContext.setId(sessionId);
			debugContext.setCallback(variables -> {
				List<Map<String, Object>> varList = (List<Map<String, Object>>) variables.get("variables");
				varList.stream().filter(it -> it.containsKey("value")).forEach(variable -> {
					variable.put("value", JsonUtils.toJsonStringWithoutLog(variable.get("value")));
				});
				WebSocketSessionManager.sendBySessionId(sessionId, BREAKPOINT, variables);
			});
			WebSocketSessionManager.createSession(sessionId, debugContext);
			context = debugContext;
		} else {
			context = new MagicScriptContext();
		}
		Object wrap = requestEntity.getApiInfo().getOptionValue(Options.WRAP_REQUEST_PARAMETERS.getValue());
		if (wrap != null && StringUtils.isNotBlank(wrap.toString())) {
			context.set(wrap.toString(), requestEntity.getParameters());
		}
		context.putMapIntoContext(requestEntity.getParameters());
		context.putMapIntoContext(requestEntity.getPathVariables());
		context.set(VAR_NAME_COOKIE, new CookieContext(requestEntity.getRequest()));
		context.set(VAR_NAME_HEADER, requestEntity.getHeaders());
		context.set(VAR_NAME_SESSION, new SessionContext(requestEntity.getRequest().getSession()));
		context.set(VAR_NAME_PATH_VARIABLE, requestEntity.getPathVariables());
		if (requestBody != null) {
			context.set(VAR_NAME_REQUEST_BODY, requestBody);
		}
		return context;
	}

	/**
	 * 包装返回结果
	 */
	private Object response(RequestEntity requestEntity, Object value) {
		if (value instanceof ResponseEntity) {
			if(requestEntity.isRequestedFromTest()){
				ResponseEntity<?> entity = (ResponseEntity<?>) value;
				Set<String> headerKeys = entity.getHeaders().keySet();
				if(!headerKeys.isEmpty()){
					// 允许前端读取自定义的header（跨域情况）。
					requestEntity.getResponse().setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(",", headerKeys));
				}
			}
			return value;
		} else if (value instanceof ResponseModule.NullValue) {
			return null;
		}
		return resultProvider.buildResult(requestEntity, value);
	}

	/**
	 * 执行后置拦截器
	 */
	private Object doPostHandle(RequestEntity requestEntity, Object value) throws Exception {
		for (RequestInterceptor requestInterceptor : configuration.getRequestInterceptors()) {
			Object target = requestInterceptor.postHandle(requestEntity, value);
			if (target != null) {
				return target;
			}
		}
		return null;
	}

	/**
	 * 执行前置拦截器
	 */
	private Object doPreHandle(RequestEntity requestEntity) throws Exception {
		for (RequestInterceptor requestInterceptor : configuration.getRequestInterceptors()) {
			Object value = requestInterceptor.preHandle(requestEntity);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

}
