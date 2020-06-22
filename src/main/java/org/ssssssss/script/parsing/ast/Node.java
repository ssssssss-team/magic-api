package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public abstract class Node {
    private final Span span;

    public Node(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

    @Override
    public String toString() {
        return span.getText();
    }

    public abstract Object evaluate(MagicScript magicScript, MagicScriptContext context);

    protected void validate(boolean value, String message, Span location) {
        if (value) {
            MagicScriptError.error(message, location);
        }
    }
}