package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostReservationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AuthHandler authHandler = new AuthHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDB;

    public PostReservationHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        String email = authHandler.getCurrentuser(requestEvent);
        if (email == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(403);
        }
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(requestEvent.getBody());
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);

        }

        System.out.println("email: " + email);

        int tableNumber = rootNode.get("tableNumber").asInt();
        boolean isTableExist = checkIfTableExistsByNumber(tableNumber);

        if(!isTableExist) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }

        String reservationDate = rootNode.get("date").asText();
        String startReservation = rootNode.get("slotTimeStart").asText();
        String endReservation = rootNode.get("slotTimeEnd").asText();
        boolean isReservationConflict = checkReservationConflict(tableNumber, reservationDate, startReservation, endReservation);

        if(isReservationConflict) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }

        Map<String, AttributeValue> itemMap = new HashMap<>();

        itemMap.put("email", new AttributeValue(email));
        String reservationId = UUID.randomUUID().toString();
        itemMap.put("id", new AttributeValue(reservationId));
        itemMap.put("tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
        itemMap.put("clientName", new AttributeValue(rootNode.get("clientName").asText()));
        itemMap.put("phoneNumber", new AttributeValue(rootNode.get("phoneNumber").asText()));
        itemMap.put("date", new AttributeValue(reservationDate));
        itemMap.put("slotTimeStart", new AttributeValue(startReservation));
        itemMap.put("slotTimeEnd", new AttributeValue(endReservation));


        System.out.println("---Before put to reservations_table log----");
        itemMap.forEach((k, v) -> {
            System.out.println("fieldName: " + k);
            System.out.println("value: " + v);
        });

        dynamoDB.putItem(new PutItemRequest().withTableName(System.getenv("reservations_table")).withItem(itemMap));

        HashMap<String, String> response = new HashMap<>();
        response.put("reservationId", reservationId);

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }
    }

    public boolean checkIfTableExistsByNumber(int number) {
        // Create a map to store the key condition for the query
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":number", new AttributeValue().withN(String.valueOf(number)));

        // Build the query request
        QueryRequest queryRequest = new QueryRequest()
                .withTableName(System.getenv("tables_table"))
                .withKeyConditionExpression("number = :number")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withLimit(1); // Only need to check if at least one item exists

        // Execute the query
        QueryResult result = dynamoDB.query(queryRequest);

        // Check if any items were returned
        return !result.getItems().isEmpty();
    }

    public boolean checkReservationConflict(int tableNumber, String date, String slotTimeStart, String slotTimeEnd) {
        // Define the query parameters
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
        expressionAttributeValues.put(":date", new AttributeValue(date));
        expressionAttributeValues.put(":slotTimeStart", new AttributeValue(slotTimeStart));
        expressionAttributeValues.put(":slotTimeEnd", new AttributeValue(slotTimeEnd));

        // Construct the filter expression for overlapping time slots on the same date
        String filterExpression = "date = :date AND " +
                "((slotTimeStart < :slotTimeEnd AND slotTimeEnd > :slotTimeStart))";

        // Build the query request
        QueryRequest queryRequest = new QueryRequest()
                .withTableName(System.getenv("reservations_table")) // Use the correct table environment variable
                .withKeyConditionExpression("tableNumber = :tableNumber")
                .withFilterExpression(filterExpression)
                .withExpressionAttributeValues(expressionAttributeValues);

        // Execute the query
        QueryResult queryResult = dynamoDB.query(queryRequest);

        // If any items are returned, a conflict exists
        return !queryResult.getItems().isEmpty();
    }
}
