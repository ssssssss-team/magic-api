package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.ssssssss.magicapi.modules.RedisModule;

/**
 * redis配置
 */
@ConditionalOnBean(RedisConnectionFactory.class)
@Configuration
public class MagicRedisAutoConfiguration {

	/**
	 * 注入redis模块
	 */
	@Bean
	public RedisModule redisFunctions(RedisConnectionFactory connectionFactory) {
		return new RedisModule(connectionFactory);
	}
}
