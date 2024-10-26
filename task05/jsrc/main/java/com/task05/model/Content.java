package com.task05.model;

import java.io.Serializable;

public class Content implements Serializable {

    private String name;
    private String surname;

    // Default constructor
    public Content() {}

    @Override
    public String toString() {
        return "Content{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }

    public Content(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
