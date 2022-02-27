package org.ssssssss.magicapi.task.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.event.GroupEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.task.model.TaskInfo;
import org.ssssssss.magicapi.utils.ScriptManager;
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
	public boolean register(TaskInfo entity) {
		unregister(entity);
		return super.register(entity);
	}

	@Override
	protected boolean register(MappingNode<TaskInfo> mappingNode) {
		TaskInfo info = mappingNode.getEntity();
		if (taskScheduler != null) {
			CronTask cronTask = new CronTask(() -> {
				TaskInfo entity = mappingNode.getEntity();
				String scriptName = MagicConfiguration.getMagicResourceService().getScriptName(entity);
				if (entity.isEnabled()) {
					try {
						logger.info("定时任务:[{}]开始执行", scriptName);
						MagicScriptContext magicScriptContext = new MagicScriptContext();
						magicScriptContext.setScriptName(scriptName);
						ScriptManager.executeScript(entity.getScript(), magicScriptContext);
					} catch (Exception e) {
						logger.error("定时任务执行出错", e);
					} finally {
						logger.info("定时任务:[{}]执行完毕", scriptName);
					}
				}
			}, info.getCron());
			mappingNode.setMappingData(taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger()));
			logger.debug("注册定时任务:[{},{}]", MagicConfiguration.getMagicResourceService().getScriptName(info), info.getCron());
		}

		return true;
	}

	@Override
	protected void unregister(MappingNode<TaskInfo> mappingNode) {
		if (taskScheduler == null) {
			return;
		}
		TaskInfo info = mappingNode.getEntity();
		logger.debug("取消注册定时任务:[{}, {}, {}]", info.getName(), info.getPath(), info.getCron());
		ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) mappingNode.getMappingData();
		if (scheduledFuture != null) {
			try {
				scheduledFuture.cancel(true);
			} catch (Exception e) {
				String scriptName = MagicConfiguration.getMagicResourceService().getScriptName(info);
				logger.warn("定时任务:[{}]取消失败", scriptName, e);
			}
		}
	}
}
