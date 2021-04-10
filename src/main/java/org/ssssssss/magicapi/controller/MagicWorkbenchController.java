package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.resource.ZipResource;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.modules.ResponseModule;
import org.ssssssss.magicapi.modules.SQLModule;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.StoreServiceProvider;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.PathUtils;
import org.ssssssss.script.MagicResourceLoader;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.ScriptClass;
import org.ssssssss.script.parsing.Span;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicWorkbenchController extends MagicController implements MagicExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(MagicWorkbenchController.class);

	public MagicWorkbenchController(MagicConfiguration configuration) {
		super(configuration);
		// 给前端添加代码提示
		MagicScriptEngine.addScriptClass(SQLModule.class);
		MagicScriptEngine.addScriptClass(MagicAPIService.class);
	}

	/**
	 * 获取所有class
	 */
	@RequestMapping("/classes")
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
	@RequestMapping("/class")
	@ResponseBody
	public JsonBean<List<ScriptClass>> clazz(String className) {
		return new JsonBean<>(MagicScriptEngine.getScriptClass(className));
	}

	/**
	 * 登录
	 */
	@RequestMapping("/login")
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


	/**
	 * 创建控制台输出
	 */
	@RequestMapping("/console")
	@Valid(requireLogin = false)
	public SseEmitter console() throws IOException {
		String sessionId = UUID.randomUUID().toString().replace("-", "");
		SseEmitter emitter = MagicLoggerContext.createEmitter(sessionId);
		emitter.send(SseEmitter.event().data(sessionId).name("create"));
		return emitter;
	}

	@RequestMapping("/options")
	@ResponseBody
	@Valid(requireLogin = false)
	public JsonBean<List<List<String>>> options() {
		return new JsonBean<>(Stream.of(Options.values()).map(item -> Arrays.asList(item.getValue(), item.getName(), item.getDefaultValue())).collect(Collectors.toList()));
	}

	@RequestMapping("/search")
	@ResponseBody
	public JsonBean<List<Map<String, Object>>> search(String keyword, String type) {
		if (StringUtils.isBlank(keyword)) {
			return new JsonBean<>(Collections.emptyList());
		}
		List<MagicEntity> entities = new ArrayList<>();
		if (!"2".equals(type)) {
			entities.addAll(configuration.getMappingHandlerMapping().getApiInfos());
		}
		if (!"1".equals(type)) {
			entities.addAll(configuration.getMagicFunctionManager().getFunctionInfos());
		}
		return new JsonBean<>(entities.stream().filter(it -> it.getScript().contains(keyword)).map(it -> {
			String script = it.getScript();
			int index = script.indexOf(keyword);
			int endIndex = script.indexOf("\n", index + keyword.length());
			Span span = new Span(script, index, endIndex == -1 ? script.length() : endIndex);
			return new HashMap<String, Object>() {
				{
					put("id", it.getId());
					put("text", span.getText().trim());
					put("line", span.getLine().getLineNumber());
					put("type", it instanceof ApiInfo ? 1 : 2);
				}
			};
		}).collect(Collectors.toList()));
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
					return responseBuilder.body(new InputStreamResource(new ClassPathResource(path).getInputStream()));
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
	public ResponseEntity<?> download(String groupId) throws IOException {
		if (StringUtils.isBlank(groupId)) {
			return download(configuration.getWorkspace(), "magic-api-all.zip");
		} else {
			Resource resource = configuration.getGroupServiceProvider().getGroupResource(groupId);
			notNull(resource, GROUP_NOT_FOUND);
			return download(resource, "magic-api-group.zip");
		}
	}

	@RequestMapping("/upload")
	@Valid(readonly = false, authorization = Authorization.UPLOAD)
	@ResponseBody
	public JsonBean<Boolean> upload(MultipartFile file, String mode) throws IOException {
		notNull(file, FILE_IS_REQUIRED);
		ZipResource root = new ZipResource(file.getInputStream());
		Set<String> apiPaths = new HashSet<>();
		Set<String> functionPaths = new HashSet<>();
		Set<Group> groups = new HashSet<>();
		Set<ApiInfo> apiInfos = new HashSet<>();
		Set<FunctionInfo> functionInfos = new HashSet<>();
		// 检查上传资源中是否有冲突
		isTrue(readPaths(groups, apiPaths, functionPaths, apiInfos, functionInfos, "/", root), UPLOAD_PATH_CONFLICT);
		// 判断是否是强制上传
		if (!"force".equals(mode)) {
			// 检测与已注册的接口和函数是否有冲突
			isTrue(!configuration.getMappingHandlerMapping().hasRegister(apiPaths), UPLOAD_PATH_CONFLICT.format("接口"));
			isTrue(!configuration.getMagicFunctionManager().hasRegister(apiPaths), UPLOAD_PATH_CONFLICT.format("函数"));
		}
		Resource item = root.getResource("group.json");
		GroupServiceProvider groupServiceProvider = configuration.getGroupServiceProvider();
		if (item.exists()) {
			Group group = groupServiceProvider.readGroup(item);
			// 检查分组是否存在
			isTrue("0".equals(group.getParentId()) || groupServiceProvider.getGroupResource(group.getParentId()).exists(), GROUP_NOT_FOUND);
			groups.removeIf(it -> it.getId().equalsIgnoreCase(group.getId()));
		}
		for (Group group : groups) {
			Resource groupResource = groupServiceProvider.getGroupResource(group.getId());
			if (groupResource != null && groupResource.exists()) {
				groupServiceProvider.update(group);
			} else {
				groupServiceProvider.insert(group);
			}
		}
		Resource backups = configuration.getWorkspace().getDirectory(Constants.PATH_BACKUPS);
		// 保存
		write(configuration.getMagicApiService(), backups, apiInfos);
		write(configuration.getFunctionServiceProvider(), backups, functionInfos);
		// 重新注册
		configuration.getMappingHandlerMapping().registerAllMapping();
		configuration.getMagicFunctionManager().registerAllFunction();
		return new JsonBean<>(SUCCESS, true);
	}

	private <T extends MagicEntity> void write(StoreServiceProvider<T> provider, Resource backups, Set<T> infos) {
		for (T info : infos) {
			Resource resource = configuration.getGroupServiceProvider().getGroupResource(info.getGroupId());
			resource = resource.getResource(info.getName() + ".ms");
			byte[] content = provider.serialize(info);
			resource.write(content);
			Resource directory = backups.getDirectory(info.getId());
			if (!directory.exists()) {
				directory.mkdir();
			}
			directory.getResource(System.currentTimeMillis() + ".ms").write(content);
			resource.write(content);
		}
	}

	private boolean readPaths(Set<Group> groups, Set<String> apiPaths, Set<String> functionPaths, Set<ApiInfo> apiInfos, Set<FunctionInfo> functionInfos, String parentPath, Resource root) {
		Resource resource = root.getResource("group.json");
		String path = "";
		if (resource.exists()) {
			Group group = JsonUtils.readValue(resource.read(), Group.class);
			groups.add(group);
			path = Objects.toString(group.getPath(), "");
			boolean isApi = Constants.GROUP_TYPE_API.equals(group.getType());
			for (Resource file : root.files(".ms")) {
				boolean conflict;
				if (isApi) {
					ApiInfo info = configuration.getMagicApiService().deserialize(file.read());
					apiInfos.add(info);
					conflict = !apiPaths.add(Objects.toString(info.getMethod(), "GET") + ":" + PathUtils.replaceSlash(parentPath + "/" + path + "/" + info.getPath()));
				} else {
					FunctionInfo info = configuration.getFunctionServiceProvider().deserialize(file.read());
					functionInfos.add(info);
					conflict = !functionPaths.add(PathUtils.replaceSlash(parentPath + "/" + path + "/" + info.getPath()));
				}
				if (conflict) {
					return false;
				}
			}
		}
		for (Resource directory : root.dirs()) {
			if (!readPaths(groups, apiPaths, functionPaths, apiInfos, functionInfos, PathUtils.replaceSlash(parentPath + "/" + path), directory)) {
				return false;
			}
		}
		return true;
	}

	private ResponseEntity<?> download(Resource resource, String filename) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		resource.export(os, Constants.PATH_BACKUPS);
		return ResponseModule.download(os.toByteArray(), filename);
	}
}
