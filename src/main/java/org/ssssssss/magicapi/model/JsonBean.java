package org.ssssssss.magicapi.model;

/**
 * 统一返回值对象
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
     * 实际逻辑
     */
    private T data;

    /**
     * 服务器时间
     */
    private long timestamp = System.currentTimeMillis();

    public JsonBean(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonBean() {
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
