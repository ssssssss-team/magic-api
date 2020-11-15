package org.ssssssss.magicapi.functions;

import org.springframework.core.env.Environment;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.script.annotation.Comment;

public class EnvFunctions implements MagicModule {

	private Environment environment;

	public EnvFunctions(Environment environment) {
		this.environment = environment;
	}

	@Override
	public String getModuleName() {
		return "env";
	}

	@Comment("获取配置")
	public String get(@Comment("配置项") String key) {
		return environment.getProperty(key);
	}

	@Comment("获取配置")
	public String get(@Comment("配置项") String key, @Comment("未配置时的默认值") String defaultValue) {
		return environment.getProperty(key, defaultValue);
	}
}
