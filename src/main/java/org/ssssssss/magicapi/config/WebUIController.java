package org.ssssssss.magicapi.config;

import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptEngine;

import java.util.Map;

public class WebUIController {

	private JdbcTemplate template;

	public WebUIController(JdbcTemplate template) {
		Environment environment;
		this.template = template;
	}

	@RequestMapping("/save")
	@ResponseBody
	public JsonBean<Integer> save(){
		return new JsonBean<>(1);
	}

	@RequestMapping("/get")
	@ResponseBody
	public JsonBean<Void> get(String id){
		return new JsonBean<>();
	}

	@RequestMapping("/delete")
	@ResponseBody
	public JsonBean<Void> delete(String id){
		return new JsonBean<>();
	}

	@RequestMapping("/test")
	@ResponseBody
	public JsonBean<Object> test(@RequestBody(required = false)Map<String, Object> request){
		Object script = request.get("script");
		if(script != null){
			try {
				long st = System.currentTimeMillis();
				Object result = MagicScriptEngine.execute(script.toString(),request);
				System.out.println(System.currentTimeMillis() - st + "ms");
				return new JsonBean<>(result);
			} catch (Exception e) {
				return new JsonBean<>(-1,e.getMessage());
			}
		}
		return new JsonBean<>(0,"脚本不能为空");
	}
}
