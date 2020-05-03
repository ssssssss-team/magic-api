package com.ssssssss.executor;

import com.ssssssss.enums.SqlMode;
import com.ssssssss.exception.S8Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * SQL执行器
 */
public class SqlExecutor {

    private DataSource dataSource;

    private Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

    /**
     * 是否启用驼峰命名
     */
    private boolean mapUnderscoreToCamelCase;

    public SqlExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    /**
     * 执行SQL
     *
     * @param mode       SQL模式
     * @param sql        SQL
     * @param parameters SQL参数
     * @param returnType 返回值类型
     * @return
     * @throws SQLException
     */
    public Object execute(SqlMode mode, String sql, List<Object> parameters, Class<?> returnType) throws SQLException {
        if (SqlMode.SELECT_LIST == mode) {
            return queryForList(sql, parameters, returnType == null ? Map.class : returnType);
        } else if (SqlMode.UPDATE == mode || SqlMode.INSERT == mode || SqlMode.DELETE == mode) {
            int value = update(sql, parameters);
            // 当设置返回值是boolean类型时,做>0比较
            if(returnType == Boolean.class){
                return value > 0;
            }
            return value;
        } else if (SqlMode.SELECT_ONE == mode) {
            return queryForOne(sql, parameters, returnType);
        } else {
            throw new S8Exception("暂时不支持[" + mode + "]模式");
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 获取Connection并调用回调函数执行
     * @param connectionCallback    回调函数
     */
    public <T> T doInConnection(ConnectionCallback<T> connectionCallback) throws SQLException {
        Connection connection = getConnection();
        try {
            return connectionCallback.execute(connection);
        } finally {
            closeConnection(connection);
        }
    }

    private int update(String sql, List<Object> params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = createPreparedStatement(connection, sql, params);
            return stmt.executeUpdate();
        } finally {
            closeStatement(stmt);
            closeConnection(connection);
        }
    }

    /**
     * 查询一条
     * @param connection    连接对象
     * @param sql   SQL
     * @param params   SQL参数
     * @param returnType    返回值类型
     */
    public <T> T queryForOne(Connection connection, String sql, List<Object> params, Class<T> returnType) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = createPreparedStatement(connection, sql, params);
            rs = stmt.executeQuery();
            if (returnType == null || returnType == Map.class) {
                if (rs.next()) {
                    return (T) fetchResultSet(rs);
                }
            } else if (rs.next()) {
                // 返回值不是Map时，只取第一行第一列
                return rs.getObject(1, returnType);
            }
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
        return null;
    }

    /**
     * 查询一条
     * @param sql   SQL
     * @param params   SQL参数
     * @param returnType    返回值类型
     */
    private <T> T queryForOne(String sql, List<Object> params, Class<T> returnType) throws SQLException {
        Connection connection = getConnection();
        try {
            return queryForOne(connection, sql, params, returnType);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * 查询List
     * @param connection    连接对象
     * @param sql   SQL
     * @param params   SQL参数
     * @param returnType    返回值类型
     */
    public List<Object> queryForList(Connection connection, String sql, List<Object> params, Class<?> returnType) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = createPreparedStatement(connection, sql, params);
            rs = stmt.executeQuery();
            List<Object> list = new ArrayList<>();
            if (returnType == null || returnType == Map.class) {
                while (rs.next()) {
                    list.add(fetchResultSet(rs));
                }
            } else {
                while (rs.next()) {
                    // 返回值不是Map时，只取第一列
                    list.add(rs.getObject(1, returnType));
                }
            }
            return list;
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
    }

    /**
     * 从ResultSet中提取map对象
     */
    private Map<String, Object> fetchResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsd = rs.getMetaData();
        int columnCount = rsd.getColumnCount();
        Map<String, Object> row = new HashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            row.put(underscoreToCamelCase(rsd.getColumnName(i)), rs.getObject(i));
        }
        return row;
    }

    /**
     * 下划线转驼峰命名
     * @param columnName 列名
     * @return
     */
    private String underscoreToCamelCase(String columnName) {
        if (mapUnderscoreToCamelCase) {
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
            columnName = sb.toString();
        }
        return columnName;
    }


    private List<Object> queryForList(String sql, List<Object> params, Class<?> returnType) throws SQLException {
        Connection connection = getConnection();
        try {
            return queryForList(connection, sql, params, returnType);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * 统一创建PrepareStatement对象
     * @param conn  连接对象
     * @param sql   SQL
     * @param params SQL参数
     */
    private PreparedStatement createPreparedStatement(Connection conn, String sql, List<Object> params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        logger.debug("执行SQL:{}", sql);
        setStatementParams(stmt, params);
        return stmt;
    }

    /**
     * 设置SQL参数
     */
    private void setStatementParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        if (params != null) {
            logger.debug("sql参数:{}", params);
            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof Date) {
                    stmt.setTimestamp(i + 1, new java.sql.Timestamp(((Date) val).getTime()));
                } else {
                    stmt.setObject(i + 1, val);
                }
            }
        }
    }


    /**
     * 关闭连接
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * 关闭ResultSet
     */
    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * 关闭PrepareStatement
     */
    private void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
