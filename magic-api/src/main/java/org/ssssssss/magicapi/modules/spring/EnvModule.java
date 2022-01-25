package org.ssssssss.magicapi.modules.spring;

import org.springframework.core.env.Environment;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.annotation.Comment;

/**
 * env模块
 *
 * @author mxd
 */
@MagicModule("env")
public class EnvModule {

	private final Environment environment;

	@Comment("获取配置")
	public String get(@Comment(name = "key", value = "配置项") String key) {
		return environment.getProperty(key);
	}

	public EnvModule(Environment environment) {
		this.environment = environment;
	}

	@Comment("获取配置")
	public String get(@Comment(name = "key", value = "配置项") String key,
					  @Comment(name = "defaultValue", value = "未配置时的默认值") String defaultValue) {
		return environment.getProperty(key, defaultValue);
	}
}
