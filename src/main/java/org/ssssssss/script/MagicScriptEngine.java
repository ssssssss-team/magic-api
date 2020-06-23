package org.ssssssss.script;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class MagicScriptEngine {

	private static Map<String, Object> defaultImports = new ConcurrentHashMap<>();

	static{
        addDefaultImport("range",(BiFunction<Integer, Integer, Iterator<Integer>>) (from, to) -> new Iterator<Integer>() {
            int idx = from;

            public boolean hasNext() {
                return idx <= to;
            }

            public Integer next() {
                return idx++;
            }
        });
    }

	public static void addDefaultImport(String name, Object target) {
		defaultImports.put(name, target);
	}

	public static Object execute(String script, Map<String, Object> variables) {
		MagicScriptContext context = new MagicScriptContext(variables);
        Iterator<Map.Entry<String, Object>> iterator = defaultImports.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            context.set(entry.getKey(),entry.getValue());
        }
        return MagicScript.create(script).execute(context);
	}
}
