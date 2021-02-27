package org.ssssssss.magicapi.adapter.resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.adapter.Resource;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatabaseResource extends KeyValueResource {

	private final JdbcTemplate template;

	private final String tableName;

	public DatabaseResource(JdbcTemplate template, String tableName, String separator, String path, KeyValueResource parent) {
		super(separator, path, parent);
		this.template = template;
		this.tableName = tableName;
	}

	public DatabaseResource(JdbcTemplate template, String tableName, String separator, String path) {
		this(template, tableName, separator, path, null);
	}

	public DatabaseResource(JdbcTemplate template, String tableName, String path) {
		this(template, tableName, "/", path);
	}

	public DatabaseResource(JdbcTemplate template, String tableName) {
		this(template, tableName, "/magic-api");
	}

	@Override
	public byte[] read() {
		String sql = String.format("select file_content from %s where file_path = ?", tableName);
		String value = template.queryForObject(sql, String.class, this.path);
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public boolean exists() {
		String sql = String.format("select count(*) from %s where file_path = ?", tableName);
		Long value = template.queryForObject(sql, Long.class, this.path);
		return value != null && value > 0;
	}

	@Override
	public boolean write(String content) {
		String sql = String.format("update %s set file_content = ? where file_path = ?", tableName);
		if(exists()){
			return template.update(sql, content,this.path) >= 0;
		}
		sql = String.format("insert into %s (file_path,file_content) values(?,?)", tableName);
		return template.update(sql, this.path, content) > 0;
	}

	@Override
	public Resource getResource(String name) {
		return new DatabaseResource(template, tableName, separator, (isDirectory() ? this.path : this.path + separator) + name, this);
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
		return Arrays.stream(template.batchUpdate(sql,args)).sum() > 0;
	}

	@Override
	public boolean delete() {
		String sql = String.format("delete from %s where file_path = ? or file_path like '%s%%'",tableName, isDirectory() ? this.path : this.path + separator);
		return template.update(sql,this.path) > 0;
	}

	@Override
	public Function<String, Resource> mappedFunction() {
		return it -> new DatabaseResource(template, tableName, separator, it, this);
	}

	@Override
	public String toString() {
		return String.format("db://%s/%s", tableName, Objects.toString(this.path, ""));
	}

}
