package org.ssssssss.magicapi.backup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.event.GroupEvent;
import org.ssssssss.magicapi.backup.model.Backup;
import org.ssssssss.magicapi.core.model.Group;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.WebUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 数据库备份实现
 *
 * @author mxd
 */
public class MagicDatabaseBackupService implements MagicBackupService {

	private final static String DEFAULT_COLUMNS = "id,create_date,tag,type,name,create_by";

	private final JdbcTemplate template;

	private final String INSERT_SQL;

	private final String FIND_BY_ID;

	private final String FIND_BY_TAG;

	private final String FIND_BY_TIMESTAMP;

	private final String FIND_BY_ID_AND_TIMESTAMP;

	private final String DELETE_BY_ID;

	private final String DELETE_BY_TIMESTAMP;

	private final BeanPropertyRowMapper<Backup> rowMapper = new BeanPropertyRowMapper<>(Backup.class);

	private static final Logger logger = LoggerFactory.getLogger(MagicDatabaseBackupService.class);

	public MagicDatabaseBackupService(JdbcTemplate template, String tableName) {
		this.template = template;
		this.template.setMaxRows(FETCH_SIZE);
		this.INSERT_SQL = String.format("insert into %s(%s,content) values(?,?,?,?,?,?,?)", tableName, DEFAULT_COLUMNS);
		this.FIND_BY_ID = String.format("select %s from %s where id = ? order by create_date desc", DEFAULT_COLUMNS, tableName);
		this.DELETE_BY_ID = String.format("delete from %s where id = ?", tableName);
		this.FIND_BY_TAG = String.format("select %s from %s where tag = ? order by create_date desc", DEFAULT_COLUMNS, tableName);
		this.FIND_BY_TIMESTAMP = String.format("select %s from %s where create_date < ? order by create_date desc", DEFAULT_COLUMNS, tableName);
		this.DELETE_BY_TIMESTAMP = String.format("delete from %s where create_date < ?", tableName);
		this.FIND_BY_ID_AND_TIMESTAMP = String.format("select * from %s where id = ? and create_date = ?", tableName);
	}

	@Override
	public void doBackupAll(String name, String createBy) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MagicConfiguration.getMagicResourceService().export(null, null, baos);
		Backup backup = new Backup();
		backup.setId("full");
		backup.setType("full");
		backup.setName(name);
		backup.setCreateBy(createBy);
		backup.setContent(baos.toByteArray());
		doBackup(backup);
	}

	@Override
	public void doBackup(Backup backup) {
		try {
			if (backup.getCreateDate() == 0) {
				backup.setCreateDate(System.currentTimeMillis());
			}
			if (backup.getCreateBy() == null) {
				backup.setCreateBy(WebUtils.currentUserName());
			}
			template.update(INSERT_SQL, backup.getId(), backup.getCreateDate(), backup.getTag(), backup.getType(), backup.getName(), backup.getCreateBy(), backup.getContent());
		} catch (Exception e) {
			logger.warn("备份失败", e);
		}
	}

	@Override
	public List<Backup> backupList(long timestamp) {
		return template.query(FIND_BY_TIMESTAMP, rowMapper, timestamp);
	}

	@Override
	public List<Backup> backupById(String id) {
		return template.query(FIND_BY_ID, rowMapper, id);
	}

	@Override
	public Backup backupInfo(String id, long timestamp) {
		return template.queryForObject(FIND_BY_ID_AND_TIMESTAMP, rowMapper, id, timestamp);
	}

	@Override
	public List<Backup> backupByTag(String tag) {
		return template.query(FIND_BY_TAG, rowMapper, tag);
	}

	@Override
	public long removeBackup(String id) {
		return template.update(DELETE_BY_ID, id);
	}

	@Override
	public long removeBackupByTimestamp(long timestamp) {
		try {
			return template.update(DELETE_BY_TIMESTAMP, timestamp);
		} catch (Exception e) {
			logger.warn("删除备份失败", e);
			return -1;
		}
	}

	@EventListener(condition = "#event.source != T(org.ssssssss.magicapi.core.config.Constants).EVENT_SOURCE_NOTIFY")
	public void onFileEvent(FileEvent event) {
		switch (event.getAction()) {
			case SAVE:
			case CREATE:
			case MOVE:
				break;
			default:
				return;
		}
		MagicEntity entity = event.getEntity();
		doBackup(entity.getId(), JsonUtils.toJsonBytes(entity), entity.getName(), event.getType());
	}

	@EventListener(condition = "#event.source != T(org.ssssssss.magicapi.core.config.Constants).EVENT_SOURCE_NOTIFY")
	public void onFolderEvent(GroupEvent event) {
		switch (event.getAction()) {
			case SAVE:
			case CREATE:
			case MOVE:
				break;
			default:
				return;
		}
		Group group = event.getGroup();
		doBackup(group.getId(), JsonUtils.toJsonBytes(group), group.getName(), group.getType() + "-group");
	}

	private void doBackup(String id, byte[] content, String name, String type) {
		Backup backup = new Backup();
		backup.setName(name);
		backup.setId(id);
		backup.setContent(content);
		backup.setType(type);
		doBackup(backup);
	}
}
