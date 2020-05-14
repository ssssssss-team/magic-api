package org.ssssssss.executor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.ssssssss.cache.SqlCache;
import org.ssssssss.context.RequestContext;
import org.ssssssss.dialect.Dialect;
import org.ssssssss.dialect.DialectUtils;
import org.ssssssss.enums.SqlMode;
import org.ssssssss.exception.S8Exception;
import org.ssssssss.provider.KeyProvider;
import org.ssssssss.scripts.SqlNode;
import org.ssssssss.session.DynamicDataSource;
import org.ssssssss.session.ExecuteSqlStatement;
import org.ssssssss.session.SqlStatement;
import org.ssssssss.utils.Assert;
import org.ssssssss.utils.DomUtils;
import org.w3c.dom.Node;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL执行器
 */
public class SqlExecutor {

    private Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

    private Map<String, JdbcTemplate> jdbcTemplates = new HashMap<>();

    private DynamicDataSource dynamicDataSource;

    private ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper();

    private Map<String, KeyProvider> keyProviders = new HashMap<>();

    private Map<String, Dialect> cachedDialects = new ConcurrentHashMap<>();

    private SqlCache sqlCache;

    public SqlExecutor(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    public void setSqlCache(SqlCache sqlCache) {
        this.sqlCache = sqlCache;
    }

    /**
     * 设置是否是驼峰命名
     *
     * @param mapUnderscoreToCamelCase
     */
    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        if (mapUnderscoreToCamelCase) {
            columnMapRowMapper = new ColumnMapRowMapper() {
                @Override
                protected String getColumnKey(String columnName) {
                    columnName = columnName.toLowerCase();
                    boolean upperCase = false;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < columnName.length(); i++) {
                        char ch = columnName.charAt(i);
                        if (ch == '_') {
                            upperCase = true;
                        } else if (upperCase) {
                            sb.append(Character.toUpperCase(ch));
                            upperCase = false;
                        } else {
                            sb.append(ch);
                        }
                    }
                    return sb.toString();
                }
            };
        } else {
            columnMapRowMapper = new ColumnMapRowMapper();
        }
    }

    public void addKeyProvider(KeyProvider provider) {
        keyProviders.put(provider.getName(), provider);
    }

    /**
     * 执行SQL
     */
    public Object execute(ExecuteSqlStatement statement) {
        // 获取SQL
        String sql = statement.getSql();
        // 获取参数
        Object[] parameters = statement.getParameters();
        // 获取SQL模式
        SqlMode mode = statement.getSqlMode();
        // 获取返回值类型
        Class<?> returnType = statement.getReturnType();
        // 缓存Key
        String sqlCacheKey = null;
        // 返回值
        Object value;
        // 判断是否使用缓存
        if (this.sqlCache != null && StringUtils.isNotBlank(statement.getUseCache())) {
            // 构建key
            sqlCacheKey = this.sqlCache.buildSqlCacheKey(sql, parameters);
            // 查询缓存
            value = this.sqlCache.get(statement.getUseCache(), sqlCacheKey);
            if (value != null) {
                return value;
            }
        }
        JdbcTemplate jdbcTemplate = getJdbcTemplate(statement.getDataSourceName());
        // 打印SQL日志
        printLog(statement.getDataSourceName(), sql, parameters);
        if (SqlMode.SELECT_LIST == mode) {  //查询List
            if (returnType == null || returnType == Map.class) {
                value = jdbcTemplate.query(sql, parameters, columnMapRowMapper);
            } else {
                value = jdbcTemplate.queryForList(sql, parameters, returnType);
            }

        } else if (SqlMode.UPDATE == mode || SqlMode.INSERT == mode || SqlMode.DELETE == mode) {    //增删改
            int retVal = jdbcTemplate.update(sql, parameters);
            // 删除缓存
            if (retVal > 0 && this.sqlCache != null && StringUtils.isNotBlank(statement.getDeleteCache())) {
                this.sqlCache.remove(statement.getDeleteCache());
            }
            // 当设置返回值是boolean类型时,做>0比较
            if (returnType == Boolean.class) {
                return retVal > 0;
            }
            return retVal;
        } else if (SqlMode.SELECT_ONE == mode) {    //查询一条
            Collection collection;
            if (returnType == null || returnType == Map.class) {
                collection = jdbcTemplate.query(sql, columnMapRowMapper, parameters);
            } else {
                collection = jdbcTemplate.queryForList(sql, returnType, parameters);
            }
            value = collection != null && collection.size() >= 1 ? collection.iterator().next() : null;
        } else {
            throw new S8Exception("暂时不支持[" + mode + "]模式");
        }
        // 判断是否使用了缓存
        if (sqlCacheKey != null && value != null) {
            this.sqlCache.put(statement.getUseCache(), sqlCacheKey, value);
        }
        return value;
    }


    public Object executeInsertWithPk(SqlStatement statement, RequestContext requestContext) throws SQLException {
        String dataSourceName = statement.getDataSourceName();
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSourceName);
        Node selectKey = statement.getSelectKey();
        String selectKeyType;
        if (selectKey != null && (selectKeyType = DomUtils.getNodeAttributeValue(selectKey, "type")) != null) {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            Object value = null;
            try {
                // 获取主键名称
                String key = DomUtils.getNodeAttributeValue(selectKey, "key");
                Assert.isNotBlank(key, "select-key标签的key不能为空");
                if ("select".equalsIgnoreCase(selectKeyType)) {
                    SqlNode selectKeySqlNode = statement.getSelectKeySqlNode();
                    // 判断执行时机是否在执行插入之前
                    boolean before = "before".equalsIgnoreCase(DomUtils.getNodeAttributeValue(selectKey, "order"));
                    if (before) {
                        // 查询key
                        value = executeSelectKey(dataSourceName, connection, selectKeySqlNode.getSql(requestContext), requestContext.getParameters());
                        // 清空参数
                        requestContext.getParameters().clear();
                        // 存入key
                        requestContext.put(key, value);
                    }
                    // 获取插入SQL
                    String insertSQL = statement.getSqlNode().getSql(requestContext);
                    // 执行插入
                    executeUpdate(dataSourceName, connection, insertSQL, requestContext.getParameters(),statement.getDeleteCache());

                    // 清空参数
                    requestContext.getParameters().clear();
                    if (!before) {
                        value = executeSelectKey(dataSourceName, connection, selectKeySqlNode.getSql(requestContext), requestContext.getParameters());
                    }

                } else {
                    // 获取主键生成策略
                    KeyProvider keyProvider = keyProviders.get(selectKeyType);
                    Assert.isNotNull(keyProvider, String.format("找不到主键生成策略%s", selectKeyType));
                    // 生成主键
                    value = keyProvider.getKey();
                    // 存入RequestContext中
                    requestContext.put(key, value);
                    // 获取插入SQL
                    String insertSQL = statement.getSqlNode().getSql(requestContext);
                    // 执行插入
                    executeUpdate(dataSourceName, connection, insertSQL, requestContext.getParameters(),statement.getDeleteCache());
                }
                return value;
            } finally {
                // 释放连接
                DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
            }
        } else {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String insertSQL = statement.getSqlNode().getSql(requestContext);
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
                new ArgumentPreparedStatementSetter(requestContext.getParameters().toArray()).setValues(ps);
                return ps;
            }, keyHolder);
            return keyHolder.getKey().longValue();
        }
    }

    /**
     * 执行插入
     */
    private int executeUpdate(String dataSourceName, Connection connection, String sql, List<Object> parameters,String deleteCache) throws SQLException {
        PreparedStatement ps = null;
        try {
            printLog(dataSourceName, sql, parameters);
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            new ArgumentPreparedStatementSetter(parameters.toArray()).setValues(ps);
            int val = ps.executeUpdate();
            if (this.sqlCache != null && StringUtils.isNotBlank(deleteCache)) {
                this.sqlCache.remove(deleteCache);
            }
            return val;
        } finally {
            JdbcUtils.closeStatement(ps);
        }
    }

    /**
     * 查询主键值
     */
    private Object executeSelectKey(String dataSourceName, Connection connection, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Object[] params = parameters.toArray();
            ps = connection.prepareStatement(sql);
            // 打印SQL语句
            printLog(dataSourceName, sql, params);
            // 设置参数
            setPreparedStatementParameters(ps, params);
            rs = ps.executeQuery();
            Assert.isTrue(rs.next(), "查询key出错，结果集至少应有一条");
            return rs.getObject(1);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
        }
    }

    /**
     * 设置参数
     */
    private void setPreparedStatementParameters(PreparedStatement ps, Object... parameters) throws SQLException {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(parameters);
        argumentPreparedStatementSetter.setValues(ps);
    }

    /**
     * 打印日志
     */
    private void printLog(String dataSourceName, String sql, Object... parameters) {
        logger.debug("执行SQL({}):{}", dataSourceName == null ? "default" : dataSourceName, sql);
        logger.debug("SQL参数{}", Arrays.toString(parameters));
    }

    /**
     * 获取JdbcTemplate
     */
    private JdbcTemplate getJdbcTemplate(String dataSourceName) {
        if (jdbcTemplates.containsKey(dataSourceName)) {
            return jdbcTemplates.get(dataSourceName);
        }
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dynamicDataSource.getDataSource(dataSourceName));
        jdbcTemplates.put(dataSourceName, jdbcTemplate);
        return jdbcTemplate;
    }

    /**
     * 获取数据库方言
     */
    public Dialect getDialect(String dataSourceName) throws SQLException {
        Dialect dialect = cachedDialects.get(dataSourceName);
        if (dialect == null && !cachedDialects.containsKey(dataSourceName)) {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSourceName);
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            try {
                dialect = DialectUtils.getDialectFromUrl(connection.getMetaData().getURL());
                cachedDialects.put(dataSourceName, dialect);
                return dialect;
            } finally {
                // 释放连接
                DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
            }
        }
        return dialect;
    }

}
