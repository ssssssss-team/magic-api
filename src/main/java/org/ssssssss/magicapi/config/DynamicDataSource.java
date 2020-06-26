package org.ssssssss.magicapi.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.utils.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DynamicDataSource {

    private static Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);

    private Map<String, JdbcTemplate> dataSourceMap = new HashMap<>();

    public void put(DataSource dataSource) {
        put(null, dataSource);
    }

    public void put(String dataSourceName, DataSource dataSource) {
        if (dataSourceName == null) {
            dataSourceName = "";
        }
        logger.info("注册数据源：{}", StringUtils.isNotBlank(dataSourceName) ? dataSourceName : "default");
        this.dataSourceMap.put(dataSourceName, new JdbcTemplate(dataSource));
    }

    public JdbcTemplate getJdbcTemplate() {
        return getJdbcTemplate(null);
    }

    public JdbcTemplate getJdbcTemplate(String dataSourceName) {
        if (dataSourceName == null) {
            dataSourceName = "";
        }
        JdbcTemplate jdbcTemplate = dataSourceMap.get(dataSourceName);
        Assert.isNotNull(jdbcTemplate, String.format("找不到数据源%s", dataSourceName));
        return jdbcTemplate;
    }
}
