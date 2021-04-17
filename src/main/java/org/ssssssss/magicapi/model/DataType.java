package org.ssssssss.magicapi.model;

import org.ssssssss.magicapi.modules.RequestModule;
import org.ssssssss.script.reflection.JavaInvoker;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.ssssssss.script.reflection.JavaReflection.findInvoker;

public enum DataType {
	String("string"),
	Integer(true, findInvoker(BigDecimal.class, "intValue"), "number"),
	Double(true, findInvoker(BigDecimal.class, "doubleValue"), "number"),
	Long(true, findInvoker(BigDecimal.class, "longValue"), "number"),
	Float(true, findInvoker(BigDecimal.class, "floatValue"), "number"),
	Byte(true, findInvoker(BigDecimal.class, "byteValue"), "number"),
	Short(true, findInvoker(BigDecimal.class, "shortValue"), "number"),
	MultipartFile(findInvoker(RequestModule.class, "getFile", new Class<?>[]{String.class}), true, false, "file"),
	MultipartFiles(findInvoker(RequestModule.class, "getFiles", new Class<?>[]{String.class}), true, false, "file");

	private boolean isNumber;

	private JavaInvoker<Method> invoker;

	private boolean needName;

	private boolean needValue;

	private String javascriptType;

	DataType(boolean isNumber, JavaInvoker<Method> invoker, boolean needName, boolean needValue, String javascriptType) {
		this.isNumber = isNumber;
		this.invoker = invoker;
		this.needName = needName;
		this.needValue = needValue;
		this.javascriptType = javascriptType;
	}

	DataType(JavaInvoker<Method> invoker, boolean needName, boolean needValue, String javascriptType) {
		this(false, invoker, needName, needValue, javascriptType);
	}

	DataType(boolean isNumber, JavaInvoker<Method> invoker, String javascriptType) {
		this(invoker, false, false, javascriptType);
		this.isNumber = isNumber;
	}

	DataType(String javascriptType) {
		this.javascriptType = javascriptType;
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

	public java.lang.String getJavascriptType() {
		return javascriptType;
	}
}
