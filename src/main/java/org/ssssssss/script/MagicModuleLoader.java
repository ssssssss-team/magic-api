package org.ssssssss.script;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MagicModuleLoader {

	private static Map<String,Object> modules = new ConcurrentHashMap<>();

	private static Function<String,Object> classLoader = (className)->{
		try{
			return Class.forName(className);
		}catch(Exception e){
			return null;
		}
	};

	public static void setClassLoader(Function<String,Object> classLoader){
		MagicModuleLoader.classLoader = classLoader;
	}

	public static void addModule(String moduleName, Object target){
		modules.put(moduleName, target);
	}

	public static Object loadModule(String moduleName){
		return modules.get(moduleName);
	}

	public static Object loadClass(String className){
		return classLoader.apply(className);
	}
}
