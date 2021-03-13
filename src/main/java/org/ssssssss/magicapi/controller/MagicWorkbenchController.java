package org.ssssssss.magicapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.resource.ZipResource;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.modules.ResponseModule;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.provider.StoreServiceProvider;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.magicapi.utils.PathUtils;

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

	@RequestMapping(value = "/config-js")
	@ResponseBody
	public ResponseEntity<byte[]> configjs() {
		ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok().contentType(MediaType.parseMediaType("application/javascript"));
		if (configuration.getEditorConfig() != null) {
			try {
				File file = ResourceUtils.getFile(configuration.getEditorConfig());
				return responseBuilder.body(Files.readAllBytes(Paths.get(file.toURI())));
			} catch (IOException e) {
				logger.warn("读取编辑器配置文件{}失败", configuration.getEditorConfig());
			}
		}
		return responseBuilder.body("var MAGIC_EDITOR_CONFIG = {}".getBytes());
	}

	@RequestMapping("/download")
	@Valid(authorization = RequestInterceptor.Authorization.DOWNLOAD)
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
	@Valid(readonly = false, authorization = RequestInterceptor.Authorization.UPLOAD)
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
			if (groupServiceProvider.getGroupResource(group.getId()).exists()) {
				groupServiceProvider.update(group);
			} else {
				groupServiceProvider.insert(group);
			}
		}
		Resource backups = configuration.getWorkspace().getDirectory("backups");
		// 保存
		write(configuration.getMagicApiService(),backups,apiInfos);
		write(configuration.getFunctionServiceProvider(),backups,functionInfos);
		// 重新注册
		configuration.getMappingHandlerMapping().registerAllMapping();
		configuration.getMagicFunctionManager().registerAllFunction();
		return new JsonBean<>(SUCCESS, true);
	}

	private <T extends MagicEntity> void write(StoreServiceProvider<T> provider, Resource backups, Set<T> infos) {
		for (T info : infos) {
			Resource resource = configuration.getGroupServiceProvider().getGroupResource(info.getGroupId());
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
			boolean isApi = "1".equals(group.getType());
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
		resource.export(os, "backups");
		return ResponseModule.download(os.toByteArray(), filename);
	}
}
