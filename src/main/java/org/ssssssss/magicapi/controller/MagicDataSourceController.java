package org.ssssssss.magicapi.controller;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.MagicDynamicDataSource;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.script.functions.ObjectConvertExtension;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MagicDataSourceController extends MagicController implements MagicExceptionHandler {

	private static final ClassLoader classLoader = MagicDataSourceController.class.getClassLoader();

	// copy from DataSourceBuilder
	private static final String[] DATA_SOURCE_TYPE_NAMES = new String[]{
			"com.zaxxer.hikari.HikariDataSource",
			"org.apache.tomcat.jdbc.pool.DataSource",
			"org.apache.commons.dbcp2.BasicDataSource"};

	private final Resource resource;

	public MagicDataSourceController(MagicConfiguration configuration) {
		super(configuration);
		resource = configuration.getWorkspace().getDirectory(Constants.PATH_DATASOURCE);
		if (!resource.exists()) {
			resource.mkdir();
		}
	}

	/**
	 * 查询数据源列表
	 */
	@RequestMapping("/datasource/list")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<Map<String, Object>>> list() {
		List<Map<String, Object>> list = configuration.getMagicDynamicDataSource().datasourceNodes().stream().map(it -> {
			Map<String, Object> row = new HashMap<>();
			row.put("id", it.getId());    // id为空的则认为是不可修改的
			row.put("key", it.getKey());    // 如果为null 说明是主数据源
			row.put("name", it.getName());
			return row;
		}).collect(Collectors.toList());
		return new JsonBean<>(list);
	}

	@RequestMapping("/datasource/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody Map<String, String> properties) {
		try {
			DataSource dataSource = createDataSource(properties);
			dataSource.getConnection();
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
		return new JsonBean<>();
	}

	/**
	 * 保存数据源
	 *
	 * @param properties 数据源配置信息
	 */
	@RequestMapping("/datasource/save")
	@Valid(readonly = false, authorization = Authorization.DATASOURCE_SAVE)
	@ResponseBody
	public JsonBean<String> save(@RequestBody Map<String, String> properties) {
		String key = properties.get("key");
		// 校验key是否符合规则
		notBlank(key, DATASOURCE_KEY_REQUIRED);
		isTrue(IoUtils.validateFileName(key), DATASOURCE_KEY_INVALID);
		String name = properties.getOrDefault("name", key);
		String id = properties.get("id");
		Stream<String> keyStream;
		if (StringUtils.isBlank(id)) {
			keyStream = configuration.getMagicDynamicDataSource().datasources().stream();
		} else {
			keyStream = configuration.getMagicDynamicDataSource().datasourceNodes().stream()
					.filter(it -> !id.equals(it.getId()))
					.map(MagicDynamicDataSource.DataSourceNode::getKey);
		}
		String dsId = StringUtils.isBlank(id) ? UUID.randomUUID().toString().replace("-", "") : id;
		// 验证是否有冲突
		isTrue(keyStream.noneMatch(key::equals), DATASOURCE_KEY_EXISTS);

		int maxRows = ObjectConvertExtension.asInt(properties.get("maxRows"), -1);
		// 注册数据源
		configuration.getMagicDynamicDataSource().put(dsId, key, name, createDataSource(properties), maxRows);
		properties.put("id", dsId);
		resource.getResource(dsId + ".json").write(JsonUtils.toJsonString(properties));
		return new JsonBean<>(dsId);
	}

	/**
	 * 删除数据源
	 *
	 * @param id 数据源ID
	 */
	@RequestMapping("/datasource/delete")
	@Valid(readonly = false, authorization = Authorization.DATASOURCE_DELETE)
	@ResponseBody
	public JsonBean<Boolean> delete(String id) {
		// 查询数据源是否存在
		Optional<MagicDynamicDataSource.DataSourceNode> dataSourceNode = configuration.getMagicDynamicDataSource().datasourceNodes().stream()
				.filter(it -> id.equals(it.getId()))
				.findFirst();
		isTrue(dataSourceNode.isPresent(), DATASOURCE_NOT_FOUND);
		Resource resource = this.resource.getResource(id + ".json");
		// 删除数据源
		isTrue(resource.delete(), DATASOURCE_NOT_FOUND);
		// 取消注册数据源
		dataSourceNode.ifPresent(it -> configuration.getMagicDynamicDataSource().delete(it.getKey()));
		return new JsonBean<>(true);

	}

	@RequestMapping("/datasource/detail")
	@Valid(authorization = Authorization.DATASOURCE_VIEW)
	@ResponseBody
	public JsonBean<Object> detail(String id) {
		Resource resource = this.resource.getResource(id + ".json");
		byte[] bytes = resource.read();
		isTrue(bytes != null && bytes.length > 0, DATASOURCE_NOT_FOUND);
		return new JsonBean<>(JsonUtils.readValue(bytes, LinkedHashMap.class));
	}

	// 启动之后注册数据源
	public void registerDataSource() {
		resource.readAll();
		List<Resource> resources = resource.files(".json");
		// 删除旧的数据源
		configuration.getMagicDynamicDataSource().datasourceNodes().stream()
				.filter(it -> it.getId() != null)
				.map(MagicDynamicDataSource.DataSourceNode::getKey)
				.collect(Collectors.toList())
				.forEach(it -> configuration.getMagicDynamicDataSource().delete(it));
		TypeFactory factory = TypeFactory.defaultInstance();
		for (Resource item : resources) {
			Map<String, String> properties = JsonUtils.readValue(item.read(), factory.constructMapType(HashMap.class, String.class, String.class));
			if (properties != null) {
				String key = properties.get("key");
				String name = properties.getOrDefault("name", key);
				int maxRows = ObjectConvertExtension.asInt(properties.get("maxRows"), -1);
				configuration.getMagicDynamicDataSource().put(properties.get("id"), key, name, createDataSource(properties), maxRows);
			}
		}
	}

	// copy from DataSourceBuilder
	private DataSource createDataSource(Map<String, String> properties) {
		Class<? extends DataSource> dataSourceType = getDataSourceType(properties.get("type"));
		if (!properties.containsKey("driverClassName")
				&& properties.containsKey("url")) {
			String url = properties.get("url");
			String driverClass = DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
			properties.put("driverClassName", driverClass);
		}
		DataSource dataSource = BeanUtils.instantiateClass(dataSourceType);
		ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
		ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
		aliases.addAliases("url", "jdbc-url");
		aliases.addAliases("username", "user");
		Binder binder = new Binder(source.withAliases(aliases));
		binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(dataSource));
		return dataSource;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends DataSource> getDataSourceType(String datasourceType) {
		if (StringUtils.isNotBlank(datasourceType)) {
			try {
				return (Class<? extends DataSource>) ClassUtils.forName(datasourceType, classLoader);
			} catch (Exception e) {
				throw new InvalidArgumentException(DATASOURCE_TYPE_NOT_FOUND.format(datasourceType));
			}
		}
		for (String name : DATA_SOURCE_TYPE_NAMES) {
			try {
				return (Class<? extends DataSource>) ClassUtils.forName(name, classLoader);
			} catch (Exception ignored) {
			}
		}
		throw new InvalidArgumentException(DATASOURCE_TYPE_NOT_SET);
	}
}
