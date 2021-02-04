package org.ssssssss.magicapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ResourceUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.Options;
import org.ssssssss.magicapi.model.SynchronizeRequest;
import org.ssssssss.magicapi.model.SynchronizeResponse;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.utils.MD5Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicWorkbenchController extends MagicController {

	private static Logger logger = LoggerFactory.getLogger(MagicWorkbenchController.class);

	private RestTemplate restTemplate = new RestTemplate();

	private ApiServiceProvider agiApiService;

	private GroupServiceProvider groupService;

	public MagicWorkbenchController(MagicConfiguration configuration) {
		super(configuration);
		this.agiApiService = configuration.getMagicApiService();
		this.groupService = configuration.getGroupServiceProvider();
	}

	/**
	 * 登录
	 */
	@RequestMapping("/login")
	@ResponseBody
	public JsonBean<Boolean> login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
		if (username != null && password != null && Objects.equals(username, configuration.getUsername()) && Objects.equals(password, configuration.getPassword())) {
			response.setHeader(configuration.getTokenKey(),MD5Utils.encrypt(String.format("%s||%s", username, password)));
			response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, configuration.getTokenKey());
			return new JsonBean<>(true);
		} else if (allowVisit(request, null)) {
			return new JsonBean<>(true);
		}
		return new JsonBean<>(false);
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

	@RequestMapping("/options")
	@ResponseBody
	public JsonBean<List<Map<String, String>>> options() {
		return new JsonBean<>(Stream.of(Options.values()).map(item -> Collections.singletonMap(item.getValue(), item.getName())).collect(Collectors.toList()));
	}


	private String validateRequest(SynchronizeRequest request) {
		if (StringUtils.isBlank(request.getMode())) {
			return "请求参数有误";
		}
		if (request.getMode().equals("1") && StringUtils.isBlank(request.getGroupId())) {
			return "分组id不能为空";
		}
		if (request.getMode().equals("2") && (StringUtils.isBlank(request.getApiId()) || StringUtils.isBlank(request.getFunctionId()))) {
			return "函数id或接口id不能同时为空";
		}
		return null;
	}

	@RequestMapping("/synchronize")
	@ResponseBody
	public JsonBean<SynchronizeResponse> synchronize(SynchronizeRequest synchronizeRequest, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SYNC)) {
			return new JsonBean<>(-10, "无权限执行同步方法");
		}
		String message = validateRequest(synchronizeRequest);
		if (message == null) {
			List<SynchronizeRequest.Info> infos = agiApiService.listForSync(null, synchronizeRequest.getApiId());
			infos.forEach(it -> it.setGroupPath(groupService.getFullPath(it.getGroupId())));
			synchronizeRequest.setInfos(infos);
			return request(synchronizeRequest);
		}
		return new JsonBean<>(0, message);
	}

	@RequestMapping("/synchronize/pull")
	@ResponseBody
	public JsonBean<Void> pull(SynchronizeRequest synchronizeRequest, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.PULL)) {
			return new JsonBean<>(-10, "无权限执行拉取方法");
		}
		return null;
	}

	@RequestMapping("/synchronize/push")
	@ResponseBody
	public JsonBean<Void> push(SynchronizeRequest synchronizeRequest, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.PUSH)) {
			return new JsonBean<>(-10, "无权限执行推送方法");
		}
		return null;
	}

	private JsonBean<SynchronizeResponse> request(SynchronizeRequest request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		String requestURL = String.format("%s/_synchronize?secret=%s", request.getRemote(), request.getSecret());
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		return restTemplate.exchange(requestURL, HttpMethod.POST, entity, new ParameterizedTypeReference<JsonBean<SynchronizeResponse>>() {
		}).getBody();
	}


	@RequestMapping(value = "/config-js", produces = "application/javascript")
	@ResponseBody
	public Object configjs() {
		if (configuration.getEditorConfig() != null) {
			try {
				File file = ResourceUtils.getFile(configuration.getEditorConfig());
				return Files.readAllBytes(Paths.get(file.toURI()));
			} catch (IOException e) {
				logger.warn("读取编辑器配置文件{}失败", configuration.getEditorConfig());
			}
		}
		return "var MAGIC_EDITOR_CONFIG = {}";
	}
}
