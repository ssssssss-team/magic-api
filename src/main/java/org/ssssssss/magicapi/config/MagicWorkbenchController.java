package org.ssssssss.magicapi.config;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.Options;
import org.ssssssss.magicapi.utils.MD5Utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicWorkbenchController extends MagicController {

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
}
