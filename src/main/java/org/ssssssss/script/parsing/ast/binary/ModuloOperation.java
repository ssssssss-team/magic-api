package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;

public class ModuloOperation extends BinaryOperation {

	public ModuloOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		Object left = getLeftOperand().evaluate(context);
		if (left == null) {
			MagicScriptError.error(getLeftOperand().getSpan().getText() + " is undefined.", getLeftOperand().getSpan());
		}
		Object right = getRightOperand().evaluate(context);
		if (right == null) {
			MagicScriptError.error(getRightOperand().getSpan().getText() + " is undefined.", getRightOperand().getSpan());
		}
		if (left instanceof Double || right instanceof Double) {
			return ((Number) left).doubleValue() % ((Number) right).doubleValue();
		} else if (left instanceof Float || right instanceof Float) {
			return ((Number) left).floatValue() % ((Number) right).floatValue();
		} else if (left instanceof Long || right instanceof Long) {
			return ((Number) left).longValue() % ((Number) right).longValue();
		} else if (left instanceof Integer || right instanceof Integer) {
			return ((Number) left).intValue() % ((Number) right).intValue();
		} else if (left instanceof Short || right instanceof Short) {
			return ((Number) left).shortValue() % ((Number) right).shortValue();
		} else if (left instanceof Byte || right instanceof Byte) {
			return ((Number) left).byteValue() % ((Number) right).byteValue();
		} else {
			MagicScriptError.error("Operands for % operator must be numbers" + left + ", " + right + ".", getSpan());
			return null; // never reached
		}
	}
}
