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

public class PostTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final AuthHandler authHandler = new AuthHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDB;

    public PostTablesHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        String email = authHandler.getCurrentuser(requestEvent);
        System.out.println("email: " + email);
        if (email == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(403);
        }

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(requestEvent.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("email: " + email);

        // Create a map to store the item data for DynamoDB
        Map<String, AttributeValue> itemMap = new HashMap<>();
        int tableNumber = rootNode.get("number").asInt();
        System.out.println("created tableNumber: " + tableNumber);
        // Add required fields
        itemMap.put("email", new AttributeValue(email));
        itemMap.put("id", new AttributeValue().withN(String.valueOf(rootNode.get("id").asInt())));
        itemMap.put("tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
        itemMap.put("places", new AttributeValue().withN(String.valueOf(rootNode.get("places").asInt())));
        itemMap.put("isVip", new AttributeValue().withBOOL(rootNode.get("isVip").asBoolean()));

        // Add optional field `minOrder` if present
        if (rootNode.has("minOrder")) {
            itemMap.put("minOrder", new AttributeValue().withN(String.valueOf(rootNode.get("minOrder").asInt())));
        }
        System.out.println("---Before put to tables_table log----");
        itemMap.forEach((k, v) -> {
            System.out.println("fieldName: " + k);
            System.out.println("value: " + v);
        });

        // Insert the item into DynamoDB
        dynamoDB.putItem(new PutItemRequest().withTableName(System.getenv("tables_table")).withItem(itemMap));

        HashMap<String, Integer> response = new HashMap<>();
        response.put("id", rootNode.get("id").asInt());
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
