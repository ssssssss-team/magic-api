package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.ast.Expression;
import org.ssssssss.script.parsing.ast.VariableAccess;
import org.ssssssss.script.parsing.ast.VariableSetter;

public class AssigmentOperation extends BinaryOperation {

	public AssigmentOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
		if (getLeftOperand() instanceof VariableSetter) {
			VariableSetter variableSetter = (VariableSetter) getLeftOperand();
			Object value = getRightOperand().evaluate(context);
			variableSetter.setValue(context, value);
			return null;
		}
		if (!(getLeftOperand() instanceof VariableAccess)) {
			MagicScriptError.error("Can only assign to top-level variables in context.", getLeftOperand().getSpan());
		}
		Object value = getRightOperand().evaluate(context);
		context.set(((VariableAccess) getLeftOperand()).getVariableName().getText(), value);
		return null;
	}
}
