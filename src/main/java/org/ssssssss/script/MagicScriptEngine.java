package org.ssssssss.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.script.exception.DebugTimeoutException;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class MagicScriptEngine {

	private static Map<String, Object> defaultImports = new ConcurrentHashMap<>();

	private static ExecutorService service = Executors.newCachedThreadPool();

	private static Logger logger = LoggerFactory.getLogger(MagicScriptEngine.class);

	static {
		addDefaultImport("range", (BiFunction<Integer, Integer, Iterator<Integer>>) (from, to) -> new Iterator<Integer>() {
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

	public static Object execute(String script, MagicScriptContext context) {
		Iterator<Map.Entry<String, Object>> iterator = defaultImports.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			context.set(entry.getKey(), entry.getValue());
		}
		MagicScript magicScript = MagicScript.create(script);
		if (context instanceof MagicScriptDebugContext) {
			MagicScriptDebugContext debugContext = (MagicScriptDebugContext) context;
			service.submit(() -> {
				try {
					debugContext.setReturnValue(magicScript.execute(debugContext));
				} catch (Exception e) {
					debugContext.setException(true);
					debugContext.setReturnValue(e);
				}
			});
			try {
				debugContext.await();
			} catch (InterruptedException e) {
				throw new DebugTimeoutException(e);
			}
			return debugContext.isRunning() ? debugContext.getDebugInfo() : debugContext.getReturnValue();
		}
		return magicScript.execute(context);
	}
}
