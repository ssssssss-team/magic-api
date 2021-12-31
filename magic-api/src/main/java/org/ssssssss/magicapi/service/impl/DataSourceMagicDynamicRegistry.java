package org.ssssssss.magicapi.service.impl;

import org.apache.commons.lang3.StringUtils;
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
import org.ssssssss.magicapi.config.MagicDynamicDataSource;
import org.ssssssss.magicapi.event.FileEvent;
import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.AbstractMagicDynamicRegistry;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DataSourceMagicDynamicRegistry extends AbstractMagicDynamicRegistry<DataSourceInfo> {

	private final MagicDynamicDataSource magicDynamicDataSource;

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
		processEvent(event);
	}

	@Override
	public boolean register(DataSourceInfo info) {
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
	public boolean unregister(DataSourceInfo info) {
		return magicDynamicDataSource.delete(info.getKey());
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


}
