package org.ssssssss.magicapi.modules.db.model;

import java.sql.Types;
import java.util.Objects;

/**
 * 过程入参
 */
public class StoredParam {
    //参数SQL类型
    private Integer type;

    //入出参
    private StoreMode inOut;

    //参数值
    private Object value;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public StoreMode getInOut() {
        return inOut;
    }

    public void setInOut(StoreMode inOut) {
        this.inOut = inOut;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static int paramType(String type){
        if (Objects.equals(type, "BIT")) {
            return Types.BIT;
        } else if (Objects.equals(type, "TINYINT")) {
            return Types.TINYINT;
        } else if (Objects.equals(type, "SMALLINT")) {
            return Types.SMALLINT;
        } else if (Objects.equals(type, "INTEGER")) {
            return Types.INTEGER;
        } else if (Objects.equals(type, "BIGINT")) {
            return Types.BIGINT;
        } else if (Objects.equals(type, "FLOAT")) {
            return Types.FLOAT;
        } else if (Objects.equals(type, "REAL")) {
            return Types.REAL;
        } else if (Objects.equals(type, "NUMERIC")) {
            return Types.NUMERIC;
        } else if (Objects.equals(type, "DECIMAL")) {
            return Types.DECIMAL;
        } else if (Objects.equals(type, "CHAR")) {
            return Types.CHAR;
        } else if (Objects.equals(type, "VARCHAR")) {
            return Types.VARCHAR;
        } else if (Objects.equals(type, "LONGVARCHAR")) {
            return Types.LONGVARCHAR;
        } else if (Objects.equals(type, "DATE")) {
            return Types.DATE;
        } else if (Objects.equals(type, "TIME")) {
            return Types.TIME;
        } else if (Objects.equals(type, "TIMESTAMP")) {
            return Types.TIMESTAMP;
        } else if (Objects.equals(type, "BINARY")) {
            return Types.BINARY;
        } else if (Objects.equals(type, "VARBINARY")) {
            return Types.VARBINARY;
        } else if (Objects.equals(type, "LONGVARBINARY")) {
            return Types.LONGVARBINARY;
        } else if (Objects.equals(type, "NULL")) {
            return Types.NULL;
        } else if (Objects.equals(type, "OTHER")) {
            return Types.OTHER;
        } else if (Objects.equals(type, "JAVA_OBJECT")) {
            return Types.JAVA_OBJECT;
        } else if (Objects.equals(type, "DISTINCT")) {
            return Types.DISTINCT;
        } else if (Objects.equals(type, "STRUCT")) {
            return Types.STRUCT;
        } else if (Objects.equals(type, "ARRAY")) {
            return Types.ARRAY;
        } else if (Objects.equals(type, "BLOB")) {
            return Types.BLOB;
        } else if (Objects.equals(type, "CLOB")) {
            return Types.CLOB;
        } else if (Objects.equals(type, "REF")) {
            return Types.REF;
        } else if (Objects.equals(type, "DATALINK")) {
            return Types.DATALINK;
        } else if (Objects.equals(type, "BOOLEAN")) {
            return Types.BOOLEAN;
        } else if (Objects.equals(type, "ROWID")) {
            return Types.ROWID;
        } else if (Objects.equals(type, "NCHAR")) {
            return Types.NCHAR;
        } else if (Objects.equals(type, "NVARCHAR")) {
            return Types.NVARCHAR;
        } else if (Objects.equals(type, "LONGNVARCHAR")) {
            return Types.LONGNVARCHAR;
        } else if (Objects.equals(type, "NCLOB")) {
            return Types.NCLOB;
        } else if (Objects.equals(type, "SQLXML")) {
            return Types.SQLXML;
        } else if (Objects.equals(type, "REF_CURSOR")) {
            return Types.REF_CURSOR;
        } else if (Objects.equals(type, "TIME_WITH_TIMEZONE")) {
            return Types.TIME_WITH_TIMEZONE;
        } else if (Objects.equals(type, "TIMESTAMP_WITH_TIMEZONE")) {
            return Types.TIMESTAMP_WITH_TIMEZONE;
        }
        return Types.NULL;
    }
}
