package org.ssssssss.magicapi.expression;

import java.util.*;

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
		params.put("test", new TestClass());
		params.put("cc", null);


		Object result = engine.execute("${test.test(cc)}", params);
		System.out.println(result);

	}
	public static class TestClass {

		public String test(String list) {
			return "String";
		}
		public String test(List list) {
			return "List";
		}
		public String test(Object list) {
			return "Object";
		}


	}


	public Object executeWrap(String expression, Map<String, Object> variables) {
		return execute("${" + expression + "}", variables);
	}

}
