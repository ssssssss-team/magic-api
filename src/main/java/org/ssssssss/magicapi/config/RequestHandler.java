package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.context.CookieContext;
import org.ssssssss.magicapi.context.HeaderContext;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.context.SessionContext;
import org.ssssssss.magicapi.functions.ResponseFunctions;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		ApiInfo info;
		try {
			RequestContext.setRequestAttribute(request, response);
			//	找到对应的接口信息
			info = MappingHandlerMapping.getMappingApiInfo(request);
			if (info == null) {
				logger.error("接口不存在");
				return resultProvider.buildResult(1001, "fail", "接口不存在");
			}
			// 构建脚本上下文
			MagicScriptContext context = new MagicScriptContext();
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
			if (httpMessageConverters != null && request.getContentType() != null) {
				MediaType mediaType = MediaType.valueOf(request.getContentType());
				Class clazz = Map.class;
				for (HttpMessageConverter<?> converter : httpMessageConverters) {
					if (converter.canRead(clazz, mediaType)) {
						context.set("body", converter.read(clazz, new ServletServerHttpRequest(request)));
						break;
					}
				}
			}
			// 执行前置拦截器
			for (RequestInterceptor requestInterceptor : requestInterceptors) {
				Object value = requestInterceptor.preHandle(info, context);
				if (value != null) {
					return value;
				}
			}
			SimpleScriptContext simpleScriptContext = new SimpleScriptContext();
			simpleScriptContext.setAttribute(MagicScript.CONTEXT_ROOT, context, ScriptContext.ENGINE_SCOPE);
			// 执行脚本
			Object value = ScriptManager.compile("MagicScript", info.getScript()).eval(simpleScriptContext);
			// 执行后置拦截器
			for (RequestInterceptor requestInterceptor : requestInterceptors) {
				Object target = requestInterceptor.postHandle(info, context, value);
				if (target != null) {
					return target;
				}
			}
			if (value instanceof ResponseEntity) {
				return value;
			} else if (value instanceof ResponseFunctions.NullValue){
				return null;
			}
			return resultProvider.buildResult(value);
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			logger.error("接口请求出错", root);
			return resultProvider.buildResult(root);
		} finally {
			RequestContext.remove();
		}
	}
}
