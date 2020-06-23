package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class NullLiteral extends Expression {
    public NullLiteral(Span span) {
        super(span);
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return null;
    }
}