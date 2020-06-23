package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.util.List;
import java.util.function.Function;

public class LambdaFunction extends Expression{
    private List<String> parameters;
    private List<Node> childNodes;

    public LambdaFunction(Span span,List<String> parameters,List<Node> childNodes) {
        super(span);
        this.parameters = parameters;
        this.childNodes = childNodes;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        return (Function<Object[], Object>) args -> {
            context.push();
            Object value;
            try {
                for (int i = 0; i < parameters.size() && i < args.length; i++) {
                    context.setOnCurrentScope(parameters.get(i),args[i]);
                }
                value = AstInterpreter.interpretNodeList(childNodes, context);
                if(value instanceof Return.ReturnValue){
                    value = ((Return.ReturnValue)value).getValue();
                }
            } finally {
                Return.RETURN_SENTINEL.setValue(null);
            }
            context.pop();
            return value;
        };
    }
}
