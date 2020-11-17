package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.ssssssss.magicapi.functions.SQLExecutor;
import org.ssssssss.magicapi.logging.MagicLoggerContext;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.utils.MD5Utils;
import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.ScriptClass;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 页面UI对应的Controller
 */
public class WebUIController {

	private static Logger logger = LoggerFactory.getLogger(WebUIController.class);


	/**
	 * 接口映射
	 */
	private MappingHandlerMapping mappingHandlerMapping;

	/**
	 * 接口查询service
	 */
	private ApiServiceProvider magicApiService;


	/**
	 * 拦截器
	 */
	private List<RequestInterceptor> requestInterceptors = new ArrayList<>();

	/**
	 * 动态数据源
	 */
	private MagicDynamicDataSource magicDynamicDataSource;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 密码
	 */
	private String password;

	private final String tokenKey = "MAGICTOKEN";

	public WebUIController() {
		// 给前端添加代码提示
		MagicScriptEngine.addScriptClass(SQLExecutor.class);
		MagicScriptEngine.addScriptClass(MagicAPIService.class);
	}

	public void setMagicDynamicDataSource(MagicDynamicDataSource magicDynamicDataSource) {
		this.magicDynamicDataSource = magicDynamicDataSource;
	}

	public void addRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.requestInterceptors.add(requestInterceptor);
	}

	public void setMappingHandlerMapping(MappingHandlerMapping mappingHandlerMapping) {
		this.mappingHandlerMapping = mappingHandlerMapping;
	}

	public void setMagicApiService(ApiServiceProvider magicApiService) {
		this.magicApiService = magicApiService;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 删除接口
	 *
	 * @param request
	 * @param id      接口ID
	 */
	@RequestMapping("/delete")
	@ResponseBody
	public JsonBean<Boolean> delete(HttpServletRequest request, String id) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = this.magicApiService.delete(id);
			if (success) {    //删除成功时在取消注册
				mappingHandlerMapping.unregisterMapping(id, true);
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 删除接口分组
	 *
	 * @param apiIds    接口ID列表，逗号分隔
	 * @param groupName 分组名称
	 */
	@RequestMapping("/group/delete")
	@ResponseBody
	public JsonBean<Boolean> deleteGroup(HttpServletRequest request, String apiIds, String groupName) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DELETE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = this.magicApiService.deleteGroup(groupName);
			if (success) {    //删除成功时取消注册
				if (StringUtils.isNotBlank(apiIds)) {
					String[] ids = apiIds.split(",");
					if (ids.length > 0) {
						for (String id : ids) {
							mappingHandlerMapping.unregisterMapping(id, true);
						}
					}
				}
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 修改分组
	 *
	 * @param groupName    分组名称
	 * @param oldGroupName 原分组名称
	 * @param prefix       分组前缀
	 */
	@RequestMapping("/group/update")
	@ResponseBody
	public JsonBean<Boolean> groupUpdate(String groupName, String oldGroupName, String prefix, HttpServletRequest request) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行删除方法");
		}
		try {
			boolean success = magicApiService.updateGroup(oldGroupName, groupName, prefix);
			if (success) {
				mappingHandlerMapping.updateGroupPrefix(oldGroupName, groupName, prefix);
			}
			return new JsonBean<>(success);
		} catch (Exception e) {
			logger.error("修改分组出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询所有接口
	 */
	@RequestMapping("/list")
	@ResponseBody
	public JsonBean<List<ApiInfo>> list() {
		try {
			return new JsonBean<>(magicApiService.list());
		} catch (Exception e) {
			logger.error("查询接口列表失败", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 登录
	 */
	@RequestMapping("/login")
	@ResponseBody
	public JsonBean<Boolean> login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
		if (username != null && password != null && Objects.equals(username, this.username) && Objects.equals(password, this.password)) {
			Cookie cookie = new Cookie(tokenKey, MD5Utils.encrypt(String.format("%s||%s", username, password)));
			cookie.setHttpOnly(true);
			response.addCookie(cookie);
			return new JsonBean<>(true);
		} else if (allowVisit(request, null)) {
			return new JsonBean<>(true);
		}
		return new JsonBean<>(false);
	}

	/**
	 * 获取所有class
	 */
	@RequestMapping("/classes")
	@ResponseBody
	public JsonBean<Map<String, Map<String, ScriptClass>>> classes() {
		Map<String, ScriptClass> classMap = MagicScriptEngine.getScriptClassMap();
		classMap.putAll(MagicModuleLoader.getModules());
		ScriptClass db = classMap.get(SQLExecutor.class.getName());
		if (db != null) {
			List<ScriptClass.ScriptAttribute> attributes =  new ArrayList<>();
			// 给与前台动态数据源提示
			magicDynamicDataSource.datasources().stream().filter(StringUtils::isNotBlank)
					.forEach(item -> attributes.add(new ScriptClass.ScriptAttribute("db", item)));
			db.setAttributes(attributes);
		}
		Map<String, Map<String, ScriptClass>> values = new HashMap<>();
		values.put("classes", classMap);
		values.put("extensions", MagicScriptEngine.getExtensionScriptClass());
		return new JsonBean<>(values);
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
	 * 查询接口详情
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<ApiInfo> get(HttpServletRequest request, String id) {
		if (!allowVisit(request, RequestInterceptor.Authorization.DETAIL)) {
			return new JsonBean<>(-10, "无权限执行查看详情方法");
		}
		try {
			return new JsonBean<>(this.magicApiService.get(id));
		} catch (Exception e) {
			logger.error("查询接口出错");
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 查询历史记录
	 *
	 * @param id 接口ID
	 */
	@RequestMapping("/backups")
	@ResponseBody
	public JsonBean<List<Long>> backups(String id) {
		return new JsonBean<>(magicApiService.backupList(id));
	}

	/**
	 * 获取历史记录
	 *
	 * @param id        接口ID
	 * @param timestamp 时间点
	 */
	@RequestMapping("/backup/get")
	@ResponseBody
	public JsonBean<ApiInfo> backups(String id, Long timestamp) {
		return new JsonBean<>(magicApiService.backupInfo(id, timestamp));
	}

	/**
	 * 保存接口
	 *
	 * @param info 接口信息
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonBean<String> save(HttpServletRequest request, ApiInfo info) {
		if (!allowVisit(request, RequestInterceptor.Authorization.SAVE)) {
			return new JsonBean<>(-10, "无权限执行保存方法");
		}
		try {
			if (StringUtils.isBlank(info.getMethod())) {
				return new JsonBean<>(0, "请求方法不能为空");
			}
			if (info.getGroupName() != null && (info.getGroupName().contains("'") || info.getGroupName().contains("\""))) {
				return new JsonBean<>(0, "分组名不能包含特殊字符' \"");
			}
			if (info.getGroupPrefix() != null && (info.getGroupPrefix().contains("'") || info.getGroupPrefix().contains("\""))) {
				return new JsonBean<>(0, "分组前缀不能包含特殊字符' \"");
			}
			if (StringUtils.isBlank(info.getPath())) {
				return new JsonBean<>(0, "请求路径不能为空");
			}
			if (StringUtils.isBlank(info.getName())) {
				return new JsonBean<>(0, "接口名称不能为空");
			}
			if (StringUtils.isBlank(info.getScript())) {
				return new JsonBean<>(0, "脚本内容不能为空");
			}
			if (mappingHandlerMapping.hasRegisterMapping(info)) {
				return new JsonBean<>(0, "该路径已被映射,请换一个请求方法或路径");
			}
			if (StringUtils.isBlank(info.getId())) {
				// 先判断接口是否存在
				if (magicApiService.exists(info.getGroupPrefix(), info.getMethod(), info.getPath())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.insert(info);
			} else {
				// 先判断接口是否存在
				if (magicApiService.existsWithoutId(info.getGroupPrefix(), info.getMethod(), info.getPath(), info.getId())) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				magicApiService.update(info);
			}
			magicApiService.backup(info.getId());
			// 注册接口
			mappingHandlerMapping.registerMapping(info, true);
			return new JsonBean<>(info.getId());
		} catch (Exception e) {
			logger.error("保存接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	/**
	 * 判断是否有权限访问按钮
	 */
	boolean allowVisit(HttpServletRequest request, RequestInterceptor.Authorization authorization) {
		if (authorization == null) {
			if (this.username != null && this.password != null) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (tokenKey.equals(cookie.getName())) {
							return cookie.getValue().equals(MD5Utils.encrypt(String.format("%s||%s", username, password)));
						}
					}
				}
				return false;
			}
			return true;
		}
		for (RequestInterceptor requestInterceptor : requestInterceptors) {
			if (!requestInterceptor.allowVisit(request, authorization)) {
				return false;
			}
		}
		return true;
	}
}
