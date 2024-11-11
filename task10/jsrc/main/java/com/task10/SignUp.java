package com.task10;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignUp {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public SignUp(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public SignUp() {
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static SignUp fromJson(String jsonString) throws JsonProcessingException {
        ObjectMapper json = new ObjectMapper();
        var jsonNode = json.readTree(jsonString);
        String email = jsonNode.get("email").asText();
        String password = jsonNode.get("password").asText();
        String firstName = jsonNode.get("firstName").asText();
        String lastName = jsonNode.get("lastName").asText();

        return new SignUp(firstName, lastName,email, password);
    }
}
