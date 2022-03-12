package org.ssssssss.magicapi.core.model;

import org.apache.commons.lang3.time.DateUtils;
import org.ssssssss.magicapi.modules.servlet.RequestModule;
import org.ssssssss.script.reflection.JavaInvoker;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;

import static org.ssssssss.script.reflection.JavaReflection.findInvoker;

/**
 * 参数类型枚举
 *
 * @author mxd
 */
public enum DataType {

	/**
	 * Object 类型
	 */
	Object("object"),

	/**
	 * 数组类型
	 */
	Array("array"),

	/**
	 * 任意类型
	 */
	Any("object"),

	/**
	 * Boolean类型
	 */
	Boolean(false, findInvoker(Boolean.class, "valueOf", new Class<?>[]{String.class}), false, true, "boolean"),

	/**
	 * String 类型
	 */
	String("string"),

	/**
	 * Integer 类型
	 */
	Integer(true, findInvoker(BigDecimal.class, "intValue"), "number"),

	/**
	 * Double 类型
	 */
	Double(true, findInvoker(BigDecimal.class, "doubleValue"), "number"),
	/**
	 * Long 类型
	 */
	Long(true, findInvoker(BigDecimal.class, "longValue"), "number"),
	/**
	 * Float 类型
	 */
	Float(true, findInvoker(BigDecimal.class, "floatValue"), "number"),
	/**
	 * Byte 类型
	 */
	Byte(true, findInvoker(BigDecimal.class, "byteValue"), "number"),

	/**
	 * Short类型
	 */
	Short(true, findInvoker(BigDecimal.class, "shortValue"), "number"),

	/**
	 * Date类型
	 */
	Date(findInvoker(DataType.class, "parseDate", new Class<?>[]{String.class}), false, true, "string"),

	/**
	 * MultipartFile 类型
	 */
	MultipartFile(findInvoker(RequestModule.class, "getFile", new Class<?>[]{String.class}), true, false, "file"),

	/**
	 * List<MultipartFile> 类型
	 */
	MultipartFiles(findInvoker(RequestModule.class, "getFiles", new Class<?>[]{String.class}), true, false, "file");


	private boolean isNumber;

	private JavaInvoker<Method> invoker;

	private boolean needName;

	private boolean needValue;

	private String javascriptType;

	public static String[] DATE_PATTERNS;

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

	public static java.util.Date parseDate(String value) throws ParseException {
		return DateUtils.parseDate(value, DATE_PATTERNS);
	}
}
