package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class IntegerLiteral extends Expression {
    private final Integer value;

    public IntegerLiteral(Span literal) {
        super(literal);
        this.value = Integer.parseInt(literal.getText());
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}