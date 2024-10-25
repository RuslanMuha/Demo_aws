package com.task05.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class MapToJsonConverter implements DynamoDBTypeConverter<String, Map> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convert(Map object) {
        try {
            return objectMapper.writeValueAsString(object); // Convert Map to JSON string
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert Map to JSON", e);
        }
    }

    @Override
    public Map unconvert(String object) {
        try {
            return objectMapper.readValue(object, Map.class); // Convert JSON string back to Map
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to Map", e);
        }
    }
}
