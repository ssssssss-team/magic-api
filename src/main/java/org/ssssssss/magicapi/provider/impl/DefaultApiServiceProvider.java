package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.ssssssss.magicapi.config.ApiInfo;
import org.ssssssss.magicapi.provider.ApiServiceProvider;

import java.util.List;
import java.util.UUID;

public class DefaultApiServiceProvider implements ApiServiceProvider {

	private final String deleteById = "delete from magic_api_info where id = ?";
	private final String deleteByGroupName = "delete from magic_api_info where api_group_name = ?";
	private final String selectList = "select id,api_name name,api_group_name group_name,api_path path,api_method method from magic_api_info order by api_update_time desc";
	private final String selectListWithScript = "select id,api_name name,api_group_name group_name,api_path path,api_method method,api_script script,api_parameter parameter,api_option `option` from magic_api_info";
	private final String selectOne = "select api_method method,api_script script,api_path path,api_name name,api_group_name group_name,api_parameter parameter,api_option `option` from magic_api_info where id = ?";
	private final String exists = "select count(*) from magic_api_info where api_method = ? and api_path = ?";
	private final String existsWithoutId = "select count(*) from magic_api_info where api_method = ? and api_path = ? and id !=?";
	private final String insert = "insert into magic_api_info(id,api_method,api_path,api_script,api_name,api_group_name,api_parameter,api_option,api_create_time,api_update_time) values(?,?,?,?,?,?,?,?,?,?)";
	private final String update = "update magic_api_info set api_method = ?,api_path = ?,api_script = ?,api_name = ?,api_group_name = ?,api_parameter = ?,api_option = ?,api_update_time = ? where id = ?";
	private RowMapper<ApiInfo> rowMapper = new BeanPropertyRowMapper<>(ApiInfo.class);
	private JdbcTemplate template;

	public DefaultApiServiceProvider(JdbcTemplate template) {
		this.template = template;
	}

	public boolean delete(String id) {
		return template.update(deleteById, id) > 0;
	}

	public boolean deleteGroup(String groupName) {
		return template.update(deleteByGroupName, groupName) > 0;
	}

	public List<ApiInfo> list() {
		return template.query(selectList, rowMapper);
	}

	public List<ApiInfo> listWithScript() {
		List<ApiInfo> infos = template.query(selectListWithScript, rowMapper);
		if(infos != null){
			for (ApiInfo info : infos) {
				unwrap(info);
			}
		}
		return infos;
	}

	public ApiInfo get(String id) {
		ApiInfo info = template.queryForObject(selectOne, rowMapper, id);
		unwrap(info);
		return info;
	}

	public boolean exists(String method, String path) {
		return template.queryForObject(exists, Integer.class, method, path) > 0;
	}

	public boolean existsWithoutId(String method, String path, String id) {
		return template.queryForObject(existsWithoutId, Integer.class, method, path, id) > 0;
	}

	public boolean insert(ApiInfo info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		wrap(info);
		long time = System.currentTimeMillis();
		return template.update(insert, info.getId(), info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getGroupName(), info.getParameter(), info.getOption(), time, time) > 0;
	}

	public boolean update(ApiInfo info) {
		wrap(info);
		return template.update(update, info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getGroupName(), info.getParameter(), info.getOption(), System.currentTimeMillis(), info.getId()) > 0;
	}
}
