package com.task02;

public class Body {

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


}
