package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public class CharacterLiteral extends Expression {
    private final Character value;

    public CharacterLiteral(Span literal) {
        super(literal);

        String text = literal.getText();
        if (text.length() > 3) {
            if (text.charAt(2) == 'n') {
                value = '\n';
            } else if (text.charAt(2) == 'r') {
                value = '\r';
            } else if (text.charAt(2) == 't') {
                value = '\t';
            } else if (text.charAt(2) == '\\') {
                value = '\\';
            } else if (text.charAt(2) == '\'') {
                value = '\'';
            } else {
                MagicScriptError.error("Unknown escape sequence '" + literal.getText() + "'.", literal);
                value = 0; // never reached
            }
        } else {
            this.value = literal.getText().charAt(1);
        }
    }

    public Character getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return value;
    }
}