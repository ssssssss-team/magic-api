package org.ssssssss.magicapi.core.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.config.WebSocketSessionManager;
import org.ssssssss.magicapi.core.context.CookieContext;
import org.ssssssss.magicapi.core.context.RequestContext;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.core.context.SessionContext;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.core.exception.ValidateException;
import org.ssssssss.magicapi.core.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.core.logging.MagicLoggerContext;
import org.ssssssss.magicapi.modules.servlet.ResponseModule;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;
import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.magicapi.core.service.impl.RequestMagicDynamicRegistry;
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
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.*;
import static org.ssssssss.magicapi.core.config.MessageType.EXCEPTION;
import static org.ssssssss.magicapi.core.config.Constants.*;

/**
 * 请求入口处理
 *
 * @author mxd
 */
public class RequestHandler extends MagicController {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private static final Map<String, Object> EMPTY_MAP = new HashMap<>();
	private static final List<String> DEFAULT_ALLOW_READ_RESPONSE_HEADERS = Arrays.asList(
			ACCESS_CONTROL_ALLOW_CREDENTIALS, ACCESS_CONTROL_ALLOW_HEADERS, ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS,
			CONTENT_TYPE, DATE, SERVER, SET_COOKIE, CONNECTION, CONTENT_LENGTH, CONTENT_ENCODING, TRANSFER_ENCODING, VARY);
	private final ResultProvider resultProvider;

	private final RequestMagicDynamicRegistry requestMagicDynamicRegistry;

	public RequestHandler(MagicConfiguration configuration, RequestMagicDynamicRegistry requestMagicDynamicRegistry) {
		super(configuration);
		this.requestMagicDynamicRegistry = requestMagicDynamicRegistry;
		this.resultProvider = configuration.getResultProvider();
	}

	/**
	 * 测试入口、实际请求入口
	 *
	 * @param request       HttpServletRequest
	 * @param response      HttpServletResponse
	 * @param pathVariables 路径变量
	 * @param parameters    表单参数&URL参数
	 * @return 返回请求结果
	 * @throws Throwable 处理失败抛出的异常
	 */
	@ResponseBody
	@Valid(requireLogin = false)
	public Object invoke(HttpServletRequest request, HttpServletResponse response,
						 @PathVariable(required = false) Map<String, Object> pathVariables,
						 @RequestHeader(required = false) Map<String, Object> defaultHeaders,
						 @RequestParam(required = false) Map<String, Object> parameters) throws Throwable {
		String clientId = null;
		Map<String, Object> headers = new LinkedCaseInsensitiveMap<>();
		headers.putAll(defaultHeaders);
		boolean requestedFromTest = configuration.isEnableWeb() && (clientId = request.getHeader(HEADER_REQUEST_CLIENT_ID)) != null && request.getHeader(HEADER_REQUEST_SCRIPT_ID) != null;
		RequestEntity requestEntity = RequestEntity.create()
				.info(requestMagicDynamicRegistry.getApiInfoFromRequest(request))
				.request(request)
				.response(response)
				.requestedFromTest(requestedFromTest)
				.pathVariables(pathVariables)
				.parameters(parameters);
		ApiInfo info = requestEntity.getApiInfo();
		if (info == null) {
			logger.error("{}找不到对应接口", request.getRequestURI());
			return afterCompletion(requestEntity, buildResult(requestEntity, API_NOT_FOUND, "接口不存在"));
		}
		requestEntity.setHeaders(headers);
		List<Path> paths = new ArrayList<>(info.getPaths());
		MagicConfiguration.getMagicResourceService().getGroupsByFileId(info.getId())
				.stream()
				.flatMap(it -> it.getPaths().stream())
				.filter(it -> !paths.contains(it))
				.forEach(paths::add);
		Object bodyValue = readRequestBody(requestEntity.getRequest());
		requestEntity.setRequestBody(bodyValue);
		String scriptName = MagicConfiguration.getMagicResourceService().getScriptName(info);
		MagicScriptContext context = createMagicScriptContext(scriptName, requestEntity);
		requestEntity.setMagicScriptContext(context);
		try {
			boolean disabledUnknownParameter = CONST_STRING_TRUE.equalsIgnoreCase(info.getOptionValue(Options.DISABLED_UNKNOWN_PARAMETER));
			// 验证参数
			doValidate(scriptName, "参数", info.getParameters(), parameters, PARAMETER_INVALID, disabledUnknownParameter);

			Object wrap = requestEntity.getApiInfo().getOptionValue(Options.WRAP_REQUEST_PARAMETERS.getValue());
			if (wrap != null && StringUtils.isNotBlank(wrap.toString())) {
				context.set(wrap.toString(), requestEntity.getParameters());
			}
			String defaultDataSourceValue = requestEntity.getApiInfo().getOptionValue(Options.DEFAULT_DATA_SOURCE.getValue());
			if (defaultDataSourceValue != null) {
				context.set(Options.DEFAULT_DATA_SOURCE.getValue(), defaultDataSourceValue);
			}
			context.putMapIntoContext(requestEntity.getParameters());
			// 验证 path
			doValidate(scriptName, "path", paths, requestEntity.getPathVariables(), PATH_VARIABLE_INVALID, disabledUnknownParameter);
			context.putMapIntoContext(requestEntity.getPathVariables());
			// 设置 cookie 变量
			context.set(VAR_NAME_COOKIE, new CookieContext(requestEntity.getRequest()));
			// 验证 header
			doValidate(scriptName, "header", info.getHeaders(), headers, HEADER_INVALID, disabledUnknownParameter);
			// 设置 header 变量
			context.set(VAR_NAME_HEADER, headers);
			// 设置 session 变量
			context.set(VAR_NAME_SESSION, new SessionContext(requestEntity.getRequest().getSession()));
			// 设置 path 变量
			context.set(VAR_NAME_PATH_VARIABLE, requestEntity.getPathVariables());
			// 设置 body 变量
			if (bodyValue != null) {
				context.set(VAR_NAME_REQUEST_BODY, bodyValue);
			}
			BaseDefinition requestBody = info.getRequestBodyDefinition();
			if (requestBody != null && !CollectionUtils.isEmpty(requestBody.getChildren())) {
				requestBody.setName(StringUtils.defaultIfBlank(requestBody.getName(), "root"));
				doValidate(scriptName, VAR_NAME_REQUEST_BODY, Collections.singletonList(requestBody), new HashMap<String, Object>() {{
					put(requestBody.getName(), bodyValue);
				}}, BODY_INVALID, disabledUnknownParameter);
			}
		} catch (ValidateException e) {
			return afterCompletion(requestEntity, resultProvider.buildResult(requestEntity, RESPONSE_CODE_INVALID, e.getMessage()));
		} catch (Throwable root) {
			return afterCompletion(requestEntity, processException(requestEntity, root), root);
		}
		RequestContext.setRequestEntity(requestEntity);
		Object value;
		// 执行前置拦截器
		if ((value = doPreHandle(requestEntity)) != null) {
			return afterCompletion(requestEntity, value);
		}
		if (requestedFromTest) {
			DebugRequest debugRequest = requestEntity.getDebugRequest();
			String sessionAndScriptId = debugRequest.getRequestedClientId() + debugRequest.getRequestedScriptId();
			try {
				if (context instanceof MagicScriptDebugContext) {
					WebSocketSessionManager.addMagicScriptContext(sessionAndScriptId, (MagicScriptDebugContext) context);
				}
				MagicLoggerContext.SESSION.set(clientId);
				return invokeRequest(requestEntity);
			} finally {
				MagicLoggerContext.remove();
				WebSocketSessionManager.removeMagicScriptContext(sessionAndScriptId);
			}
		} else {
			return invokeRequest(requestEntity);
		}
	}

	private Object buildResult(RequestEntity requestEntity, JsonCode code, Object data) {
		return resultProvider.buildResult(requestEntity, code.getCode(), code.getMessage(), data);
	}

	private void removeUnknownKey(Map<String, Object> src, List<? extends BaseDefinition> definitions) {
		if (!src.isEmpty()) {
			Map<String, Object> newMap = new HashMap<>(definitions.size());
			for (BaseDefinition definition : definitions) {
				newMap.put(definition.getName(), src.get(definition.getName()));
			}
			src.clear();
			src.putAll(newMap);
		}
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

	private Map<String, Object> doValidate(String scriptName, String comment, List<? extends BaseDefinition> validateParameters, Map<String, Object> parameters, JsonCode jsonCode, boolean disabledUnknownParameter) {
		parameters = parameters != null ? parameters : EMPTY_MAP;
		if (CollectionUtils.isEmpty(validateParameters)) {
			return parameters;
		}
		if (disabledUnknownParameter) {
			removeUnknownKey(parameters, validateParameters);
		}
		for (BaseDefinition parameter : validateParameters) {
			if (parameter.getDataType() == DataType.Any) {
				continue;
			}
			// 针对requestBody多层级的情况
			if (DataType.Object == parameter.getDataType()) {
				if (doValidateBody(comment, parameter, parameters, jsonCode, Map.class)) {
					continue;
				}
				doValidate(scriptName, VAR_NAME_REQUEST_BODY, parameter.getChildren(), (Map) parameters.get(parameter.getName()), jsonCode, disabledUnknownParameter);
			} else if (DataType.Array == parameter.getDataType()) {
				if (doValidateBody(comment, parameter, parameters, jsonCode, List.class)) {
					continue;
				}
				List<Object> list = (List) parameters.get(parameter.getName());
				if (list != null) {
					List<Map<String, Object>> newList = list.stream().map(it -> doValidate(scriptName, VAR_NAME_REQUEST_BODY, parameter.getChildren(), new HashMap<String, Object>() {{
						put(Constants.EMPTY, it);
					}}, jsonCode, disabledUnknownParameter)).collect(Collectors.toList());
					for (int i = 0, size = newList.size(); i < size; i++) {
						list.set(i, newList.get(i).get(Constants.EMPTY));
					}
				}

			} else if (StringUtils.isNotBlank(parameter.getName()) || parameters.containsKey(parameter.getName())) {
				boolean isFile = parameter.getDataType() == DataType.MultipartFile || parameter.getDataType() == DataType.MultipartFiles;
				String requestValue = StringUtils.defaultIfBlank(Objects.toString(parameters.get(parameter.getName()), Constants.EMPTY), Objects.toString(parameter.getDefaultValue(), Constants.EMPTY));
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
					// 正则验证
					if (VALIDATE_TYPE_PATTERN.equals(parameter.getValidateType())) {
						String expression = parameter.getExpression();
						if (StringUtils.isNotBlank(expression) && !PatternUtils.match(Objects.toString(value, Constants.EMPTY), expression)) {
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
				context.setScriptName(scriptName);
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
				return dataType.getInvoker().invoke0(decimal, null, EMPTY_OBJECT_ARRAY);
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
			throw new IllegalArgumentException(throwable);
		}
	}

	private Object invokeRequest(RequestEntity requestEntity) throws Throwable {
		try {
			MagicScriptContext context = requestEntity.getMagicScriptContext();
			Object result = ScriptManager.executeScript(requestEntity.getApiInfo().getScript(), context);
			Object value = result;
			// 执行后置拦截器
			if ((value = doPostHandle(requestEntity, value)) != null) {
				return afterCompletion(requestEntity, value);
			}
			// 对返回结果包装处理
			return afterCompletion(requestEntity, response(requestEntity, result));
		} catch (Throwable root) {
			return afterCompletion(requestEntity, processException(requestEntity, root), root);
		} finally {
			RequestContext.remove();
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
		if (se != null && requestEntity.isRequestedFromTest()) {
			Span.Line line = se.getLine();
			WebSocketSessionManager.sendByClientId(requestEntity.getDebugRequest().getRequestedClientId(), EXCEPTION, Arrays.asList(
					requestEntity.getDebugRequest().getRequestedScriptId(),
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
	private MagicScriptContext createMagicScriptContext(String scriptName, RequestEntity requestEntity) {
		DebugRequest debugRequest = requestEntity.getDebugRequest();
		List<Integer> breakpoints = debugRequest.getRequestedBreakpoints();
		// 构建脚本上下文
		MagicScriptContext context;
		// TODO 安全校验
		if (requestEntity.isRequestedFromDebug() && breakpoints.size() > 0) {
			context = debugRequest.createMagicScriptContext(configuration.getDebugTimeout());
		} else {
			context = new MagicScriptContext();
		}
		context.setScriptName(scriptName);
		return context;
	}


	/**
	 * 包装返回结果
	 */
	private Object response(RequestEntity requestEntity, Object value) {
		if (value instanceof ResponseEntity) {
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
				return afterCompletion(requestEntity, target);
			}
		}
		return null;
	}

	private Object afterCompletion(RequestEntity requestEntity, Object returnValue) {
		return afterCompletion(requestEntity, returnValue, null);
	}

	private Object afterCompletion(RequestEntity requestEntity, Object returnValue, Throwable throwable) {
		for (RequestInterceptor requestInterceptor : configuration.getRequestInterceptors()) {
			try {
				requestInterceptor.afterCompletion(requestEntity, returnValue, throwable);
			} catch (Exception e) {
				logger.warn("执行afterCompletion出现出错", e);
			}
		}
		Set<String> exposeHeaders = new HashSet<>(16);
		if (returnValue instanceof ResponseEntity) {
			exposeHeaders.addAll(((ResponseEntity<?>) returnValue).getHeaders().keySet());
		}
		if (requestEntity.isRequestedFromTest()) {
			HttpServletResponse response = requestEntity.getResponse();
			exposeHeaders.addAll(response.getHeaderNames());
			exposeHeaders.addAll(DEFAULT_ALLOW_READ_RESPONSE_HEADERS);
		}
		if (!exposeHeaders.isEmpty()) {
			requestEntity.getResponse().setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, String.join(",", exposeHeaders));
		}
		return returnValue;
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
