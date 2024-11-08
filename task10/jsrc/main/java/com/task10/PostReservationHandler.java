package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
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

        Map<String, AttributeValue> itemMap = new HashMap<>();

        itemMap.put("email", new AttributeValue(email));
        String reservationId = UUID.randomUUID().toString();
        itemMap.put("id", new AttributeValue(reservationId));
        itemMap.put("tableNumber", new AttributeValue().withN(String.valueOf(rootNode.get("tableNumber").asInt())));
        itemMap.put("clientName", new AttributeValue(rootNode.get("clientName").asText()));
        itemMap.put("phoneNumber", new AttributeValue(rootNode.get("phoneNumber").asText()));
        itemMap.put("date", new AttributeValue(rootNode.get("date").asText()));
        itemMap.put("slotTimeStart", new AttributeValue(rootNode.get("slotTimeStart").asText()));
        itemMap.put("slotTimeEnd", new AttributeValue(rootNode.get("slotTimeEnd").asText()));


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
}
