package com.ssssssss.expression;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultExpressionEngine {
	

	public Object execute(String expression, Map<String, Object> variables) {
		ExpressionTemplateContext context = new ExpressionTemplateContext(variables);
		return ExpressionTemplate.create(expression).render(context);
	}
	
}
