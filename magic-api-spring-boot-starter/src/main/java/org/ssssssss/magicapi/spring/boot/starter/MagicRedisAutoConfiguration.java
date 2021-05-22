package org.ssssssss.magicapi.spring.boot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.adapter.resource.RedisResource;
import org.ssssssss.magicapi.model.MagicNotify;
import org.ssssssss.magicapi.modules.RedisModule;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.MagicNotifyService;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.util.Objects;

/**
 * redis配置
 */
@ConditionalOnBean(RedisConnectionFactory.class)
@Configuration
@AutoConfigureBefore(MagicAPIAutoConfiguration.class)
public class MagicRedisAutoConfiguration {

	private final static Logger logger = LoggerFactory.getLogger(MagicRedisAutoConfiguration.class);

	private final MagicAPIProperties properties;

	private final StringRedisTemplate stringRedisTemplate;

	public MagicRedisAutoConfiguration(MagicAPIProperties properties, ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
		this.properties = properties;
		this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
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
		return new RedisResource(new StringRedisTemplate(connectionFactory), resource.getPrefix(), resource.isReadonly());
	}

	/**
	 * 使用Redis推送通知
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "magic-api", name = "cluster-config.enable", havingValue = "true")
	public MagicNotifyService magicNotifyService() {
		return magicNotify -> stringRedisTemplate.convertAndSend(properties.getClusterConfig().getChannel(), Objects.requireNonNull(JsonUtils.toJsonString(magicNotify)));
	}

	/**
	 * 集群通知监听
	 */
	@Bean
	@ConditionalOnProperty(prefix = "magic-api", name = "cluster-config.enable", havingValue = "true")
	public RedisMessageListenerContainer magicRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, MagicAPIService magicAPIService) {
		ClusterConfig config = properties.getClusterConfig();
		logger.info("开启集群通知监听， Redis channel: {}", config.getChannel());
		RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
		redisMessageListenerContainer.addMessageListener((message, pattern) -> magicAPIService.processNotify(JsonUtils.readValue(message.getBody(), MagicNotify.class)), ChannelTopic.of(config.getChannel()));
		return redisMessageListenerContainer;
	}
}
