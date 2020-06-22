package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class BooleanLiteral extends Expression {
    private final Boolean value;

    public BooleanLiteral(Span literal) {
        super(literal);
        this.value = Boolean.parseBoolean(literal.getText());
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        return value;
    }
}