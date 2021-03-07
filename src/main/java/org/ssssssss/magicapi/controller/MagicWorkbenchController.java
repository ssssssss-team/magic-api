package org.ssssssss.magicapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.Options;
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

	private static final Logger logger = LoggerFactory.getLogger(MagicWorkbenchController.class);

	public MagicWorkbenchController(MagicConfiguration configuration) {
		super(configuration);
	}

	/**
	 * 登录
	 */
	@RequestMapping("/login")
	@ResponseBody
	public JsonBean<Boolean> login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
		if (username != null && password != null && Objects.equals(username, configuration.getUsername()) && Objects.equals(password, configuration.getPassword())) {
			response.setHeader(configuration.getTokenKey(), MD5Utils.encrypt(String.format("%s||%s", username, password)));
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
