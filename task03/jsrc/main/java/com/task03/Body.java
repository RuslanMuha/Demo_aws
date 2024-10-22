package com.task03;

import java.io.Serializable;

public class Body implements Serializable {

    private Integer statusCode;
    private String message;

    public Body(int code, String message) {
        this.statusCode = code;
        this.message = message;
    }

    public static Body ok(String message) {
        return new Body(200, message);
    }

    public static Body notFound(String message) {
        return new Body(400, message);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
