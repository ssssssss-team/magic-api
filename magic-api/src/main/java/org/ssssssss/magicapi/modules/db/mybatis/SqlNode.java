package org.ssssssss.magicapi.modules.db.mybatis;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sql节点
 *
 * @author jmxd
 * @version : 2020-05-18
 */
public abstract class SqlNode {

	/**
	 * 子节点
	 */
	List<SqlNode> nodes = new ArrayList<>();
	/**
	 * SQL参数
	 */
	List<Object> parameters;

	/**
	 * 追加子节点
	 */
	public void addChildNode(SqlNode node) {
		this.nodes.add(node);
	}

	/**
	 * 获取该节点的SQL
	 */
	public String getSql(Map<String, Object> paramMap) {
		this.parameters = new ArrayList<>();
		return getSql(paramMap, parameters);
	}

	/**
	 * 获取该节点的SQL
	 */
	public abstract String getSql(Map<String, Object> paramMap, List<Object> parameters);

	/**
	 * 获取子节点SQL
	 */
	public String executeChildren(Map<String, Object> paramMap, List<Object> parameters) {
		StringBuilder sqlBuilder = new StringBuilder();
		for (SqlNode node : nodes) {
			sqlBuilder.append(StringUtils.defaultString(node.getSql(paramMap, parameters)));
			sqlBuilder.append(" ");
		}
		return sqlBuilder.toString();
	}

	public List<Object> getParameters() {
		return parameters;
	}
}
