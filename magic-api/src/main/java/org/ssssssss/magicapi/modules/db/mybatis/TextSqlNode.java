package org.ssssssss.magicapi.modules.db.mybatis;

import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.ssssssss.magicapi.modules.db.SQLModule;
import org.ssssssss.magicapi.modules.db.model.StoreMode;
import org.ssssssss.magicapi.modules.db.model.StoredParam;
import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.parsing.GenericTokenParser;
import org.ssssssss.script.parsing.ast.literal.BooleanLiteral;

import java.sql.Types;
import java.util.*;
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

	private static final GenericTokenParser OUT_PARAM_TOKEN_PARSER = new GenericTokenParser("@{", ",", true);

    private static final GenericTokenParser OUT_TOKEN_PARSER = new GenericTokenParser("@{", "}", true);

    private static final GenericTokenParser TYPE_TOKEN_PARSER = new GenericTokenParser(",", "}", true);

    private static final GenericTokenParser INOUT_TOKEN_PARSER = new GenericTokenParser("@{", "(", true);

    private static final GenericTokenParser IN_PARAM_TOKEN_PARSER = new GenericTokenParser("#{", ",", true);

    private static final GenericTokenParser PARAM_TOKEN_PARSER = new GenericTokenParser("(", ")", true);

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
            StoredParam storedParam = new StoredParam();
            if (text.indexOf(",") > 0) {
                IN_PARAM_TOKEN_PARSER.parse("#{" + text, param -> {
                    PARAM_TOKEN_PARSER.parse(param,variable -> {
                        Object value = ScriptManager.executeExpression(variable, varMap);
                        storedParam.setValue(value);
                        storedParam.setInOut(StoreMode.IN);
                        TYPE_TOKEN_PARSER.parse(text + "}", type -> {
                            storedParam.setType(StoredParam.paramType(type));
                            SQLModule.params.add(new SqlParameter(param, StoredParam.paramType(type)));
                            return null;
                        });
                        parameters.add(storedParam);
                        return null;
                    });
                    return null;
                });
                return "?";
            } else {
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
            }
		});

        sql = OUT_TOKEN_PARSER.parse(sql,text -> {
            StoredParam storedParam = new StoredParam();
            String val = OUT_PARAM_TOKEN_PARSER.parse("@{" + text, param -> {
                TYPE_TOKEN_PARSER.parse(text + "}", type -> {
                if (param.indexOf("(") > 0) {
                    PARAM_TOKEN_PARSER.parse(param,variable -> {
                        Object value = ScriptManager.executeExpression(variable, varMap);
                        storedParam.setValue(value);
                        storedParam.setInOut(StoreMode.INOUT);
                        storedParam.setType(StoredParam.paramType(type));
                        return null;
                    });
                    INOUT_TOKEN_PARSER.parse("@{" + param, inoutParam -> {
                        SQLModule.params.add(new SqlInOutParameter(inoutParam, StoredParam.paramType(type)));
                        return null;
                    });
                } else {
                    Object value = ScriptManager.executeExpression(param, varMap);
                    storedParam.setValue(value);
                    storedParam.setInOut(StoreMode.OUT);
                    storedParam.setType(StoredParam.paramType(type));
                    SQLModule.params.add(new SqlOutParameter(param, StoredParam.paramType(type)));
                }
                    return null;
                });
                parameters.add(storedParam);
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
