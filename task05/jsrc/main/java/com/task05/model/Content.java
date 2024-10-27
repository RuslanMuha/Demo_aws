package com.task05.model;

import java.io.Serializable;

public class Content implements Serializable {

    private String param1;
    private String param2;

    // Default constructor
    public Content() {}

    @Override
    public String toString() {
        return "Content{" +
                "name='" + param1 + '\'' +
                ", surname='" + param2 + '\'' +
                '}';
    }

    public Content(String param1, String param2) {
        this.param1 = param1;
        this.param2 = param2;
    }


    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }
}
