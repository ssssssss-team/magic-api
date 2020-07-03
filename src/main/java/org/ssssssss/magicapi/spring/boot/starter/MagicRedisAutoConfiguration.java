package org.ssssssss.magicapi.spring.boot.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.ssssssss.magicapi.functions.RedisFunctions;

@ConditionalOnBean(RedisConnectionFactory.class)
@Configuration
public class MagicRedisAutoConfiguration {

	@Bean
	public RedisFunctions redisFunctions(RedisConnectionFactory connectionFactory) {
		return new RedisFunctions(connectionFactory);
	}
}
