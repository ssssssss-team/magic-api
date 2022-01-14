package org.ssssssss.magicapi.datasource.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.datasource.model.DataSourceInfo;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
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
