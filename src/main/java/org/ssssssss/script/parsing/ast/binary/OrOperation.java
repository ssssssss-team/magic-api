package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

public class OrOperation extends BinaryOperation {

	public OrOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object left = getLeftOperand().evaluate(context);
		if (!(left instanceof Boolean)) {
			MagicScriptError.error("Left operand must be a boolean, got " + left + ".", getLeftOperand().getSpan());
		}
		if ((Boolean) left) {
			return true;
		}
		Object right = getRightOperand().evaluate(context);
		if (!(right instanceof Boolean)) {
			MagicScriptError.error("Right operand must be a boolean, got " + right + ".", getRightOperand().getSpan());
		}
		return (Boolean) left || (Boolean) right;
	}
}
