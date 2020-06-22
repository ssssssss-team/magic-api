package org.ssssssss.script;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

public class MagicScriptEngine {

    public Object evaluate(String expression, Map<String, Object> variables) {
        MagicScriptContext context = new MagicScriptContext(variables);
        context.set("range", (BiFunction<Integer, Integer, Iterator<Integer>>) (from, to) -> new Iterator<Integer>() {
            int idx = from;

            public boolean hasNext() {
                return idx <= to;
            }

            public Integer next() {
                return idx++;
            }
        });
        return MagicScript.create(expression).evaluate(context);
    }
}
