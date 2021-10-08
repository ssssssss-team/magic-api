package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.provider.MagicBackupService;
import org.ssssssss.magicapi.utils.WebUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public MagicDatabaseBackupService(JdbcTemplate template, String tableName) {
		this.template = template;
		this.INSERT_SQL = String.format("insert into %s(%s,content) values(?,?,?,?,?,?,?)", tableName, DEFAULT_COLUMNS);
		this.FIND_BY_ID = String.format("select %s from %s where id = ? order by create_date desc", DEFAULT_COLUMNS, tableName);
		this.DELETE_BY_ID = String.format("delete from %s where id = ?", tableName);
		this.FIND_BY_TAG = String.format("select %s from %s where tag = ? order by create_date desc", DEFAULT_COLUMNS, tableName);
		this.FIND_BY_TIMESTAMP = String.format("select %s from %s where create_date < ? order by create_date desc", DEFAULT_COLUMNS, tableName);
		this.DELETE_BY_TIMESTAMP = String.format("delete from %s where create_date < ?", tableName);
		this.FIND_BY_ID_AND_TIMESTAMP = String.format("select * from %s where id = ? and create_date = ?", tableName);
	}

	@Override
	public void doBackup(Backup backup) {
		if (backup.getCreateDate() == 0) {
			backup.setCreateDate(System.currentTimeMillis());
		}
		if (backup.getCreateBy() == null) {
			backup.setCreateBy(WebUtils.currentUserName());
		}
		template.update(INSERT_SQL, backup.getId(), backup.getCreateDate(), backup.getTag(), backup.getType(), backup.getName(), backup.getCreateBy(), backup.getContent());
	}

	@Override
	public List<Backup> backupList(long timestamp) {
		Stream<Backup> stream = template.queryForStream(FIND_BY_TIMESTAMP, rowMapper, timestamp);
		return stream.limit(FETCH_SIZE).collect(Collectors.toList());
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
	public long removeBackup(List<String> idList) {
		return idList.stream().mapToLong(this::removeBackup).sum();
	}

	@Override
	public long removeBackupByTimestamp(long timestamp) {
		return template.update(DELETE_BY_TIMESTAMP, timestamp);
	}
}
