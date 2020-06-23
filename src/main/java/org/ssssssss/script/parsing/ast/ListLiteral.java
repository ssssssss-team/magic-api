package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

import java.util.ArrayList;
import java.util.List;

public class ListLiteral extends Expression {
    public final List<Expression> values;

    public ListLiteral(Span span, List<Expression> values) {
        super(span);
        this.values = values;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        List<Object> list = new ArrayList<>();
        for (int i = 0, n = values.size(); i < n; i++) {
            list.add(values.get(i).evaluate(context));
        }
        return list;
    }
}