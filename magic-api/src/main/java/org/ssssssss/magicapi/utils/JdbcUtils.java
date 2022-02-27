package org.ssssssss.magicapi.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.ssssssss.magicapi.core.exception.MagicAPIException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcUtils {

	private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

	public static Connection getConnection(String driver, String url, String username, String password) {
		try {
			if (StringUtils.isBlank(driver)) {
				driver = DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
				if (StringUtils.isBlank(driver)) {
					throw new MagicAPIException("无法从url中获得驱动类");
				}
			}
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new MagicAPIException("找不到驱动：" + driver);
		}
		try {
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			logger.error("获取Jdbc链接失败", e);
			throw new MagicAPIException("获取Jdbc链接失败：" + e.getMessage());
		}
	}

	public static void close(Connection connection) {
		try {
			connection.close();
		} catch (Exception ignored) {

		}
	}
}
