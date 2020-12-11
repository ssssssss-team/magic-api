package org.ssssssss.magicapi.modules;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.controller.MagicDynamicDataSource.DataSourceNode;
import org.ssssssss.magicapi.exception.MagicAPIException;
import org.ssssssss.script.annotation.UnableCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamedTable {

    @UnableCall
    private String tableName;

    @UnableCall
	private DataSourceNode dataSourceNode;

    @UnableCall
    StringBuilder sqlBuilder;

    @UnableCall
    String primary;

    @UnableCall
    Map<String,Object> params = new HashMap<>();

    @UnableCall
    List<Where> wheres = new ArrayList<>();

    public NamedTable(){

    }

    public NamedTable(String tableName, DataSourceNode dataSourceNode){
        this.tableName = tableName;
        this.dataSourceNode = dataSourceNode;
    }

    public NamedTable primary(String primary){
        this.primary = primary;
        return this;
    }

    public NamedTable column(String key,Object value){
        this.params.put(key,value);
        return this;
    }

    public int insert(Map<String,Object> data){
        this.params.putAll(data);
        if(this.params.size() == 0){
            throw new MagicAPIException("参数不能为空");
        }
        try {
            List<Object> values = new ArrayList<>();
            List<String> fields = new ArrayList<>();
            List<String> valuePlaceholders = new ArrayList<>();
            StringBuffer sb = new StringBuffer();
            sb.append("insert into ");
            sb.append(tableName);
            for(Map.Entry<String, Object> entry : this.params.entrySet()){
                String key = entry.getKey();
                fields.add(key);
                valuePlaceholders.add("?");
                values.add(entry.getValue());
            }
            sb.append("("+ StringUtils.join(fields,",") +")");
            sb.append(" values("+StringUtils.join(valuePlaceholders,",")+")");
            return dataSourceNode.getJdbcTemplate().update(sb.toString(),values.toArray());
        }catch (Exception e){
            throw new MagicAPIException("执行插入报错："+e.getMessage());
        }
    }

    public int update(){
        return update(null);
    }

    public int update(Map<String,Object> data){
        if(null != data){
            this.params.putAll(data);
        }
        if((null != this.primary && !"".equals(this.primary)) || (null != wheres && wheres.size() != 0)){
            if(this.params.size() == 0){
                throw new MagicAPIException("参数不能为空");
            }
            try {
                Map<String, Object> wheresMap = buildWheres();
                String whereSql = (null == wheresMap.get("where")) ? "" : wheresMap.get("where").toString();
                StringBuffer sb = new StringBuffer();
                sb.append("update ");
                sb.append(tableName);
                sb.append(" set ");
                List<Object> params = new ArrayList<>();
                for(Map.Entry<String, Object> entry : this.params.entrySet()){
                    String key = entry.getKey();
                    if(StringUtils.isNotBlank(whereSql)){
                        sb.append(key + "=" + "?,");
                        params.add(entry.getValue());
                    }else{
                        if(!key.equals(this.primary)){
                            sb.append(key + "=" + "?,");
                            params.add(entry.getValue());
                        }
                    }
                }
                if(StringUtils.isNotBlank(whereSql)){
                    sb.append(whereSql);
                    List<Object> values = (List<Object>) wheresMap.get("values");
                    params.addAll(values);
                }else{
                    String primaryValue = this.params.get(this.primary).toString();
                    if(StringUtils.isBlank(primaryValue)){
                        throw new MagicAPIException("主键值不能为空");
                    }
                    sb.append(" where ");
                    sb.append(this.primary);
                    sb.append("=?");
                    params.add(primaryValue);
                }
                return dataSourceNode.getJdbcTemplate().update(sb.toString().replace("?, ","? "),params.toArray());
            }catch (Exception e){
                throw new MagicAPIException("执行更新报错："+e.getMessage());
            }
        }else{
            throw new MagicAPIException("设置主键或者设置条件");
        }
    }

    public Map<String,Object> buildWheres(){
        List<Object> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" where 1=1 ");
        for(Where where : wheres){
            String relation = where.getRelation();
            String column = where.getColumn();
            Object value = where.getValue();
            relation = relation.equals("eq") ? "=" : relation;
            sb.append(" and ");
            sb.append(column);
            sb.append(relation);
            sb.append("?");
            values.add(value);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("values",values);
        map.put("where", sb.toString());
        return map;
    }

    public NamedTable eq(String key,Object value){
        wheres.add(new Where(Where.EQ,key,value));
        return this;
    }

    public class Where{

        public static final String EQ = "eq";

        private String relation;

        private String column;

	    private Object value;

	    public Where(){

        }

        public Where(String relation,String column,Object value){
	        this.relation = relation;
	        this.column = column;
	        this.value = value;
        }

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }

}
