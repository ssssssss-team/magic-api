package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.provider.ApiServiceProvider;

import java.util.List;
import java.util.UUID;

public class DefaultApiServiceProvider extends BeanPropertyRowMapper<ApiInfo> implements ApiServiceProvider {

	private final String COMMON_COLUMNS = "id,\n" +
			"api_name,\n" +
			"api_group_id,\n" +
			"api_path,\n" +
			"api_description,\n" +
			"api_method";

	private final String SCRIPT_COLUMNS = "api_script,\n" +
			"api_parameter,\n" +
			"api_request_header,\n" +
			"api_request_body,\n" +
			"api_response_body,\n" +
			"api_response_header,\n" +
			"api_option\n";

	private JdbcTemplate template;

	public DefaultApiServiceProvider(JdbcTemplate template) {
		super(ApiInfo.class);
		this.template = template;
	}

	public boolean delete(String id) {
		String deleteById = "delete from magic_api_info where id = ?";
		return template.update(deleteById, id) > 0;
	}

	public List<ApiInfo> list() {
		String selectList = "select " + COMMON_COLUMNS + " from magic_api_info order by api_update_time desc";
		return template.query(selectList, this);
	}

	public List<ApiInfo> listWithScript() {
		String selectListWithScript = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_api_info";
		List<ApiInfo> infos = template.query(selectListWithScript, this);
		if (infos != null) {
			for (ApiInfo info : infos) {
				unwrap(info);
			}
		}
		return infos;
	}

	public ApiInfo get(String id) {
		String selectOne = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_api_info where id = ?";
		ApiInfo info = template.queryForObject(selectOne, this, id);
		unwrap(info);
		return info;
	}

	@Override
	public boolean move(String id, String groupId) {
		return template.update("update magic_api_info SET api_group_id = ? where id = ?", groupId, id) > 0;
	}

	@Override
	public boolean deleteGroup(String groupId) {
		return template.update("delete from magic_api_info where api_group_id = ?", groupId) > 0;
	}

	public boolean exists(String groupId, String method, String path) {
		String exists = "select count(*) from magic_api_info where api_method = ? and api_path = ? and api_group_id = ?";
		return template.queryForObject(exists, Integer.class, method, path, groupId) > 0;
	}

	public boolean existsWithoutId(String groupId, String method, String path, String id) {
		String existsWithoutId = "select count(*) from magic_api_info where api_method = ? and api_path = ? and api_group_id = ? and id !=?";
		return template.queryForObject(existsWithoutId, Integer.class, method, path, groupId, id) > 0;
	}

	public boolean insert(ApiInfo info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		wrap(info);
		long time = System.currentTimeMillis();
		String insert = "insert into magic_api_info(id,api_method,api_path,api_script,api_name,api_group_id,api_parameter,api_description,api_option,api_request_header,api_request_body,api_response_body,api_response_header,api_create_time,api_update_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return template.update(insert, info.getId(), info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getGroupId(), info.getParameter(), info.getDescription(), info.getOption(), info.getRequestHeader(), info.getRequestBody(),info.getResponseBody(), info.getResponseHeader(), time, time) > 0;
	}

	public boolean update(ApiInfo info) {
		wrap(info);
		String update = "update magic_api_info set api_method = ?,api_path = ?,api_script = ?,api_name = ?,api_group_id = ?,api_description = ?,api_parameter = ?,api_option = ?,api_request_header = ?,api_request_body = ?,api_response_body = ?,api_response_header = ?,api_update_time = ? where id = ?";
		return template.update(update, info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getGroupId(), info.getDescription(), info.getParameter(), info.getOption(), info.getRequestHeader(), info.getRequestBody(), info.getResponseBody(), info.getResponseHeader(), System.currentTimeMillis(), info.getId()) > 0;
	}

	@Override
	public void backup(String apiId) {
		String backupSql = "insert into magic_api_info_his select * from magic_api_info where id = ?";
		template.update(backupSql, apiId);
	}


	@Override
	public List<Long> backupList(String apiId) {
		return template.queryForList("select api_update_time from magic_api_info_his where id = ? order by api_update_time desc", Long.class, apiId);
	}

	@Override
	public ApiInfo backupInfo(String apiId, Long timestamp) {
		String selectOne = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_api_info_his where id = ? and api_update_time = ?";
		List<ApiInfo> list = template.query(selectOne, this, apiId, timestamp);
		if (list != null && !list.isEmpty()) {
			ApiInfo info = list.get(0);
			unwrap(info);
			return info;
		}
		return null;
	}

	@Override
	protected String lowerCaseName(String name) {
		return super.lowerCaseName(name).replace("api_", "");
	}
}
