package com.ssssssss.scripts;

import com.ssssssss.context.RequestContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * 对应XML中 <foreach>
 */
public class ForeachSqlNode extends SqlNode{

    private String collection;

    private String item;

    private String open;

    private String close;

    private String separator;

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String getSql(RequestContext context) {
        Object value = context.evaluate(this.collection);
        if(value == null){
            return "";
        }
        String sql = StringUtils.defaultString(this.open);
        if(value instanceof Collection){
            value = ((Collection) value).toArray();
        }
        if(value.getClass().isArray()){
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                context.put(this.item,Array.get(value,i));
                sql += executeChildren(context);
                if(i + 1 < len){
                    sql += StringUtils.defaultString(this.separator);
                }
            }
        }
        sql+=StringUtils.defaultString(this.close);
        return sql;
    }
}
