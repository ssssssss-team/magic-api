package org.ssssssss.magicapi.datasource.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.datasource.model.DataSourceInfo;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSourceMagicDynamicRegistry extends AbstractMagicDynamicRegistry<DataSourceInfo> {

	private final MagicDynamicDataSource magicDynamicDataSource;

	private static final Logger logger = LoggerFactory.getLogger(DataSourceMagicDynamicRegistry.class);

	private static final ClassLoader CLASSLOADER = DataSourceMagicDynamicRegistry.class.getClassLoader();

	// copy from DataSourceBuilder
	private static final String[] DATA_SOURCE_TYPE_NAMES = new String[]{
			"com.zaxxer.hikari.HikariDataSource",
			"org.apache.tomcat.jdbc.pool.DataSource",
			"org.apache.commons.dbcp2.BasicDataSource"};

	public DataSourceMagicDynamicRegistry(MagicResourceStorage<DataSourceInfo> magicResourceStorage, MagicDynamicDataSource magicDynamicDataSource) {
		super(magicResourceStorage);
		this.magicDynamicDataSource = magicDynamicDataSource;
	}

	@EventListener(condition = "#event.type == 'datasource'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("注册数据源失败", e);
		}
	}

	@Override
	protected boolean register(MappingNode<DataSourceInfo> mappingNode) {
		DataSourceInfo info = mappingNode.getEntity();
		Map<String, Object> properties = new HashMap<>(info.getProperties());
		properties.put("url", info.getUrl());
		properties.put("username", info.getUsername());
		properties.put("password", info.getPassword());
		if (StringUtils.isBlank(info.getDriverClassName())) {
			String driverClass = DatabaseDriver.fromJdbcUrl(info.getUrl()).getDriverClassName();
			properties.put("driverClassName", driverClass);
		} else {
			properties.put("driverClassName", info.getDriverClassName());
		}
		DataSource datasource = createDataSource(getDataSourceType(info.getType()), properties);
		magicDynamicDataSource.put(info.getId(), info.getKey(), info.getName(), datasource, info.getMaxRows());
		return true;
	}

	@Override
	protected void unregister(MappingNode<DataSourceInfo> mappingNode) {
		magicDynamicDataSource.delete(mappingNode.getMappingKey());
	}

	// copy from DataSourceBuilder
	private DataSource createDataSource(Class<? extends DataSource> dataSourceType, Map<String, Object> properties) {
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
				return (Class<? extends DataSource>) ClassUtils.forName(datasourceType, CLASSLOADER);
			} catch (Exception ignored) {

			}
		}
		for (String name : DATA_SOURCE_TYPE_NAMES) {
			try {
				return (Class<? extends DataSource>) ClassUtils.forName(name, CLASSLOADER);
			} catch (Exception ignored) {
				// ignored
			}
		}
		return null;
	}

	@Override
	public List<DataSourceInfo> defaultMappings() {
		return magicDynamicDataSource.datasourceNodes().stream().filter(it -> it.getId() == null).map(it -> {
			DataSourceInfo dataSourceInfo = new DataSourceInfo();
			dataSourceInfo.setName(StringUtils.defaultIfBlank(it.getName(), StringUtils.defaultIfBlank(it.getKey(), "默认数据源")));
			dataSourceInfo.setKey(it.getKey());
			return dataSourceInfo;
		}).collect(Collectors.toList());
	}
}
