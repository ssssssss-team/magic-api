package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.ssssssss.magicapi.config.ApiInfo;
import org.ssssssss.magicapi.provider.ApiServiceProvider;

import java.util.List;
import java.util.UUID;

public class DefaultApiServiceProvider implements ApiServiceProvider {

	private final String COMMON_COLUMNS = "id,\n" +
			"api_name name,\n" +
			"api_group_name group_name,\n" +
			"api_group_prefix group_prefix,\n" +
			"api_path path,\n" +
			"api_method method";

	private final String SCRIPT_COLUMNS = "api_script script,\n" +
			"api_parameter parameter,\n" +
			"api_output output,\n" +
			"api_option option_value\n";

	private RowMapper<ApiInfo> rowMapper = new BeanPropertyRowMapper<>(ApiInfo.class);

	private JdbcTemplate template;

	public DefaultApiServiceProvider(JdbcTemplate template) {
		this.template = template;
	}

	public boolean delete(String id) {
		String deleteById = "delete from magic_api_info where id = ?";
		return template.update(deleteById, id) > 0;
	}

	public boolean deleteGroup(String groupName) {
		String deleteByGroupName = "delete from magic_api_info where api_group_name = ?";
		return template.update(deleteByGroupName, groupName) > 0;
	}

	public List<ApiInfo> list() {
		String selectList = "select " + COMMON_COLUMNS + " from magic_api_info order by api_update_time desc";
		return template.query(selectList, rowMapper);
	}

	public List<ApiInfo> listWithScript() {
		String selectListWithScript = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_api_info";
		List<ApiInfo> infos = template.query(selectListWithScript, rowMapper);
		if (infos != null) {
			for (ApiInfo info : infos) {
				unwrap(info);
			}
		}
		return infos;
	}

	public ApiInfo get(String id) {
		String selectOne = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_api_info where id = ?";
		ApiInfo info = template.queryForObject(selectOne, rowMapper, id);
		unwrap(info);
		return info;
	}

	public boolean exists(String method, String path) {
		String exists = "select count(*) from magic_api_info where api_method = ? and api_path = ?";
		return template.queryForObject(exists, Integer.class, method, path) > 0;
	}

	@Override
	public boolean updateGroup(String oldGroupName, String groupName, String groupPrefix) {
		String updateGroup = "update magic_api_info set api_group_name = ?,api_group_prefix=?,api_update_time = ? where api_group_name = ?";
		return template.update(updateGroup, groupName, groupPrefix, System.currentTimeMillis(), oldGroupName) > 0;
	}

	public boolean existsWithoutId(String method, String path, String id) {
		String existsWithoutId = "select count(*) from magic_api_info where api_method = ? and api_path = ? and id !=?";
		return template.queryForObject(existsWithoutId, Integer.class, method, path, id) > 0;
	}

	public boolean insert(ApiInfo info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		wrap(info);
		long time = System.currentTimeMillis();
		String insert = "insert into magic_api_info(id,api_method,api_path,api_script,api_name,api_group_name,api_parameter,api_option,api_output,api_group_prefix,api_create_time,api_update_time) values(?,?,?,?,?,?,?,?,?,?,?,?)";
		return template.update(insert, info.getId(), info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getGroupName(), info.getParameter(), info.getOption(), info.getOutput(), info.getGroupPrefix(), time, time) > 0;
	}

	public boolean update(ApiInfo info) {
		wrap(info);
		String update = "update magic_api_info set api_method = ?,api_path = ?,api_script = ?,api_name = ?,api_group_name = ?,api_parameter = ?,api_option = ?,api_output = ?,api_group_prefix = ?,api_update_time = ? where id = ?";
		return template.update(update, info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getGroupName(), info.getParameter(), info.getOption(), info.getOutput(), info.getGroupPrefix(), System.currentTimeMillis(), info.getId()) > 0;
	}

	@Override
	public void backup(String apiId) {
		String backupSql = "insert into magic_api_info_his select * from magic_api_info where id = ?";
		template.update(backupSql, apiId);
	}

	@Override
	public List<Long> backupList(String apiId) {
		return template.queryForList("select api_update_time from magic_api_info_his where id = ? order by api_update_time desc",Long.class,apiId);
	}

	@Override
	public ApiInfo backupInfo(String apiId, Long timestamp) {
		String selectOne = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_api_info_his where id = ? and api_update_time = ? limit 1";
		ApiInfo info = template.queryForObject(selectOne, rowMapper, apiId,timestamp);
		unwrap(info);
		return info;
	}
}
