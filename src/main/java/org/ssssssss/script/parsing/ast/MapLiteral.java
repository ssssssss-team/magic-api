package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.Token;
import org.ssssssss.script.parsing.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapLiteral extends Expression {
    private final List<Token> keys;
    private final List<Expression> values;

    public MapLiteral(Span span, List<Token> keys, List<Expression> values) {
        super(span);
        this.keys = keys;
        this.values = values;
    }

    @Override
    public Object evaluate(MagicScriptContext context) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0, n = keys.size(); i < n; i++) {
            Object value = values.get(i).evaluate(context);
            Token tokenKey = keys.get(i);
            String key = tokenKey.getSpan().getText();
            if (tokenKey.getType() == TokenType.StringLiteral) {
                key = (String) new StringLiteral(tokenKey.getSpan()).evaluate(context);
            } else if (key != null && key.startsWith("$")) {
                Object objKey = context.get(key.substring(1));
                if (objKey != null) {
                    key = objKey.toString();
                } else {
                    key = null;
                }
            }
            map.put(key, value);
        }
        return map;
    }
}