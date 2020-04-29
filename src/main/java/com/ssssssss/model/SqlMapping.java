package com.ssssssss.model;

public class SqlMapping {

    /**
     * SQL语句
     */
    private String sql;

    /**
     * 访问路径
     */
    private String path;

    /**
     * 文件路径
     */
    private String file;

    public SqlMapping(String sql, String path, String file) {
        this.sql = sql;
        this.path = path;
        this.file = file;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
