package org.ssssssss.magicapi.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.ssssssss.magicapi.core.config.MagicAPIProperties;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.MagicNotify;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.service.MagicAPIService;
import org.ssssssss.magicapi.core.service.MagicNotifyService;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.util.Objects;


@EnableConfigurationProperties(ClusterConfig.class)
@Configuration
public class MagicClusterConfiguration implements MagicPluginConfiguration {

	private final ClusterConfig config;

	private final MagicAPIProperties properties;

	private final StringRedisTemplate stringRedisTemplate;

	private final Logger logger = LoggerFactory.getLogger(MagicClusterConfiguration.class);

	public MagicClusterConfiguration(MagicAPIProperties properties, ClusterConfig config, ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
		this.properties = properties;
		this.config = config;
		this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
	}

	@Override
	public Plugin plugin() {
		return new Plugin("Cluster");
	}

	/**
	 * 使用Redis推送通知
	 */
	@Bean
	@ConditionalOnMissingBean
	public MagicNotifyService magicNotifyService() {
		return magicNotify -> stringRedisTemplate.convertAndSend(config.getChannel(), Objects.requireNonNull(JsonUtils.toJsonString(magicNotify)));
	}

	/**
	 * 消息处理服务
	 */
	@Bean
	@ConditionalOnMissingBean
	public MagicSynchronizationService magicSynchronizationService(MagicNotifyService magicNotifyService) {
		return new MagicSynchronizationService(magicNotifyService, properties.getInstanceId());
	}

	/**
	 * 集群通知监听
	 */
	@Bean
	public RedisMessageListenerContainer magicRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, MagicAPIService magicAPIService) {
		logger.info("开启集群通知监听， Redis channel: {}", config.getChannel());
		RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
		redisMessageListenerContainer.addMessageListener((message, pattern) -> magicAPIService.processNotify(JsonUtils.readValue(message.getBody(), MagicNotify.class)), ChannelTopic.of(config.getChannel()));
		return redisMessageListenerContainer;
	}
}
