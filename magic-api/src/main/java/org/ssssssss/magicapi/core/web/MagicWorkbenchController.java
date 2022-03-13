package org.ssssssss.magicapi.core.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.core.exception.MagicLoginException;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.modules.servlet.ResponseModule;
import org.ssssssss.magicapi.modules.db.SQLModule;
import org.ssssssss.magicapi.core.service.MagicAPIService;
import org.ssssssss.magicapi.utils.ClassScanner;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.SignUtils;
import org.ssssssss.magicapi.utils.WebUtils;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.ScriptClass;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Tokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicWorkbenchController extends MagicController implements MagicExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(MagicWorkbenchController.class);

	private static final Pattern SINGLE_LINE_COMMENT_TODO = Pattern.compile("((TODO)|(todo)|(fixme)|(FIXME))[ \t]+[^\n]+");

	private static final Pattern MULTI_LINE_COMMENT_TODO = Pattern.compile("((TODO)|(todo)|(fixme)|(FIXME))[ \t]+[^\n(?!*/)]+");

	private final String secretKey;

	private final List<Plugin> plugins;

	private final MagicAPIProperties properties;

	private String allClassTxt;


	public MagicWorkbenchController(MagicConfiguration configuration, MagicAPIProperties properties, List<Plugin> plugins) {
		super(configuration);
		this.properties = properties;
		this.plugins = plugins;
		this.secretKey = properties.getSecretKey();
		// 给前端添加代码提示
		MagicScriptEngine.addScriptClass(SQLModule.class);
		MagicScriptEngine.addScriptClass(MagicAPIService.class);
	}

	@GetMapping({"", "/"})
	@Valid(requireLogin = false)
	public String redirectIndex(HttpServletRequest request) {
		if (request.getRequestURI().endsWith("/")) {
			return "redirect:./index.html";
		}
		return "redirect:" + properties.getWeb() + "/index.html";
	}

	@GetMapping("/config.json")
	@Valid(requireLogin = false)
	@ResponseBody
	public MagicAPIProperties readConfig() {
		return properties;
	}

	@GetMapping(value = "/classes.txt", produces = "text/plain")
	@ResponseBody
	@Valid(requireLogin = false)
	private String readClass() {
		if (allClassTxt == null) {
			try {
				allClassTxt = ClassScanner.compress(ClassScanner.scan());
			} catch (Throwable t) {
				logger.warn("扫描Class失败", t);
				allClassTxt = "";
			}
		}
		return allClassTxt;
	}

	/**
	 * 获取所有class
	 */
	@PostMapping("/classes")
	@ResponseBody
	@Valid(requireLogin = false)
	public JsonBean<Map<String, Object>> classes() {
		Map<String, ScriptClass> classMap = MagicScriptEngine.getScriptClassMap();
		classMap.putAll(MagicResourceLoader.getModules());
		Map<String, Object> values = new HashMap<>();
		values.put("classes", classMap);
		values.put("extensions", MagicScriptEngine.getExtensionScriptClass());
		values.put("functions", MagicScriptEngine.getFunctions());
		return new JsonBean<>(values);
	}

	/**
	 * 获取单个class
	 *
	 * @param className 类名
	 */
	@PostMapping("/class")
	@ResponseBody
	public JsonBean<Set<ScriptClass>> clazz(String className) {
		if (StringUtils.isBlank(className)) {
			return new JsonBean<>(Collections.emptySet());
		}
		return new JsonBean<>(MagicScriptEngine.getScriptClass(className));
	}

	/**
	 * 登录
	 */
	@PostMapping("/login")
	@ResponseBody
	@Valid(requireLogin = false)
	public JsonBean<Boolean> login(String username, String password, HttpServletRequest request, HttpServletResponse response) throws MagicLoginException {
		if (configuration.getAuthorizationInterceptor().requireLogin()) {
			if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
				try {
					configuration.getAuthorizationInterceptor().getUserByToken(request.getHeader(Constants.MAGIC_TOKEN_HEADER));
				} catch (MagicLoginException ignored) {
					return new JsonBean<>(false);
				}
			} else {
				MagicUser user = configuration.getAuthorizationInterceptor().login(username, password);
				response.setHeader(Constants.MAGIC_TOKEN_HEADER, user.getToken());
				response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, Constants.MAGIC_TOKEN_HEADER);
			}
		}
		return new JsonBean<>(true);
	}

	@PostMapping("/user")
	@ResponseBody
	public JsonBean<MagicUser> user(HttpServletRequest request) {
		if (configuration.getAuthorizationInterceptor().requireLogin()) {
			try {
				return new JsonBean<>(configuration.getAuthorizationInterceptor().getUserByToken(request.getHeader(Constants.MAGIC_TOKEN_HEADER)));
			} catch (MagicLoginException ignored) {

			}
		}
		return new JsonBean<>(MagicUser.guest());
	}

	@PostMapping("/logout")
	@ResponseBody
	@Valid(requireLogin = false)
	public JsonBean<Void> logout(HttpServletRequest request) {
		configuration.getAuthorizationInterceptor().logout(request.getHeader(Constants.MAGIC_TOKEN_HEADER));
		return new JsonBean<>();
	}

	@GetMapping("/plugins")
	@Valid(requireLogin = false)
	@ResponseBody
	public JsonBean<List<Plugin>> plugins() {
		return new JsonBean<>(plugins);
	}


	@RequestMapping("/options")
	@ResponseBody
	@Valid(requireLogin = false)
	public JsonBean<List<List<String>>> options() {
		return new JsonBean<>(Stream.of(Options.values()).map(item -> Arrays.asList(item.getValue(), item.getName(), item.getDefaultValue())).collect(Collectors.toList()));
	}

	@GetMapping("/reload")
	@ResponseBody
	public JsonBean<Boolean> reload(HttpServletRequest request) {
		isTrue(allowVisit(request, Authorization.RELOAD), PERMISSION_INVALID);
		MagicConfiguration.getMagicResourceService().refresh();
		return new JsonBean<>(true);
	}

	@GetMapping("/search")
	@ResponseBody
	public JsonBean<List<Map<String, Object>>> search(String keyword, HttpServletRequest request) {
		if (StringUtils.isBlank(keyword)) {
			return new JsonBean<>(Collections.emptyList());
		}
		return new JsonBean<>(entities(request, Authorization.VIEW)
				.stream()
				.filter(it -> it.getScript().contains(keyword))
				.map(it -> {
					String script = it.getScript();
					int index = script.indexOf(keyword);
					int endIndex = script.indexOf("\n", index + keyword.length());
					index = script.lastIndexOf("\n", index) + 1;
					Span span = new Span(script, index, endIndex == -1 ? script.length() : endIndex);
					return new HashMap<String, Object>() {
						{
							put("id", it.getId());
							put("text", span.getText().trim());
							put("line", span.getLine().getLineNumber());
						}
					};
				}).collect(Collectors.toList()));
	}

	@GetMapping("/todo")
	@ResponseBody
	@Valid
	public JsonBean<List<Map<String, Object>>> todo(HttpServletRequest request) {
		List<MagicEntity> entities = entities(request, Authorization.VIEW);
		List<Map<String, Object>> result = new ArrayList<>(entities.size());
		for (MagicEntity entity : entities) {
			try {
				List<Span> comments = Tokenizer.tokenize(entity.getScript(), true).comments();
				for (Span comment : comments) {
					String text = comment.getText();
					Pattern pattern = text.startsWith("//") ? SINGLE_LINE_COMMENT_TODO : MULTI_LINE_COMMENT_TODO;
					Matcher matcher = pattern.matcher(text);
					while (matcher.find()) {
						result.add(new HashMap<String, Object>() {
							{
								put("id", entity.getId());
								put("text", matcher.group(0).trim());
								put("line", comment.getLine().getLineNumber());
							}
						});
					}
				}
			} catch (Exception ignored) {
			}

		}
		return new JsonBean<>(result);
	}

	@RequestMapping(value = "/config-js")
	@ResponseBody
	@Valid(requireLogin = false)
	public ResponseEntity<?> configJs() {
		ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok().contentType(MediaType.parseMediaType("application/javascript"));
		if (configuration.getEditorConfig() != null) {
			try {
				String path = configuration.getEditorConfig();
				if (path.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
					path = path.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
					return responseBuilder.body(IoUtils.bytes(new ClassPathResource(path).getInputStream()));
				}
				File file = ResourceUtils.getFile(configuration.getEditorConfig());
				return responseBuilder.body(Files.readAllBytes(Paths.get(file.toURI())));
			} catch (IOException e) {
				logger.warn("读取编辑器配置文件{}失败", configuration.getEditorConfig());
			}
		}
		return responseBuilder.body("var MAGIC_EDITOR_CONFIG = {}".getBytes());
	}

	@RequestMapping("/download")
	@Valid(authorization = Authorization.DOWNLOAD)
	@ResponseBody
	public ResponseEntity<?> download(String groupId, @RequestBody(required = false) List<SelectedResource> resources, HttpServletRequest request) throws IOException {
		isTrue(allowVisit(request, Authorization.DOWNLOAD), PERMISSION_INVALID);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		magicAPIService.download(groupId, resources, os);
		if (StringUtils.isBlank(groupId)) {
			return ResponseModule.download(os.toByteArray(), "magic-api-group.zip");
		} else {
			return ResponseModule.download(os.toByteArray(), "magic-api-all.zip");
		}
	}

	@RequestMapping("/upload")
	@Valid(readonly = false, authorization = Authorization.UPLOAD)
	@ResponseBody
	public JsonBean<Boolean> upload(MultipartFile file, String mode, HttpServletRequest request) throws IOException {
		notNull(file, FILE_IS_REQUIRED);
		isTrue(allowVisit(request, Authorization.UPLOAD), PERMISSION_INVALID);
		if (configuration.getMagicBackupService() != null) {
			configuration.getMagicBackupService().doBackupAll("上传前，系统自动全量备份", WebUtils.currentUserName());
		}
		return new JsonBean<>(magicAPIService.upload(file.getInputStream(), mode));
	}

	@RequestMapping("/push")
	@ResponseBody
	@Valid(authorization = Authorization.PUSH)
	public JsonBean<?> push(@RequestHeader("magic-push-target") String target, @RequestHeader("magic-push-secret-key") String secretKey,
							@RequestHeader("magic-push-mode") String mode, @RequestBody List<SelectedResource> resources,
							HttpServletRequest request) {
		isTrue(allowVisit(request, Authorization.PUSH), PERMISSION_INVALID);
		return magicAPIService.push(target, secretKey, mode, resources);
	}

	@ResponseBody
	@Valid(requireLogin = false)
	public JsonBean<Void> receivePush(MultipartFile file, String mode, Long timestamp, String sign) throws IOException {
		notNull(timestamp, SIGN_IS_INVALID);
		notBlank(mode, SIGN_IS_INVALID);
		notBlank(sign, SIGN_IS_INVALID);
		notNull(file, SIGN_IS_INVALID);
		byte[] bytes = IoUtils.bytes(file.getInputStream());
		isTrue(sign.equals(SignUtils.sign(timestamp, secretKey, mode, bytes)), SIGN_IS_INVALID);
		if (configuration.getMagicBackupService() != null) {
			configuration.getMagicBackupService().doBackupAll("推送前，系统自动全量备份", WebUtils.currentUserName());
		}
		magicAPIService.upload(new ByteArrayInputStream(bytes), mode);
		return new JsonBean<>();
	}
}
