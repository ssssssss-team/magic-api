package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

public class Import extends Node {

    private String packageName;

    private String variableName;

    public Import(Span span, String packageName, String variableName) {
        super(span);
        this.packageName = packageName;
        this.variableName = variableName;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        try {
            context.set(variableName, Class.forName(packageName));
        } catch (ClassNotFoundException e) {
            MagicScriptError.error(packageName + " not found.", getSpan(), e);
        }
        return null;
    }
}
