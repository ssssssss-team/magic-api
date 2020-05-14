package org.ssssssss.session;

import org.ssssssss.utils.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DynamicDataSource {

    private Map<String, DataSource> dataSourceMap = new HashMap<>();

    public void put(String dataSourceName, DataSource dataSource) {
        if(dataSourceName == null){
            dataSourceName = "";
        }
        this.dataSourceMap.put(dataSourceName, dataSource);
    }

    public DataSource getDataSource(String dataSourceName) {
        if(dataSourceName == null){
            dataSourceName = "";
        }
        DataSource dataSource = dataSourceMap.get(dataSourceName);
        Assert.isNotNull(dataSource, String.format("找不到数据源%s", dataSourceName));
        return dataSource;
    }
}
