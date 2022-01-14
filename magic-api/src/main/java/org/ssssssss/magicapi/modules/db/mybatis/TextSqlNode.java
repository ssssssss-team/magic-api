package org.ssssssss.magicapi.modules.db.mybatis;

import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.parsing.GenericTokenParser;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 普通SQL节点
 *
 * @author jmxd
 * @version : 2020-05-18
 */
public class TextSqlNode extends SqlNode {

	private static final GenericTokenParser CONCAT_TOKEN_PARSER = new GenericTokenParser("${", "}", false);

	private static final GenericTokenParser REPLACE_TOKEN_PARSER = new GenericTokenParser("#{", "}", true);

	private static final GenericTokenParser IF_TOKEN_PARSER = new GenericTokenParser("?{", "}", true);

	private static final GenericTokenParser IF_PARAM_TOKEN_PARSER = new GenericTokenParser("?{", ",", true);

	/**
	 * SQL
	 */
	private final String text;

	public TextSqlNode(String text) {
		this.text = text;
	}

	public static String parseSql(String sql, Map<String, Object> varMap, List<Object> parameters) {
		// 处理?{}参数
		sql = IF_TOKEN_PARSER.parse(sql.trim(), text -> {
			AtomicBoolean ifTrue = new AtomicBoolean(false);
			String val = IF_PARAM_TOKEN_PARSER.parse("?{" + text, param -> {
				ifTrue.set(BooleanLiteral.isTrue(ScriptManager.executeExpression(param, varMap)));
				return null;
			});
			return ifTrue.get() ? val : "";
		});
		// 处理${}参数
		sql = CONCAT_TOKEN_PARSER.parse(sql, text -> String.valueOf(ScriptManager.executeExpression(text, varMap)));
		// 处理#{}参数
		sql = REPLACE_TOKEN_PARSER.parse(sql, text -> {
			Object value = ScriptManager.executeExpression(text, varMap);
			if (value == null) {
				parameters.add(null);
				return "?";
			}
			try {
				//对集合自动展开
				List<Object> objects = StreamExtension.arrayLikeToList(value);
				parameters.addAll(objects);
				return IntStream.range(0, objects.size()).mapToObj(t -> "?").collect(Collectors.joining(","));
			} catch (Exception e) {
				parameters.add(value);
				return "?";
			}
		});
		return sql;
	}

	@Override
	public String getSql(Map<String, Object> paramMap, List<Object> parameters) {
		return parseSql(text, paramMap, parameters) + executeChildren(paramMap, parameters).trim();
	}
}
