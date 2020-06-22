package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class VariableAccess extends Expression {
    public VariableAccess(Span name) {
        super(name);
    }

    public Span getVariableName() {
        return getSpan();
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        return context.get(getSpan().getText());
    }
}