package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.CharacterStream;
import org.ssssssss.script.parsing.Span;

public class StringLiteral extends Expression {
    private final String value;

    public StringLiteral(Span literal) {
        super(literal);
        String text = getSpan().getText();
        String unescapedValue = text.substring(1, text.length() - 1);
        StringBuilder builder = new StringBuilder();

        CharacterStream stream = new CharacterStream(unescapedValue);
        while (stream.hasMore()) {
            if (stream.match("\\\\", true)) {
                builder.append('\\');
            } else if (stream.match("\\n", true)) {
                builder.append('\n');
            } else if (stream.match("\\r", true)) {
                builder.append('\r');
            } else if (stream.match("\\t", true)) {
                builder.append('\t');
            } else if (stream.match("\\\"", true)) {
                builder.append('"');
            } else {
                builder.append(stream.consume());
            }
        }
        value = builder.toString();
    }

    /**
     * Returns the literal without quotes
     **/
    public String getValue() {
        return value;
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        return value;
    }
}