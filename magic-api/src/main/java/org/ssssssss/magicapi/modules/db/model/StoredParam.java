package org.ssssssss.magicapi.modules.db.model;

import java.sql.Types;

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
        Integer sqlType = SqlTypes.getSqlType(type);
        return sqlType == null ? Types.NULL : sqlType;
    }
}
