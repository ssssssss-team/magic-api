package org.ssssssss.magicapi.modules.db.mybatis;

import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.List;
import java.util.Map;

/**
 * 对应XML中 <if>、<elseif>
 *
 * @author jmxd
 * @version : 2020-05-18
 */
public class IfSqlNode extends SqlNode {
	/**
	 * 判断表达式
	 */
	private final String test;

	private final SqlNode nextNode;

	public IfSqlNode(String test, SqlNode nextNode) {
		this.test = test;
		this.nextNode = nextNode;
	}

	@Override
	public String getSql(Map<String, Object> paramMap, List<Object> parameters) {
		// 执行表达式
		Object value = ScriptManager.executeExpression(test, paramMap);
		// 判断表达式返回结果是否是true，如果不是则过滤子节点
		if (BooleanLiteral.isTrue(value)) {
			return executeChildren(paramMap, parameters);
		}
		if (nextNode != null) {
			return nextNode.getSql(paramMap, parameters);
		}
		return "";
	}
}
