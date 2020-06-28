package org.ssssssss.magicapi.config;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MagicApiService {

	private final String deleteById = "delete from magic_api_info where id = ?";
	private final String selectList = "select id,api_name name,api_path path,api_method method from magic_api_info order by api_update_time desc";
	private final String selectListWithScript = "select id,api_name name,api_path path,api_method method,api_script script,api_parameter parameter,api_option `option` from magic_api_info";
	private final String selectOne = "select api_method method,api_script script,api_path path,api_name name,api_parameter parameter,api_option `option` from magic_api_info where id = ?";
	private final String exists = "select count(*) from magic_api_info where api_method = ? and api_path = ?";
	private final String existsWithoutId = "select count(*) from magic_api_info where api_method = ? and api_path = ? and id !=?";
	private final String insert = "insert into magic_api_info(id,api_method,api_path,api_script,api_name,api_parameter,api_option,api_create_time,api_update_time) values(?,?,?,?,?,?,?,?,?)";
	private final String update = "update magic_api_info set api_method = ?,api_path = ?,api_script = ?,api_name = ?,api_parameter = ?,api_option = ?,api_update_time = ? where id = ?";
	private RowMapper<ApiInfo> rowMapper = new BeanPropertyRowMapper<>(ApiInfo.class);
	private JdbcTemplate template;

	public MagicApiService(JdbcTemplate template) {
		this.template = template;
	}

	protected boolean delete(String id) {
		Map<String, Object> info = template.queryForMap("select * from magic_api_info where id = ?", id);
		if (info != null) {
			return template.update(deleteById, id) > 0;
		}
		return false;
	}

	protected List<ApiInfo> list() {
		return template.query(selectList, rowMapper);
	}

	protected List<ApiInfo> listWithScript() {
		return template.query(selectListWithScript, rowMapper);
	}

	protected ApiInfo get(String id) {
		return template.queryForObject(selectOne, rowMapper, id);
	}

	protected boolean exists(String method, String path) {
		return template.queryForObject(exists, Integer.class, method, path) > 0;
	}

	protected boolean existsWithoutId(String method, String path, String id) {
		return template.queryForObject(existsWithoutId, Integer.class, method, path, id) > 0;
	}

	protected boolean insert(ApiInfo info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		long time = System.currentTimeMillis();
		return template.update(insert, info.getId(), info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getParameter(), info.getOption(), time, time) > 0;
	}

	protected boolean update(ApiInfo info) {
		return template.update(update, info.getMethod(), info.getPath(), info.getScript(), info.getName(), info.getParameter(), info.getOption(), System.currentTimeMillis(), info.getId()) > 0;
	}
}
