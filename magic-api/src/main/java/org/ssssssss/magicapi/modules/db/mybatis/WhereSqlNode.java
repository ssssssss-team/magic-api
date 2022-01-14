package org.ssssssss.magicapi.modules.db.mybatis;

import java.util.List;
import java.util.Map;

/**
 * 对应XML中 <where>
 *
 * @author zhangxu
 * @version : 2020-12-05
 */
public class WhereSqlNode extends TrimSqlNode {
	public WhereSqlNode() {
		this.prefix = "WHERE";
		this.prefixOverrides = "AND | OR | AND\n| OR\n| AND\r| OR\r| AND\t| OR\t";
	}

	@Override
	public String getSql(Map<String, Object> paramMap, List<Object> parameters) {
		String sql = super.getSql(paramMap, parameters);
		if (this.prefix.equals(sql.trim())) {
			return "";
		}
		return sql;
	}

}
