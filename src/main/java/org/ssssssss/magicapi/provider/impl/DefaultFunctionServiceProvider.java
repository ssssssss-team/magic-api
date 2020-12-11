package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.provider.FunctionServiceProvider;

import java.util.List;
import java.util.UUID;

public class DefaultFunctionServiceProvider extends BeanPropertyRowMapper<FunctionInfo> implements FunctionServiceProvider {

	private final String COMMON_COLUMNS = "id,\n" +
			"function_name,\n" +
			"function_group_id,\n" +
			"function_arguments,\n" +
			"function_description,\n" +
			"function_create_time,\n" +
			"function_update_time";

	private final String SCRIPT_COLUMNS = "function_script";

	private JdbcTemplate template;

	public DefaultFunctionServiceProvider(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public boolean insert(FunctionInfo info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		wrap(info);
		String insert = String.format("insert into magic_function(%s,%s) values(?,?,?,?,?,?,?,?)", COMMON_COLUMNS, SCRIPT_COLUMNS);
		long time = System.currentTimeMillis();
		return template.update(insert, info.getId(), info.getName(), info.getGroupId(), info.getArguments(), info.getDescription(), info.getScript(), time, time) > 0;
	}

	@Override
	public boolean update(FunctionInfo info) {
		wrap(info);
		String update = "update magic_function set function_name = ?,function_arguments = ?,function_script = ?,function_description = ?,function_group_id = ?,function_update_time = ? where id = ?";
		return template.update(update, info.getName(), info.getArguments(), info.getScript(), info.getDescription(), info.getGroupId(), System.currentTimeMillis(), info.getId()) > 0;
	}

	@Override
	public void backup(String apiId) {

	}

	@Override
	public List<Long> backupList(String id) {
		return null;
	}

	@Override
	public ApiInfo backupInfo(String id, Long timestamp) {
		return null;
	}

	@Override
	public boolean delete(String id) {
		return template.update("delete from magic_function where id = ?", id) > 0;
	}

	@Override
	public List<FunctionInfo> list() {
		String selectList = "select " + COMMON_COLUMNS + " from magic_api_info order by api_update_time desc";
		return template.query(selectList, this);
	}

	@Override
	public List<FunctionInfo> listWithScript() {
		return null;
	}

	@Override
	public FunctionInfo get(String id) {
		String selectOne = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_function where id = ?";
		FunctionInfo info = template.queryForObject(selectOne, this, id);
		unwrap(info);
		return info;
	}

	@Override
	public boolean move(String id, String groupId) {
		return false;
	}

	@Override
	public boolean deleteGroup(List<String> groupIds) {
		return false;
	}

	@Override
	protected String lowerCaseName(String name) {
		return super.lowerCaseName(name).replace("function_", "");
	}
}
