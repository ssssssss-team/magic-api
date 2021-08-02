package org.ssssssss.magicapi.utils;

import org.ssssssss.script.reflection.JavaInvoker;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * TODO 此类兼容ASM分支处理，待切换至ASM分支时删除。
 */
public class Invoker {

	private final JavaInvoker<?> target;

	private static final Method invokeMethod;

	static {
		invokeMethod = Arrays.stream(JavaInvoker.class.getDeclaredMethods())
				.filter(it -> "invoke0".equals(it.getName()))
				.findFirst()
				.orElse(null);
	}

	private Invoker(JavaInvoker<?> target) {
		this.target = target;
	}

	public Class<?>[] getParameterTypes(){
		return this.target.getParameterTypes();
	}

	public static Invoker from(JavaInvoker<?> target) {
		return target == null ? null : new Invoker(target);
	}

	public Object invoke(Object target, Object context, Object ... args) throws Throwable{
		return invokeMethod.invoke(this.target, target, context, args);
	}
}
