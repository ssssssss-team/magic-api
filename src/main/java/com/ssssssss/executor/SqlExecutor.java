package com.ssssssss.executor;

import com.ssssssss.enums.SqlMode;
import com.ssssssss.exception.S8Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;

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

    public Object execute(SqlMode mode, String sql, List<Object> parameters, Class<?> returnType) throws SQLException {
        if (SqlMode.SELECT_LIST == mode) {
            return queryForList(sql, parameters, returnType == null ? Map.class : returnType);
        } else if (SqlMode.UPDATE == mode || SqlMode.INSERT == mode || SqlMode.DELETE == mode) {
            return update(sql, parameters);
        } else if (SqlMode.SELECT_ONE == mode) {
            return queryForOne(sql, parameters, returnType);
        } else if (SqlMode.SELECT_NUMBER == mode) {
            return queryForNumber(sql, parameters, returnType);
        } else {
            throw new S8Exception("暂时不支持[" + mode + "]模式");
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

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

    private <T> T queryForNumber(String sql, List<Object> params, Class<T> returnType) throws SQLException {
        Connection conn = getConnection();
        try {
            return queryForNumber(conn, sql, params, returnType);
        } finally {
            closeConnection(conn);
        }
    }

    public <T> T queryForNumber(Connection connection, String sql, List<Object> params, Class<T> returnType) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = createPreparedStatement(connection, sql, params);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getObject(1, returnType);
            }
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
        return null;
    }

    private Object queryForOne(String sql, List<Object> params, Class<?> returnType) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = createPreparedStatement(connection, sql, params);
            rs = stmt.executeQuery();
            if (returnType == null || returnType == Map.class) {
                ResultSetMetaData rsd = rs.getMetaData();
                int columnCount = rsd.getColumnCount();
                if (rs.next()) {
                    return fetchResultSet(rs);
                }
            } else if (rs.next()) {
                return rs.getObject(1, returnType);
            }
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(connection);
        }
        return null;
    }

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
                    list.add(rs.getObject(1, returnType));
                }
            }
            return list;
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }
    }

    private Map<String,Object> fetchResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsd = rs.getMetaData();
        int columnCount = rsd.getColumnCount();
        Map<String,Object> row = new HashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            row.put(underscoreToCamelCase(rsd.getColumnName(i)), rs.getObject(i));
        }
        return row;
    }

    private String underscoreToCamelCase(String columnName){
        if(mapUnderscoreToCamelCase){
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

    private PreparedStatement createPreparedStatement(Connection conn, String sql, List<Object> params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        logger.debug("执行SQL:{}",sql);
        setStatementParams(stmt, params);
        return stmt;
    }

    private void setStatementParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        if (params != null) {
            logger.debug("sql参数:{}",params);
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


    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
