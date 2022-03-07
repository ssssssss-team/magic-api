package org.ssssssss.magicapi.backup.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.backup.model.Backup;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.backup.service.MagicBackupService;
import org.ssssssss.magicapi.core.service.MagicDynamicRegistry;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.WebUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MagicBackupController extends MagicController implements MagicExceptionHandler {

	private final MagicBackupService service;

	public MagicBackupController(MagicConfiguration configuration) {
		super(configuration);
		this.service = configuration.getMagicBackupService();
	}

	@GetMapping("/backups")
	@ResponseBody
	public JsonBean<List<Backup>> backups(Long timestamp) {
		if(service == null){
			return new JsonBean<>(Collections.emptyList());
		}
		return new JsonBean<>(service.backupList(timestamp == null ? System.currentTimeMillis() : timestamp));
	}

	@GetMapping("/backup/{id}")
	@ResponseBody
	public JsonBean<List<Backup>> backups(@PathVariable("id") String id) {
		if(service == null || StringUtils.isBlank(id)){
			return new JsonBean<>(Collections.emptyList());
		}
		return new JsonBean<>(service.backupById(id));
	}

	@GetMapping("/backup/rollback")
	@ResponseBody
	public JsonBean<Boolean> rollback(String id, Long timestamp) throws IOException {
		notNull(service, BACKUP_NOT_ENABLED);
		Backup backup = service.backupInfo(id, timestamp);
		if("full".equals(id)){
			service.doBackupAll("还原全量备份前，系统自动全量备份", WebUtils.currentUserName());
			configuration.getMagicAPIService().upload(new ByteArrayInputStream(backup.getContent()), Constants.UPLOAD_MODE_FULL);
			return new JsonBean<>(true);
		}
		if(backup.getType().endsWith("-group")){
			Group group = JsonUtils.readValue(backup.getContent(), Group.class);
			return new JsonBean<>(MagicConfiguration.getMagicResourceService().saveGroup(group));
		}
		MagicEntity entity = configuration.getMagicDynamicRegistries().stream()
				.map(MagicDynamicRegistry::getMagicResourceStorage)
				.filter(it -> it.folder().equals(backup.getType()))
				.map(it -> it.read(backup.getContent()))
				.findFirst()
				.orElse(null);
		if(entity != null){
			return new JsonBean<>(MagicConfiguration.getMagicResourceService().saveFile(entity));
		}
		return new JsonBean<>(false);
	}

	@GetMapping("/backup")
	@ResponseBody
	public JsonBean<String> backup(Long timestamp, String id) {
		notNull(service, BACKUP_NOT_ENABLED);
		notBlank(id, PARAMETER_INVALID);
		notNull(timestamp, PARAMETER_INVALID);
		Backup backup = service.backupInfo(id, timestamp);
		MagicEntity entity = JsonUtils.readValue(backup.getContent(), MagicEntity.class);
		return new JsonBean<>(entity == null ? null : entity.getScript());
	}

	@PostMapping("/backup/full")
	@ResponseBody
	public JsonBean<Boolean> doBackup() throws IOException {
		notNull(service, BACKUP_NOT_ENABLED);
		service.doBackupAll("主动全量备份", WebUtils.currentUserName());
		return new JsonBean<>(true);
	}
}
