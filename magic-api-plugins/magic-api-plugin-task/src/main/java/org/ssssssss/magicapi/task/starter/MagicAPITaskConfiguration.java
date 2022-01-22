package org.ssssssss.magicapi.task.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.task.service.TaskInfoMagicResourceStorage;
import org.ssssssss.magicapi.task.service.TaskMagicDynamicRegistry;
import org.ssssssss.magicapi.task.web.MagicTaskController;

@Configuration
@EnableScheduling
public class MagicAPITaskConfiguration implements MagicPluginConfiguration{

	@Bean
	@ConditionalOnMissingBean
	public TaskInfoMagicResourceStorage taskInfoMagicResourceStorage() {
		return new TaskInfoMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public TaskMagicDynamicRegistry taskMagicDynamicRegistry(TaskInfoMagicResourceStorage taskInfoMagicResourceStorage, TaskScheduler taskScheduler) {
		return new TaskMagicDynamicRegistry(taskInfoMagicResourceStorage, taskScheduler);
	}

	@Override
	public Plugin plugin() {
		return new Plugin("定时任务", "magic-task.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicTaskController(configuration));
	}
}
