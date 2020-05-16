package org.ssssssss.magicapi.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpressionEngine {

	public Object execute(String expression, Map<String, Object> variables) {
		ExpressionTemplateContext context = new ExpressionTemplateContext(variables);
		return ExpressionTemplate.create(expression).render(context);
	}

	public static void main(String[] args) {

		ExpressionEngine engine = new ExpressionEngine();

		Map<String, Object> params = new HashMap<>();
		params.put("abc", "876");
		params.put("array", new String[]{"甲", "乙"});
		ArrayList<Object> list = new ArrayList<>();
		list.add("987654321");
		list.add("yyy");
		list.add("");
		params.put("list", list);
		Object result = engine.execute("${list.filter((e,i)->i==1)}", params);
		System.out.println(result);

	}

	public Object executeWrap(String expression, Map<String, Object> variables) {
		return execute("${" + expression + "}", variables);
	}

}
