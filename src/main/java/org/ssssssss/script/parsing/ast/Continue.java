package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class Continue extends Node {
    public static final Object CONTINUE_SENTINEL = new Object();

    public Continue(Span span) {
        super(span);
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return CONTINUE_SENTINEL;
    }
}