package org.ssssssss.magicapi.core.model;

/**
 * 统一返回值对象
 *
 * @author mxd
 */
public class JsonBean<T> {

	/**
	 * 状态码
	 */
	private int code = 1;

	/**
	 * 状态说明
	 */
	private String message = "success";

	/**
	 * 实际数据
	 */
	private T data;

	/**
	 * 服务器时间
	 */
	private long timestamp = System.currentTimeMillis();

	private Integer executeTime;

	public JsonBean(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public JsonBean(int code, String message, T data, Integer executeTime) {
		this(code, message, data);
		this.executeTime = executeTime;
	}

	public JsonBean(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public JsonBean() {
	}

	public JsonBean(JsonCode jsonCode) {
		this(jsonCode, null);
	}

	public JsonBean(JsonCode jsonCode, T data) {
		this(jsonCode.getCode(), jsonCode.getMessage(), data);
	}

	public JsonBean(T data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(Integer executeTime) {
		this.executeTime = executeTime;
	}
}
