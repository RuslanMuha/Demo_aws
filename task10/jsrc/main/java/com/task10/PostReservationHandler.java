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
        System.out.println("isTableExist: " + isTableExist);
        if (!isTableExist) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }

        String reservationDate = rootNode.get("date").asText();
        String startReservation = rootNode.get("slotTimeStart").asText();
        String endReservation = rootNode.get("slotTimeEnd").asText();
        boolean isReservationConflict = checkReservationConflict(tableNumber, reservationDate, startReservation, endReservation);
        System.out.println("isReservationConflict: " + isReservationConflict);

        if (isReservationConflict) {
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
        // Define the filter expression to match the number
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#tableNumber", "tableNumber");

        System.out.println("checkIfTableExistsByNumber: " + number);

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":filterTableNumber", new AttributeValue().withN(String.valueOf(number)));

        // Build the scan request with a filter expression
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(System.getenv("tables_table"))
                .withFilterExpression("#tableNumber = :filterTableNumber")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        // Execute the scan
        ScanResult result = dynamoDB.scan(scanRequest);

        // Check if any items were returned
        return result.getCount() > 0;
    }

    public boolean checkReservationConflict(int tableNumber, String date, String slotTimeStart, String slotTimeEnd) {
        // Define the filter expression to match the tableNumber, date, and overlapping time slots
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#tableNumber", "tableNumber");
        expressionAttributeNames.put("#date", "date");
        expressionAttributeNames.put("#slotTimeStart", "slotTimeStart");
        expressionAttributeNames.put("#slotTimeEnd", "slotTimeEnd");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
        expressionAttributeValues.put(":date", new AttributeValue(date));
        expressionAttributeValues.put(":slotTimeStart", new AttributeValue(slotTimeStart));
        expressionAttributeValues.put(":slotTimeEnd", new AttributeValue(slotTimeEnd));

        // Define the filter expression to check for overlapping reservations
        String filterExpression = "#tableNumber = :tableNumber AND #date = :date AND " +
                "#slotTimeStart < :slotTimeEnd AND #slotTimeEnd > :slotTimeStart";

        // Build the scan request with a filter expression
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(System.getenv("reservations_table"))
                .withFilterExpression(filterExpression)
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        // Execute the scan
        ScanResult result = dynamoDB.scan(scanRequest);

        // Check if any items were returned, indicating a conflict
        return result.getCount() > 0;
    }
}
