package org.ssssssss.magicapi.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.ssssssss.magicapi.event.FileEvent;
import org.ssssssss.magicapi.event.GroupEvent;
import org.ssssssss.magicapi.model.TaskInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.script.ScriptManager;
import org.ssssssss.magicapi.service.AbstractMagicDynamicRegistry;
import org.ssssssss.script.MagicScriptContext;

import java.util.concurrent.ScheduledFuture;

public class TaskMagicDynamicRegistry extends AbstractMagicDynamicRegistry<TaskInfo> {

	private final TaskScheduler taskScheduler;

	private static final Logger logger = LoggerFactory.getLogger(TaskMagicDynamicRegistry.class);

	public TaskMagicDynamicRegistry(MagicResourceStorage<TaskInfo> magicResourceStorage, TaskScheduler taskScheduler) {
		super(magicResourceStorage);
		this.taskScheduler = taskScheduler;
	}

	@EventListener(condition = "#event.type == 'task'")
	public void onFileEvent(FileEvent event) {
		processEvent(event);
	}

	@EventListener(condition = "#event.type == 'task'")
	public void onGroupEvent(GroupEvent event) {
		processEvent(event);
	}

	@Override
	protected boolean register(MappingNode<TaskInfo> mappingNode) {
		TaskInfo info = mappingNode.getEntity();
		CronTask cronTask = new CronTask(() -> {
			try {
				ScriptManager.executeScript(info.getScript(), new MagicScriptContext());
			} catch (Exception e) {
				logger.error("定时任务执行出错", e);
			}
		}, info.getCron());
		mappingNode.setMappingData(taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger()));
		if(taskScheduler != null){
			logger.debug("注册定时任务:[{}, {}, {}]", info.getName(), info.getPath(), info.getCron());
		} else {
			logger.debug("注册定时任务失败:[{}, {}, {}]， 当前 TaskScheduler 为空", info.getName(), info.getPath(), info.getCron());
		}

		return true;
	}

	@Override
	protected void unregister(MappingNode<TaskInfo> mappingNode) {
		TaskInfo info = mappingNode.getEntity();
		logger.debug("取消注册定时任务:[{}, {}, {}]", info.getName(), info.getPath(), info.getCron());
		ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) mappingNode.getMappingData();
		if(scheduledFuture != null){
			try {
				scheduledFuture.cancel(true);
			} catch (Exception ignored) {
			}
		}
	}
}
