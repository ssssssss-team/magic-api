package org.ssssssss.magicapi.model;

import org.ssssssss.magicapi.modules.RequestModule;
import org.ssssssss.script.reflection.JavaInvoker;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.ssssssss.script.reflection.JavaReflection.findInvoker;

public enum DataType {
	String,
	Integer(true, findInvoker(BigDecimal.class, "intValue")),
	Double(true, findInvoker(BigDecimal.class, "doubleValue")),
	Long(true, findInvoker(BigDecimal.class, "longValue")),
	Float(true, findInvoker(BigDecimal.class, "floatValue")),
	Byte(true, findInvoker(BigDecimal.class, "byteValue")),
	Short(true, findInvoker(BigDecimal.class, "shortValue")),
	MultipartFile(findInvoker(RequestModule.class, "getFile", new Class<?>[]{String.class}), true, false),
	MultipartFiles(findInvoker(RequestModule.class, "getFiles", new Class<?>[]{String.class}), true, false);

	private boolean isNumber;

	private JavaInvoker<Method> invoker;

	private boolean needName;

	private boolean needValue;

	DataType(boolean isNumber, JavaInvoker<Method> invoker, boolean needName, boolean needValue) {
		this.isNumber = isNumber;
		this.invoker = invoker;
		this.needName = needName;
		this.needValue = needValue;
	}

	DataType(JavaInvoker<Method> invoker, boolean needName, boolean needValue) {
		this.invoker = invoker;
		this.needName = needName;
		this.needValue = needValue;
	}

	DataType(boolean isNumber, JavaInvoker<Method> invoker) {
		this.isNumber = isNumber;
		this.invoker = invoker;
	}

	DataType() {
	}

	public boolean isNumber() {
		return isNumber;
	}

	public JavaInvoker<Method> getInvoker() {
		return invoker;
	}

	public boolean isNeedName() {
		return needName;
	}

	public boolean isNeedValue() {
		return needValue;
	}
}
