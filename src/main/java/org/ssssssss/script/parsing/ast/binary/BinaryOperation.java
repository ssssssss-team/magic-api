package org.ssssssss.script.parsing.ast.binary;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.ast.Expression;

public abstract class BinaryOperation extends Expression {

	private Expression leftOperand;
	private Expression rightOperand;

	public BinaryOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(span);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public static Expression create(Expression left, Token operator, Expression right) {
		Expression expression = null;
		Span span = operator.getSpan();
		switch (operator.getType()) {
			case Assignment:
				expression = new AssigmentOperation(left, span, right);
				break;
			case Plus:
				expression = new AddOperation(left, span, right);
				break;
			case Minus:
				expression = new SubtractionOperation(left, span, right);
				break;
			case Asterisk:
				expression = new MultiplicationOperation(left, span, right);
				break;
			case ForwardSlash:
				expression = new DivisionOperation(left, span, right);
				break;
			case Percentage:
				expression = new ModuloOperation(left, span, right);
				break;
			case Less:
				expression = new LessOperation(left, span, right);
				break;
			case LessEqual:
				expression = new LessEqualOperation(left, span, right);
				break;
			case Greater:
				expression = new GreaterOperation(left, span, right);
				break;
			case GreaterEqual:
				expression = new GreaterEqualOperation(left, span, right);
				break;
			case Equal:
				expression = new EqualOperation(left, span, right);
				break;
			case NotEqual:
				expression = new NotEqualOperation(left, span, right);
				break;
			case And:
				expression = new AndOperation(left, span, right);
				break;
			case Or:
				expression = new OrOperation(left, span, right);
				break;
			default:
				MagicScriptError.error("Binary operator " + operator + " not implemented", span);
		}
		return expression;
	}

	public Expression getLeftOperand() {
		return leftOperand;
	}

	public Expression getRightOperand() {
		return rightOperand;
	}

}
