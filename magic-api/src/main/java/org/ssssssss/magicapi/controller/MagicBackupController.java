package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.MagicBackupService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MagicBackupController extends MagicController {

	private final MagicBackupService service;

	public MagicBackupController(MagicConfiguration configuration) {
		super(configuration);
		this.service = configuration.getMagicBackupService();
	}

	@GetMapping("/backups")
	@ResponseBody
	public JsonBean<List<Backup>> backups(Long timestamp) {
		return new JsonBean<>(service.backupList(timestamp == null ? System.currentTimeMillis() : timestamp));
	}

	@PostMapping("/backup")
	@ResponseBody
	public JsonBean<Boolean> doBackup(String tag) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MagicConfiguration.getMagicResourceService().export(null, null, baos);
		Backup backup = new Backup();
		backup.setId("full");
		backup.setType("full");
		backup.setName("全量备份");
		backup.setContent(baos.toByteArray());
		backup.setTag(tag);
		service.doBackup(backup);
		return new JsonBean<>(true);
	}
}
