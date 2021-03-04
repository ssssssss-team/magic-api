package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.resource.RedisResource;
import org.ssssssss.magicapi.modules.RedisModule;

/**
 * redis配置
 */
@ConditionalOnBean(RedisConnectionFactory.class)
@Configuration
public class MagicRedisAutoConfiguration {

	private MagicAPIProperties properties;

	public MagicRedisAutoConfiguration(MagicAPIProperties properties) {
		this.properties = properties;
	}

	/**
	 * 注入redis模块
	 */
	@Bean
	public RedisModule redisFunctions(RedisConnectionFactory connectionFactory) {
		return new RedisModule(connectionFactory);
	}

	/**
	 * 使用Redis存储
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "redis")
	public Resource magicRedisResource(RedisConnectionFactory connectionFactory) {
		ResourceConfig resource = properties.getResource();
		return new RedisResource(new StringRedisTemplate(connectionFactory), resource.getPrefix(), resource.getSeparator(), resource.isReadonly());
	}
}
