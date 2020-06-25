package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;
import org.ssssssss.script.MagicScriptEngine;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

import java.lang.ref.ReferenceQueue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebUIController {

	private static Logger logger = LoggerFactory.getLogger(WebUIController.class);

	private JdbcTemplate template;

	public WebUIController(JdbcTemplate template) {
		this.template = template;
	}

	@RequestMapping("/delete")
	@ResponseBody
	public JsonBean<Void> delete(String id) {
		try {
			Map<String, Object> info = template.queryForMap("select * from magic_api_info where id = ?", id);
			if (info != null) {
				template.update("delete from magic_api_info where id = ?", id);
			}
			return new JsonBean<>();
		} catch (Exception e) {
			logger.error("删除接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/list")
	@ResponseBody
	public JsonBean<List<ApiInfo>> list() {
		try {
			return new JsonBean<>(template.query("select id,api_name name,api_path path,api_method method from magic_api_info order by api_update_time desc",new BeanPropertyRowMapper<ApiInfo>(ApiInfo.class)));
		} catch (Exception e) {
			logger.error("查询接口列表失败", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/continue")
	@ResponseBody
	public JsonBean<Object> debugContinue(String id){
		MagicScriptDebugContext context = MagicScriptDebugContext.getDebugContext(id);
		try {
			context.singal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(context.isRunning()){
			return new JsonBean<>(1000,context.getId(),context.getDebugInfo());
		}
		return new JsonBean<>(context.getReturnValue());
	}

	@RequestMapping("/test")
	@ResponseBody
	public JsonBean<Object> test(@RequestBody(required = false) Map<String, Object> request) {
		Object script = request.get("script");
		if (script != null) {
			request.remove("script");
			Object breakpoints = request.get("breakpoints");
			request.remove("breakpoints");
			MagicScriptDebugContext context = new MagicScriptDebugContext(request);
			try {
				context.setBreakpoints((List<Integer>) breakpoints);
				Object result = MagicScriptEngine.execute(script.toString(), context);
				if(context.isRunning()){
					return new JsonBean<>(1000,context.getId(),result);
				}
				return new JsonBean<>(result);
			} catch (MagicScriptError.ScriptException se) {
				logger.error("测试脚本出错",se);
				Throwable parent = se;
				while((parent = parent.getCause()) != null){
					if(parent instanceof MagicScriptError.ScriptException){
						se = (MagicScriptError.ScriptException)parent;
					}
				}
				Span.Line line = se.getLine();
				return new JsonBean<>(-1000, se.getSimpleMessage(), line == null ? null : Arrays.asList(line.getLineNumber(), line.getEndLineNumber(),line.getStartCol(), line.getEndCol()));
			} catch (Exception e) {
				logger.error("测试脚本出错",e);
				return new JsonBean<>(-1, e.getMessage());
			}
		}
		return new JsonBean<>(0, "脚本不能为空");
	}

	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<Map<String, Object>> get(String id) {
		try {
			return new JsonBean<>(template.queryForMap("select * from magic_api_info where id = ?", id));
		} catch (Exception e) {
			logger.error("查询接口出错");
			return new JsonBean<>(-1, e.getMessage());
		}
	}

	@RequestMapping("/save")
	@ResponseBody
	public JsonBean<String> save(ApiInfo info) {
		try {
			if (StringUtils.isBlank(info.getMethod())) {
				return new JsonBean<>(0, "请求方法不能为空");
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
			if (StringUtils.isBlank(info.getId())) {
				info.setId(UUID.randomUUID().toString().replace("-", ""));
				Integer count = template.queryForObject("select count(*) from magic_api_info where api_method = ? and api_path = ?",
						Integer.class,
						info.getMethod(),
						info.getPath());
				if (count > 0) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				long time = System.currentTimeMillis();
				template.update("insert into magic_api_info(id,api_method,api_path,api_script,api_name,api_create_time,api_update_time) values(?,?,?,?,?,?,?)",
						info.getId(),
						info.getMethod(),
						info.getPath(),
						info.getScript(),
						info.getName(),
						time,
						time);
			} else {
				Integer count = template.queryForObject("select count(*) from magic_api_info where api_method = ? and api_path = ? and id !=?",
						Integer.class,
						info.getMethod(),
						info.getPath(),
						info.getId());
				if (count > 0) {
					return new JsonBean<>(0, String.format("接口%s:%s已存在", info.getMethod(), info.getPath()));
				}
				template.update("update magic_api_info set api_method = ?,api_path = ?,api_script = ?,api_name = ?,api_update_time = ? where id = ?",
						info.getMethod(),
						info.getPath(),
						info.getScript(),
						info.getName(),
						System.currentTimeMillis(),
						info.getId());
			}
			return new JsonBean<>(info.getId());
		} catch (Exception e) {
			logger.error("保存接口出错", e);
			return new JsonBean<>(-1, e.getMessage());
		}
	}
}
