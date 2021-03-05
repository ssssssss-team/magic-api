package org.ssssssss.magicapi.adapter.resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.ssssssss.magicapi.adapter.Resource;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatabaseResource extends KeyValueResource {

	private final JdbcTemplate template;

	private final String tableName;

	private Map<String,String> cachedContent = new ConcurrentHashMap<>();

	public DatabaseResource(JdbcTemplate template, String tableName, String separator, String path, boolean readonly, KeyValueResource parent) {
		super(separator, path, readonly, parent);
		this.template = template;
		this.tableName = tableName;
	}

	public DatabaseResource(JdbcTemplate template, String tableName, String separator, String path, boolean readonly, Map<String,String> cachedContent, KeyValueResource parent) {
		this(template, tableName, separator, path, readonly, parent);
		this.cachedContent = cachedContent;
	}

	@Override
	public byte[] read() {
		String value = this.cachedContent.get(path);
		if(value == null){
			String sql = String.format("select file_content from %s where file_path = ?", tableName);
			value = template.queryForObject(sql, String.class, this.path);
		}
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void readAll() {
		String sql = String.format("select file_path, file_content from %s where file_path like '%s%%'", tableName, this.path);
		SqlRowSet sqlRowSet = template.queryForRowSet(sql);
		while (sqlRowSet.next()){
			this.cachedContent.put(sqlRowSet.getString(1),sqlRowSet.getString(2));
		}
	}

	@Override
	public boolean exists() {
		if(this.cachedContent.containsKey(this.path)){
			return true;
		}
		String sql = String.format("select count(*) from %s where file_path = ?", tableName);
		Long value = template.queryForObject(sql, Long.class, this.path);
		return value != null && value > 0;
	}

	@Override
	public boolean write(String content) {
		String sql = String.format("update %s set file_content = ? where file_path = ?", tableName);
		if (exists()) {
			return template.update(sql, content, this.path) >= 0;
		}
		sql = String.format("insert into %s (file_path,file_content) values(?,?)", tableName);
		return template.update(sql, this.path, content) > 0;
	}

	@Override
	public Set<String> keys() {
		String sql = String.format("select file_path from %s where file_path like '%s%%'", tableName, isDirectory() ? this.path : (this.path + separator));
		return new HashSet<>(template.queryForList(sql, String.class));
	}

	@Override
	public boolean renameTo(Map<String, String> renameKeys) {
		List<Object[]> args = renameKeys.entrySet().stream().map(entry -> new Object[]{entry.getValue(), entry.getKey()}).collect(Collectors.toList());
		String sql = String.format("update %s set file_path = ? where file_path = ?", tableName);
		return Arrays.stream(template.batchUpdate(sql, args)).sum() > 0;
	}

	@Override
	public boolean delete() {
		String sql = String.format("delete from %s where file_path = ? or file_path like '%s%%'", tableName, isDirectory() ? this.path : this.path + separator);
		return template.update(sql, this.path) > 0;
	}

	@Override
	public Function<String, Resource> mappedFunction() {
		return it -> new DatabaseResource(template, tableName, separator, it, readonly, this.cachedContent,this);
	}

	@Override
	public String toString() {
		return String.format("db://%s/%s", tableName, Objects.toString(this.path, ""));
	}

}
