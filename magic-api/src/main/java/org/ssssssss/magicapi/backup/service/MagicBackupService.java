package org.ssssssss.magicapi.backup.service;

import org.ssssssss.magicapi.backup.model.Backup;

import java.io.IOException;
import java.util.List;

/**
 * 数据备份接口
 *
 * @author mxd
 */
public interface MagicBackupService {

	int FETCH_SIZE = 100;

	/**
	 * 执行备份动作
	 *
	 * @param backup 备份对象
	 */
	void doBackup(Backup backup);

	void doBackupAll(String name, String createBy) throws IOException;

	/**
	 * 根据时间戳查询最近的 FETCH_SIZE 条记录
	 *
	 * @param timestamp 时间戳
	 * @return 返回备份记录
	 */
	List<Backup> backupList(long timestamp);

	/**
	 * 根据对象ID查询备份记录
	 *
	 * @param id 对象ID
	 * @return 返回备份记录
	 */
	List<Backup> backupById(String id);

	/**
	 * 根据对象ID和备份时间查询
	 */
	Backup backupInfo(String id, long timestamp);

	/**
	 * 根据标签查询备份记录
	 *
	 * @param tag 标签
	 * @return 返回备份记录
	 */
	List<Backup> backupByTag(String tag);

	/**
	 * 删除备份
	 *
	 * @param id 对象ID
	 * @return 返回删除的记录数
	 */
	long removeBackup(String id);

	/**
	 * 根据13位时间戳删除备份记录（清除小于该值的记录）
	 *
	 * @param timestamp 时间戳
	 * @return 返回删除的记录数
	 */
	long removeBackupByTimestamp(long timestamp);


}
