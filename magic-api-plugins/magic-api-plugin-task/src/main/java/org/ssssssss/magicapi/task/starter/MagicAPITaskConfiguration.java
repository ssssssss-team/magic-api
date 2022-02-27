package org.ssssssss.magicapi.task.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.task.service.TaskInfoMagicResourceStorage;
import org.ssssssss.magicapi.task.service.TaskMagicDynamicRegistry;
import org.ssssssss.magicapi.task.web.MagicTaskController;

@Configuration
@EnableConfigurationProperties(MagicTaskConfig.class)
public class MagicAPITaskConfiguration implements MagicPluginConfiguration {

	private final MagicTaskConfig config;

	public MagicAPITaskConfiguration(MagicTaskConfig config) {
		this.config = config;
	}

	@Bean
	@ConditionalOnMissingBean
	public TaskInfoMagicResourceStorage taskInfoMagicResourceStorage() {
		return new TaskInfoMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public TaskMagicDynamicRegistry taskMagicDynamicRegistry(TaskInfoMagicResourceStorage taskInfoMagicResourceStorage) {
		MagicTaskConfig.Shutdown shutdown = config.getShutdown();
		ThreadPoolTaskScheduler poolTaskScheduler = null;
		if(config.isEnable()){
			poolTaskScheduler = new ThreadPoolTaskScheduler();
			poolTaskScheduler.setPoolSize(config.getPool().getSize());
			poolTaskScheduler.setWaitForTasksToCompleteOnShutdown(shutdown.isAwaitTermination());
			if(shutdown.getAwaitTerminationPeriod() != null){
				poolTaskScheduler.setAwaitTerminationSeconds((int) shutdown.getAwaitTerminationPeriod().getSeconds());
			}
			poolTaskScheduler.setThreadNamePrefix(config.getThreadNamePrefix());
			poolTaskScheduler.initialize();
		}
		return new TaskMagicDynamicRegistry(taskInfoMagicResourceStorage, poolTaskScheduler);
	}

	@Override
	public Plugin plugin() {
		return new Plugin("定时任务", "MagicTask", "magic-task.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicTaskController(configuration));
	}
}
