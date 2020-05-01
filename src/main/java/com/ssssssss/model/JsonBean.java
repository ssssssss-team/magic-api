package com.ssssssss.model;

public class JsonBean<T> {

    private int code = 1;

    private String message = "success";

    private T data;

    private long timestamp = System.currentTimeMillis();

    public JsonBean(int code, String message) {
        this.code = code;
        this.message = message;
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
}
