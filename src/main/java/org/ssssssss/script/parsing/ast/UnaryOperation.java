package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;

public class UnaryOperation extends Expression {

    private final UnaryOperator operator;
    private final Expression operand;

    public UnaryOperation(Token operator, Expression operand) {
        super(operator.getSpan());
        this.operator = UnaryOperator.getOperator(operator);
        this.operand = operand;
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        Object operand = getOperand().evaluate(magicScript, context);

        if (getOperator() == UnaryOperator.Negate) {
            if (operand instanceof Integer) {
                return -(Integer) operand;
            } else if (operand instanceof Float) {
                return -(Float) operand;
            } else if (operand instanceof Double) {
                return -(Double) operand;
            } else if (operand instanceof Byte) {
                return -(Byte) operand;
            } else if (operand instanceof Short) {
                return -(Short) operand;
            } else if (operand instanceof Long) {
                return -(Long) operand;
            } else {
                MagicScriptError.error("Operand of operator '" + getOperator().name() + "' must be a number, got " + operand, getSpan());
                return null; // never reached
            }
        } else if (getOperator() == UnaryOperator.Not) {
            if (!(operand instanceof Boolean)) {
                MagicScriptError.error("Operand of operator '" + getOperator().name() + "' must be a boolean", getSpan());
            }
            return !(Boolean) operand;
        } else {
            return operand;
        }
    }

    public static enum UnaryOperator {
        Not, Negate, Positive;

        public static UnaryOperator getOperator(Token op) {
            if (op.getType() == TokenType.Not) {
                return UnaryOperator.Not;
            }
            if (op.getType() == TokenType.Plus) {
                return UnaryOperator.Positive;
            }
            if (op.getType() == TokenType.Minus) {
                return UnaryOperator.Negate;
            }
            MagicScriptError.error("Unknown unary operator " + op + ".", op.getSpan());
            return null; // not reached
        }
    }
}