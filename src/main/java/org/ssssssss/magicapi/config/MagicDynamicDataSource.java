package org.ssssssss.magicapi.config;

import javax.sql.DataSource;

public class MagicDynamicDataSource extends DynamicDataSource {

	public void setDefault(DataSource dataSource) {
		put(dataSource);
	}

	public void add(String dataSourceName, DataSource dataSource) {
		put(dataSourceName, dataSource);
	}
}
