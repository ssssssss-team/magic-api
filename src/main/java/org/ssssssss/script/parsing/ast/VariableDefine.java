package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class VariableDefine extends Node {

    private Expression right;

    private String variableName;

    public VariableDefine(Span span, String variableName, Expression right) {
        super(span);
        this.variableName = variableName;
        this.right = right;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        context.set(variableName, right.evaluate(context));
        return null;
    }
}
