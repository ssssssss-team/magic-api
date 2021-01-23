package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.model.SynchronizeRequest;
import org.ssssssss.magicapi.provider.FunctionServiceProvider;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultFunctionServiceProvider extends BeanPropertyRowMapper<FunctionInfo> implements FunctionServiceProvider {

	private final String COMMON_COLUMNS = "id,\n" +
			"function_name,\n" +
			"function_group_id,\n" +
			"function_path,\n" +
			"function_parameter,\n" +
			"function_return_type,\n" +
			"function_description,\n" +
			"function_create_time,\n" +
			"function_update_time";

	private final String SCRIPT_COLUMNS = "function_script";

	private JdbcTemplate template;

	public DefaultFunctionServiceProvider(JdbcTemplate template) {
		super(FunctionInfo.class);
		this.template = template;
	}

	@Override
	public boolean insert(FunctionInfo info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		wrap(info);
		String insert = String.format("insert into magic_function(%s,%s) values(?,?,?,?,?,?,?,?,?,?)", COMMON_COLUMNS, SCRIPT_COLUMNS);
		long time = System.currentTimeMillis();
		return template.update(insert, info.getId(), info.getName(), info.getGroupId(), info.getPath(), info.getParameter(), info.getReturnType(), info.getDescription(), time, time, info.getScript()) > 0;
	}

	@Override
	public boolean update(FunctionInfo info) {
		wrap(info);
		String update = "update magic_function set function_name = ?,function_parameter = ?,function_return_type = ?, function_script = ?,function_description = ?,function_path = ?,function_group_id = ?,function_update_time = ? where id = ?";
		return template.update(update, info.getName(), info.getParameter(), info.getReturnType(), info.getScript(), info.getDescription(), info.getPath(), info.getGroupId(), System.currentTimeMillis(), info.getId()) > 0;
	}

	@Override
	public void backup(String functionId) {
		String backupSql = "insert into magic_function_his select * from magic_function where id = ?";
		template.update(backupSql, functionId);
	}

	@Override
	public List<Long> backupList(String id) {
		return template.queryForList("select function_update_time from magic_function_his where id = ? order by function_update_time desc", Long.class, id);
	}

	@Override
	public FunctionInfo backupInfo(String id, Long timestamp) {
		String selectOne = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_function_his where id = ? and function_update_time = ?";
		List<FunctionInfo> list = template.query(selectOne, this, id, timestamp);
		if (list != null && !list.isEmpty()) {
			FunctionInfo info = list.get(0);
			unwrap(info);
			return info;
		}
		return null;
	}

	@Override
	public boolean delete(String id) {
		return template.update("delete from magic_function where id = ?", id) > 0;
	}

	@Override
	public List<FunctionInfo> list() {
		String selectList = "select " + COMMON_COLUMNS + " from magic_function order by function_update_time desc";
		return template.query(selectList, this);
	}

	@Override
	public List<FunctionInfo> listWithScript() {
		String selectListWithScript = "select " + COMMON_COLUMNS + "," + SCRIPT_COLUMNS + " from magic_function";
		List<FunctionInfo> infos = template.query(selectListWithScript, this);
		for (FunctionInfo info : infos) {
			unwrap(info);
		}
		return infos;
	}

	@Override
	public List<SynchronizeRequest.Info> listForSync(String groupId, String id) {
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
		return template.update("update magic_function SET function_group_id = ? where id = ?", groupId, id) > 0;
	}

	@Override
	public boolean deleteGroup(List<String> groupIds) {
		List<Object[]> params = groupIds.stream().map(groupId -> new Object[]{groupId}).collect(Collectors.toList());
		return Arrays.stream(template.batchUpdate("delete from magic_function where function_group_id = ?", params)).sum() >= 0;
	}

	@Override
	protected String lowerCaseName(String name) {
		return super.lowerCaseName(name).replace("function_", "");
	}

	@Override
	public boolean exists(String path, String groupId) {
		return template.queryForObject("select count(*) from magic_function where function_group_id = ? and function_path = ?", Integer.class, groupId, path) > 0;
	}

	@Override
	public boolean existsWithoutId(String path, String groupId, String id) {
		return template.queryForObject("select count(*) from magic_function where function_group_id = ? and function_path = ? and id != ?", Integer.class, groupId, path, id) > 0;
	}

}
