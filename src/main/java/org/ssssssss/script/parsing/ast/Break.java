package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class Break extends Node {
    public static final Object BREAK_SENTINEL = new Object();

    public Break(Span span) {
        super(span);
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return BREAK_SENTINEL;
    }
}