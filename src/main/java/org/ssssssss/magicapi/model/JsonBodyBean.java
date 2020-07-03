package org.ssssssss.magicapi.model;

public class JsonBodyBean<T> extends JsonBean<T> {

	private Object body;

	public JsonBodyBean(int code, String message, T data, Object body) {
		super(code, message, data);
		this.body = body;
	}

	public JsonBodyBean(T data, Object body) {
		super(data);
		this.body = body;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
}
