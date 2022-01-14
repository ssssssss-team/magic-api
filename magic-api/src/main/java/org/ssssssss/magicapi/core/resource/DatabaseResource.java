package org.ssssssss.magicapi.core.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.ssssssss.magicapi.utils.Assert;
import org.ssssssss.magicapi.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据库资源存储
 *
 * @author mxd
 */
public class DatabaseResource extends KeyValueResource {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseResource.class);
	private final JdbcTemplate template;
	private final String tableName;
	private Map<String, String> cachedContent = new ConcurrentHashMap<>();

	public DatabaseResource(JdbcTemplate template, String tableName) {
		this(template, tableName, false);
	}

	public DatabaseResource(JdbcTemplate template, String tableName, boolean readonly) {
		this(template, tableName, "/magic-api", readonly);
	}

	public DatabaseResource(JdbcTemplate template, String tableName, String path, boolean readonly) {
		this(template, tableName, path, readonly, null);
	}

	public DatabaseResource(JdbcTemplate template, String tableName, String path, boolean readonly, KeyValueResource parent) {
		super("/", path, readonly, parent);
		this.template = template;
		this.tableName = tableName;
	}

	public DatabaseResource(JdbcTemplate template, String tableName, String path, boolean readonly, Map<String, String> cachedContent, KeyValueResource parent) {
		this(template, tableName, path, readonly, parent);
		this.cachedContent = cachedContent;
	}

	@Override
	public byte[] read() {
		String value = this.cachedContent.get(path);
		if (value == null) {
			String sql = String.format("select file_content from %s where file_path = ?", tableName);
			value = template.queryForObject(sql, String.class, this.path);
			if (value != null) {
				this.cachedContent.put(path, value);
			}
		}
		return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void readAll() {
		this.cachedContent.entrySet().removeIf(entry -> entry.getKey().startsWith(path));
		String sql = String.format("select file_path, file_content from %s where file_path like '%s%%'", tableName, this.path);
		SqlRowSet sqlRowSet = template.queryForRowSet(sql);
		while (sqlRowSet.next()) {
			Object object = sqlRowSet.getObject(2);
			String content = null;
			if (object instanceof String) {
				content = object.toString();
			} else if (object instanceof byte[]) {
				content = new String((byte[]) object, StandardCharsets.UTF_8);
			} else if (object instanceof Blob) {
				Blob blob = (Blob) object;
				try (InputStream is = blob.getBinaryStream()) {
					content = new String(IoUtils.bytes(is), StandardCharsets.UTF_8);
				} catch (SQLException | IOException ex) {
					logger.error("读取content失败", ex);
				}
			} else if (object instanceof Clob) {
				Clob clob = (Clob) object;
				try {
					content = clob.getSubString(1, (int) clob.length());
				} catch (SQLException ex) {
					logger.error("读取content失败", ex);
				}
			}
			Assert.isNotNull(content, "读取content失败，请检查列类型是否正确");
			this.cachedContent.put(sqlRowSet.getString(1), content);
		}
	}

	@Override
	public boolean exists() {
		if (this.cachedContent.get(this.path) != null) {
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
			if (template.update(sql, content, this.path) > 0) {
				this.cachedContent.put(this.path, content);
				return true;
			}
		}
		sql = String.format("insert into %s (file_path,file_content) values(?,?)", tableName);
		if (template.update(sql, this.path, content) > 0) {
			this.cachedContent.put(this.path, content);
			return true;
		}
		return false;
	}

	@Override
	public Set<String> keys() {
		String prefix = isDirectory() ? this.path : (this.path + separator);
		if (!cachedContent.isEmpty()) {
			return cachedContent.keySet().stream().filter(it -> it.startsWith(prefix)).collect(Collectors.toSet());
		}
		String sql = String.format("select file_path from %s where file_path like '%s%%'", tableName, prefix);
		return new HashSet<>(template.queryForList(sql, String.class));
	}

	@Override
	public boolean renameTo(Map<String, String> renameKeys) {
		List<Object[]> args = renameKeys.entrySet().stream().map(entry -> new Object[]{entry.getValue(), entry.getKey()}).collect(Collectors.toList());
		String sql = String.format("update %s set file_path = ? where file_path = ?", tableName);
		if (Arrays.stream(template.batchUpdate(sql, args)).sum() > 0) {
			renameKeys.forEach((oldKey, newKey) -> this.cachedContent.put(newKey, this.cachedContent.remove(oldKey)));
			return true;
		}
		return false;
	}

	@Override
	public boolean delete() {
		String sql = String.format("delete from %s where file_path = ? or file_path like '%s%%'", tableName, path);
		if (template.update(sql, this.path) > 0) {
			this.cachedContent.entrySet().removeIf(entry -> entry.getKey().startsWith(path));
			return true;
		}
		return false;
	}

	@Override
	public Function<String, Resource> mappedFunction() {
		return it -> new DatabaseResource(template, tableName, it, readonly, this.cachedContent, this);
	}

	@Override
	public String toString() {
		return String.format("db://%s/%s", tableName, Objects.toString(this.path, ""));
	}

}
