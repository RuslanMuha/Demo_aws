package com.task10;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignIn {
    private String email;

    private String password;

    public String getEmail() {
        return email;
    }

    public SignIn(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public SignIn() {
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

    public static SignIn fromJson(String jsonString) throws JsonProcessingException {
        ObjectMapper json = new ObjectMapper();
        JsonNode jsonNode = json.readTree(jsonString);
        String email = jsonNode.get("email").asText();
        String password = jsonNode.get("password").asText();

        return new SignIn(email, password);
    }
}
