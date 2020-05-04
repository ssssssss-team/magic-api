package org.ssssssss.scripts;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.context.RequestContext;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * 对应XML中 <foreach>
 */
public class ForeachSqlNode extends SqlNode{

    /**
     * 数据集合，支持Collection、数组
     */
    private String collection;

    /**
     * item 变量名
     */
    private String item;

    /**
     * 拼接起始SQL
     */
    private String open;

    /**
     * 拼接结束SQL
     */
    private String close;

    /**
     * 分隔符
     */
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
        // 提取集合
        Object value = context.evaluate(this.collection);
        // 如果集合为空，则过滤该节点
        if(value == null){
            return "";
        }
        // 开始拼接SQL,
        String sql = StringUtils.defaultString(this.open);
        // 如果集合是Collection对象或其子类，则转成数组
        if(value instanceof Collection){
            value = ((Collection) value).toArray();
        }
        // 判断不是数组，则过滤子节点并返回
        if (!value.getClass().isArray()) {
            return "";
        }
        // 获取数组长度
        int len = Array.getLength(value);
        for (int i = 0; i < len; i++) {
            // 存入item对象
            context.put(this.item, Array.get(value, i));
            // 拼接子节点
            sql += executeChildren(context);
            // 拼接分隔符
            if (i + 1 < len) {
                sql += StringUtils.defaultString(this.separator);
            }
        }
        // 拼接结束SQL
        return sql + StringUtils.defaultString(this.close);
    }
}
