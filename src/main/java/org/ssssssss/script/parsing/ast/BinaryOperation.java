package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;

import java.util.Objects;

public class BinaryOperation extends Expression {

    private final Expression leftOperand;
    private final BinaryOperator operator;
    private final Expression rightOperand;

    public BinaryOperation(Expression leftOperand, Token operator, Expression rightOperand) {
        super(operator.getSpan());
        this.leftOperand = leftOperand;
        this.operator = BinaryOperator.getOperator(operator);
        this.rightOperand = rightOperand;
    }

    public Expression getLeftOperand() {
        return leftOperand;
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public Expression getRightOperand() {
        return rightOperand;
    }

    private Object evaluateAddition(Object left, Object right) {
        if (left instanceof String || right instanceof String) {
            return left + Objects.toString(right);
        }
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() + ((Number) right).doubleValue();
        }
        if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() + ((Number) right).floatValue();
        }
        if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() + ((Number) right).longValue();
        }
        if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() + ((Number) right).intValue();
        }
        if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() + ((Number) right).shortValue();
        }
        if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() + ((Number) right).byteValue();
        }

        MagicScriptError.error("Operands for addition operator must be numbers or strings, got " + left + ", " + right + ".", getSpan());
        return null; // never reached
    }

    private Object evaluateSubtraction(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() - ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() - ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() - ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() - ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() - ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() - ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for subtraction operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private Object evaluateMultiplication(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() * ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() * ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() * ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() * ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() * ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() * ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for multiplication operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private Object evaluateDivision(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() / ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() / ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() / ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() / ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() / ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() / ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for division operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private Object evaluateModulo(Object left, Object right) {
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
            MagicScriptError.error("Operands for modulo operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private boolean evaluateLess(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() < ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() < ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() < ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() < ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() < ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() < ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for less operator must be numbers" + left + ", " + right + ".", getSpan());
            return false; // never reached
        }
    }

    private Object evaluateLessEqual(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() <= ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() <= ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() <= ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() <= ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() <= ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for less/equal operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private Object evaluateGreater(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() > ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() > ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() > ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() > ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() > ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() > ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for greater operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private Object evaluateGreaterEqual(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
        } else if (left instanceof Float || right instanceof Float) {
            return ((Number) left).floatValue() >= ((Number) right).floatValue();
        } else if (left instanceof Long || right instanceof Long) {
            return ((Number) left).longValue() >= ((Number) right).longValue();
        } else if (left instanceof Integer || right instanceof Integer) {
            return ((Number) left).intValue() >= ((Number) right).intValue();
        } else if (left instanceof Short || right instanceof Short) {
            return ((Number) left).shortValue() >= ((Number) right).shortValue();
        } else if (left instanceof Byte || right instanceof Byte) {
            return ((Number) left).byteValue() >= ((Number) right).byteValue();
        } else {
            MagicScriptError.error("Operands for greater/equal operator must be numbers" + left + ", " + right + ".", getSpan());
            return null; // never reached
        }
    }

    private Object evaluateAnd(Object left, MagicScriptContext context) {
        if (!(left instanceof Boolean)) {
            MagicScriptError.error("Left operand must be a boolean, got " + left + ".", getLeftOperand().getSpan());
        }
        if (!(Boolean) left) {
            return false;
        }
        Object right = getRightOperand().evaluate(context);
        if (!(right instanceof Boolean)) {
            MagicScriptError.error("Right operand must be a boolean, got " + right + ".", getRightOperand().getSpan());
        }
        return (Boolean) left && (Boolean) right;
    }

    private Object evaluateOr(Object left, MagicScriptContext context) {
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

    private Object evaluateXor(Object left, Object right) {
        if (!(left instanceof Boolean)) {
            MagicScriptError.error("Left operand must be a boolean, got " + left + ".", getLeftOperand().getSpan());
        }
        if (!(right instanceof Boolean)) {
            MagicScriptError.error("Right operand must be a boolean, got " + right + ".", getRightOperand().getSpan());
        }
        return (Boolean) left ^ (Boolean) right;
    }

    private Object evaluateEqual(Object left, Object right) {
        if (left != null) {
            return left.equals(right);
        }
        if (right != null) {
            return right.equals(left);
        }
        return true;
    }

    private Object evaluateNotEqual(Object left, Object right) {
        return !(Boolean) evaluateEqual(left, right);
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        if (getOperator() == BinaryOperator.Assignment) {
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

        Object left = getLeftOperand().evaluate(context);
        BinaryOperator operator = getOperator();
        Object right = operator == BinaryOperator.And || getOperator() == BinaryOperator.Or ? null : getRightOperand().evaluate(context);
        if(operator == BinaryOperator.Addition && (left instanceof String || right instanceof String)){
            return evaluateAddition(left,right);
        }
        if(operator != BinaryOperator.Equal && operator != BinaryOperator.NotEqual){
            validate(left == null, getLeftOperand().getSpan().getText() + " is null", getLeftOperand().getSpan());
            validate(right == null, getRightOperand().getSpan().getText() + " is null", getRightOperand().getSpan());
        }
        switch (getOperator()) {
            case Addition:
                return evaluateAddition(left, right);
            case Subtraction:
                return evaluateSubtraction(left, right);
            case Multiplication:
                return evaluateMultiplication(left, right);
            case Division:
                return evaluateDivision(left, right);
            case Modulo:
                return evaluateModulo(left, right);
            case Less:
                return evaluateLess(left, right);
            case LessEqual:
                return evaluateLessEqual(left, right);
            case Greater:
                return evaluateGreater(left, right);
            case GreaterEqual:
                return evaluateGreaterEqual(left, right);
            case Equal:
                return evaluateEqual(left, right);
            case NotEqual:
                return evaluateNotEqual(left, right);
            case And:
                return evaluateAnd(left, context);
            case Or:
                return evaluateOr(left, context);
            case Xor:
                return evaluateXor(left, right);
            default:
                MagicScriptError.error("Binary operator " + getOperator().name() + " not implemented", getSpan());
                return null;
        }
    }

    public static enum BinaryOperator {
        Addition, Subtraction, Multiplication, Division, Modulo, Equal, NotEqual, Less, LessEqual, Greater, GreaterEqual, And, Or, Xor, Assignment;

        public static BinaryOperator getOperator(Token op) {
            if (op.getType() == TokenType.Plus) {
                return BinaryOperator.Addition;
            }
            if (op.getType() == TokenType.Minus) {
                return BinaryOperator.Subtraction;
            }
            if (op.getType() == TokenType.Asterisk) {
                return BinaryOperator.Multiplication;
            }
            if (op.getType() == TokenType.ForwardSlash) {
                return BinaryOperator.Division;
            }
            if (op.getType() == TokenType.Percentage) {
                return BinaryOperator.Modulo;
            }
            if (op.getType() == TokenType.Equal) {
                return BinaryOperator.Equal;
            }
            if (op.getType() == TokenType.NotEqual) {
                return BinaryOperator.NotEqual;
            }
            if (op.getType() == TokenType.Less) {
                return BinaryOperator.Less;
            }
            if (op.getType() == TokenType.LessEqual) {
                return BinaryOperator.LessEqual;
            }
            if (op.getType() == TokenType.Greater) {
                return BinaryOperator.Greater;
            }
            if (op.getType() == TokenType.GreaterEqual) {
                return BinaryOperator.GreaterEqual;
            }
            if (op.getType() == TokenType.And) {
                return BinaryOperator.And;
            }
            if (op.getType() == TokenType.Or) {
                return BinaryOperator.Or;
            }
            if (op.getType() == TokenType.Xor) {
                return BinaryOperator.Xor;
            }
            if (op.getType() == TokenType.Assignment) {
                return BinaryOperator.Assignment;
            }
            MagicScriptError.error("Unknown binary operator " + op + ".", op.getSpan());
            return null; // not reached
        }
    }
}