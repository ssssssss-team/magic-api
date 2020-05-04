
package org.ssssssss.expression.interpreter;

import org.ssssssss.expression.ExpressionError;
import org.ssssssss.expression.ExpressionError.TemplateException;
import org.ssssssss.expression.ExpressionTemplate;
import org.ssssssss.expression.ExpressionTemplateContext;
import org.ssssssss.expression.parsing.Ast;
import org.ssssssss.expression.parsing.Ast.Node;
import org.ssssssss.expression.parsing.Ast.Text;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * Interprets a Template given a TemplateContext to lookup variable values in and writes the evaluation results to an output
 * stream. Uses the global {@link AbstractReflection} instance as returned by {@link AbstractReflection#getInstance()} to access members and call
 * methods.
 * </p>
 *
 * <p>
 * The interpeter traverses the AST as stored in {@link ExpressionTemplate#getNodes()}. the interpeter has a method for each AST node type
 * (see {@link Ast} that evaluates that node. A node may return a value, to be used in the interpretation of a parent node or to
 * be written to the output stream.
 * </p>
 **/
public class AstInterpreter {
	public static Object interpret (ExpressionTemplate template, ExpressionTemplateContext context) {
		try {
			return interpretNodeList(template.getNodes(), template, context);
		} catch (Throwable t) {
			if (t instanceof TemplateException) {
                throw (TemplateException)t;
            } else {
				ExpressionError.error("执行表达式出错 " + t.getMessage(), template.getNodes().get(0).getSpan(),t);
				return null; // never reached
			}
		} 
	}

	public static Object interpretNodeList (List<Node> nodes, ExpressionTemplate template, ExpressionTemplateContext context) throws IOException {
		String result = "";
		for (int i = 0, n = nodes.size(); i < n; i++) {
			Node node = nodes.get(i);
			Object value = node.evaluate(template, context);
			if(node instanceof Text){
				result += node.getSpan().getText();
			}else if(value == null){
				if(i ==	 0 && i + 1 == n){
					return null;
				}
				result += "null";
			}else {
				if(i ==0 && i + 1 ==n){
					return value;
				}
				result += value;
			}
		}
		return result;
	}
}
