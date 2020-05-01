package com.ssssssss.expression;

<<<<<<< HEAD:src/main/java/com/ssssssss/expression/DefaultExpressionEngine.java
import org.springframework.stereotype.Component;

import java.util.HashMap;
=======
>>>>>>> bea407944d46c13c7683b957cafd4061088611a7:src/main/java/com/ssssssss/expression/ExpressionEngine.java
import java.util.Map;

public class ExpressionEngine {

	public Object execute(String expression, Map<String, Object> variables) {
		ExpressionTemplateContext context = new ExpressionTemplateContext(variables);
		return ExpressionTemplate.create(expression).render(context);
	}

<<<<<<< HEAD:src/main/java/com/ssssssss/expression/DefaultExpressionEngine.java
	public static void main(String[] args) {

		DefaultExpressionEngine engine = new DefaultExpressionEngine();

		Map<String, Object> params = new HashMap<>();
		params.put("abc", "");
		engine.execute("${}", params);


	}
	
=======
	public Object executeWrap(String expression, Map<String, Object> variables) {
		return execute("${" + expression + "}", variables);
	}

>>>>>>> bea407944d46c13c7683b957cafd4061088611a7:src/main/java/com/ssssssss/expression/ExpressionEngine.java
}
