package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class DoubleLiteral extends Expression {
    private final Double value;

    public DoubleLiteral(Span literal) {
        super(literal);
        this.value = Double.parseDouble(literal.getText().substring(0, literal.getText().length() - 1));
    }

    public Double getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}