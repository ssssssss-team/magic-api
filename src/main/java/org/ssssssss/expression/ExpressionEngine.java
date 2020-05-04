package org.ssssssss.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpressionEngine {

	public Object execute(String expression, Map<String, Object> variables) {
		ExpressionTemplateContext context = new ExpressionTemplateContext(variables);
		return ExpressionTemplate.create(expression).render(context);
	}


	public Object executeWrap(String expression, Map<String, Object> variables) {
		return execute("${" + expression + "}", variables);
	}

}
