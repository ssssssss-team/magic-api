package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.util.List;

public class IfStatement extends Node {
    private final Expression condition;
    private final List<Node> trueBlock;
    private final List<IfStatement> elseIfs;
    private final List<Node> falseBlock;

    public IfStatement(Span span, Expression condition, List<Node> trueBlock, List<IfStatement> elseIfs, List<Node> falseBlock) {
        super(span);
        this.condition = condition;
        this.trueBlock = trueBlock;
        this.elseIfs = elseIfs;
        this.falseBlock = falseBlock;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Node> getTrueBlock() {
        return trueBlock;
    }

    public List<IfStatement> getElseIfs() {
        return elseIfs;
    }

    public List<Node> getFalseBlock() {
        return falseBlock;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        Object condition = getCondition().evaluate(context);
        if (!(condition instanceof Boolean))
            MagicScriptError.error("Expected a condition evaluating to a boolean, got " + condition, getCondition().getSpan());
        if ((Boolean) condition) {
            context.push();
            Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getTrueBlock(), context);
            context.pop();
            return breakOrContinueOrReturn;
        }

        if (getElseIfs().size() > 0) {
            for (IfStatement elseIf : getElseIfs()) {
                condition = elseIf.getCondition().evaluate(context);
                if (!(condition instanceof Boolean))
                    MagicScriptError.error("Expected a condition evaluating to a boolean, got " + condition, elseIf.getCondition().getSpan());
                if ((Boolean) condition) {
                    context.push();
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(elseIf.getTrueBlock(), context);
                    context.pop();
                    return breakOrContinueOrReturn;
                }
            }
        }

        if (getFalseBlock().size() > 0) {
            context.push();
            Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getFalseBlock(), context);
            context.pop();
            return breakOrContinueOrReturn;
        }
        return null;
    }
}