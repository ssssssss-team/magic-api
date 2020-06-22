package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

import java.util.List;

public class FunctionStatement extends Expression {

    private List<Node> childNodes;

    private List<String> parameters;

    public FunctionStatement(Span span, List<Node> childNodes, List<String> parameters) {
        super(span);
        this.childNodes = childNodes;
        this.parameters = parameters;
    }

    public List<Node> getChildNodes() {
        return childNodes;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        return this;
    }
}
