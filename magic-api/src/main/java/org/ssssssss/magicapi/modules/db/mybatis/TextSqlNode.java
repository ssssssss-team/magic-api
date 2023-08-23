package org.ssssssss.magicapi.modules.db.mybatis;

import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.ssssssss.magicapi.modules.db.SQLModule;
import org.ssssssss.magicapi.modules.db.model.SqlTypes;
import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.parsing.GenericTokenParser;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

	private static final GenericTokenParser OUT_PARAM_TOKEN_PARSER = new GenericTokenParser("@{", ",", true);

	private static final GenericTokenParser OUT_TOKEN_PARSER = new GenericTokenParser("@{", "}", true);

	private static final GenericTokenParser TYPE_TOKEN_PARSER = new GenericTokenParser(",", "}", true);

	private static final GenericTokenParser INOUT_TOKEN_PARSER = new GenericTokenParser("@{", "(", true);

	/**
	 * SQL
	 */
	private final String text;

	public TextSqlNode(String text) {
		this.text = text;
	}

	public static String parseSql(String sql, Map<String, Object> varMap, List<Object> parameters) {
		SQLModule.params = new ArrayList<>();
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
		sql = OUT_TOKEN_PARSER.parse(sql, text -> {
			// 获取类型
			AtomicInteger sqlType = new AtomicInteger(Types.NULL);
			TYPE_TOKEN_PARSER.parse(text + "}", type -> {
				sqlType.set(SqlTypes.getSqlType(type, true));
				return null;
			});
			// 获取名称
			OUT_PARAM_TOKEN_PARSER.parse("@{" + text, param -> {
				int index = param.indexOf("(");
				if (index > 0) {
					// 获取入参值
					String value = param.substring(index + 1, param.lastIndexOf(")"));
					INOUT_TOKEN_PARSER.parse("@{" + param, inoutParam -> {
						SqlInOutParameter p = new SqlInOutParameter(inoutParam, sqlType.get());
						parameters.add(new SqlParameterValue(p, ScriptManager.executeExpression(value, varMap)));
						return null;
					});
				} else {
					parameters.add(new SqlOutParameter(param, sqlType.get()));
				}
				return null;
			});
			return "?";
		});
		return sql;
	}

	@Override
	public String getSql(Map<String, Object> paramMap, List<Object> parameters) {
		return parseSql(text, paramMap, parameters) + executeChildren(paramMap, parameters).trim();
	}
}
