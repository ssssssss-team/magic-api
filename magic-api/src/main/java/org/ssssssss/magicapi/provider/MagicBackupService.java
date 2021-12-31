package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.Backup;
import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.model.FunctionInfo;

import java.util.List;

/**
 * 数据备份接口
 *
 * @author mxd
 */
public interface MagicBackupService {

	int FETCH_SIZE = 100;

	/**
	 * 备份接口
	 *
	 * @param apiInfo 接口信息
	 */
	default void backup(ApiInfo apiInfo) {
//		doBackup(new Backup(apiInfo.getId(), Constants.PATH_API, apiInfo.getName(), JsonUtils.toJsonString(apiInfo)));
	}

	/**
	 * 备份函数
	 *
	 * @param functionInfo 函数信息
	 */
	default void backup(FunctionInfo functionInfo) {
//		doBackup(new Backup(functionInfo.getId(), Constants.PATH_FUNCTION, functionInfo.getName(), JsonUtils.toJsonString(functionInfo)));
	}

	/**
	 * 备份数据源
	 *
	 * @param dataSourceInfo 数据源信息
	 */
	default void backup(DataSourceInfo dataSourceInfo) {
		// doBackup(new Backup(dataSourceInfo.getId(), Constants.PATH_DATASOURCE, dataSourceInfo.get("name"), JsonUtils.toJsonString(dataSourceInfo)));
	}

	/**
	 * 执行备份动作
	 *
	 * @param backup 备份对象
	 */
	void doBackup(Backup backup);

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
	 * 删除一组备份信息
	 *
	 * @param idList 对象ID集合
	 * @return 返回删除的记录数
	 */
	long removeBackup(List<String> idList);

	/**
	 * 根据13位时间戳删除备份记录（清除小于该值的记录）
	 *
	 * @param timestamp 时间戳
	 * @return 返回删除的记录数
	 */
	long removeBackupByTimestamp(long timestamp);


}
