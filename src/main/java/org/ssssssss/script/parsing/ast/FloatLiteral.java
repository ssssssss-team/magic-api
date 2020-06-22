package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class FloatLiteral extends Expression {
    private final Float value;

    public FloatLiteral(Span literal) {
        super(literal);
        String text = literal.getText();
        if (text.charAt(text.length() - 1) == 'f') {
            text = text.substring(0, text.length() - 1);
        }
        this.value = Float.parseFloat(text);
    }

    public Float getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        return value;
    }
}