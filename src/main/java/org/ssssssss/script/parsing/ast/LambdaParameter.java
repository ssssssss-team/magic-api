package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class LambdaParameter extends Expression {
    public LambdaParameter(Span span) {
        super(span);
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        return null;
    }
}
