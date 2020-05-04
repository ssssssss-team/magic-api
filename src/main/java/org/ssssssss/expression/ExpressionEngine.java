package org.ssssssss.expression;

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
		ArrayList<Object> list = new ArrayList<>();
		list.add("987654321");
		list.add("");
		params.put("e", list);
		Object result = engine.execute("${e.map(e->123)}", params);
		System.out.println(result);


	}
	
	public Object executeWrap(String expression, Map<String, Object> variables) {
		return execute("${" + expression + "}", variables);
	}

}
