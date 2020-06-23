package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public class TernaryOperation extends Expression {
    private final Expression condition;
    private final Expression trueExpression;
    private final Expression falseExpression;

    public TernaryOperation(Expression condition, Expression trueExpression, Expression falseExpression) {
        super(new Span(condition.getSpan(), falseExpression.getSpan()));
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getTrueExpression() {
        return trueExpression;
    }

    public Expression getFalseExpression() {
        return falseExpression;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        Object condition = getCondition().evaluate(context);
        if (!(condition instanceof Boolean)) {
            MagicScriptError.error("Condition of ternary operator must be a boolean, got " + condition + ".", getSpan());
        }
        return ((Boolean) condition) ? getTrueExpression().evaluate(context) : getFalseExpression().evaluate(context);
    }
}