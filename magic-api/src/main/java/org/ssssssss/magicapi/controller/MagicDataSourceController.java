package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.utils.JdbcUtils;

import java.sql.Connection;

public class MagicDataSourceController extends MagicController implements MagicExceptionHandler {

	public MagicDataSourceController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/datasource/jdbc/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody DataSourceInfo properties) {
		try {
			Connection connection = JdbcUtils.getConnection(properties.getDriverClassName(), properties.getUrl(), properties.getUsername(), properties.getPassword());
			JdbcUtils.close(connection);
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
		return new JsonBean<>("ok");
	}
}
