package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class GetTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AuthHandler authHandler = new AuthHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDB;

    public GetTablesHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        String email = authHandler.getCurrentuser(requestEvent);
        if (email == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(403);
        }

        String tableId = requestEvent.getPathParameters().get("tableId");
        if(tableId == null || tableId.isEmpty()) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }

        System.out.println("email: " + email);

        // Create a map with the key (e.g., table id) for querying DynamoDB
        Map<String, AttributeValue> key = Map.of(
                "id", new AttributeValue().withN(tableId)
        );

        // Create the GetItemRequest
        GetItemRequest request = new GetItemRequest()
                .withTableName((System.getenv("tables_table")))
                .withKey(key);

        // Get the item from DynamoDB
        GetItemResult result = dynamoDB.getItem(request);

        // If item doesn't exist, return a not found message
        if (result.getItem() == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }

        // Map the DynamoDB item to a response object
        Map<String, AttributeValue> item = result.getItem();

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("id", Integer.parseInt(item.get("id").getN()));
        response.put("number", Integer.parseInt(item.get("tableNumber").getN()));
        response.put("places", Integer.parseInt(item.get("places").getN()));
        response.put("isVip", Boolean.parseBoolean(item.get("isVip").getBOOL().toString()));

        // Check if optional field 'minOrder' exists
        if (item.containsKey("minOrder")) {
            response.put("minOrder", Integer.parseInt(item.get("minOrder").getN()));
        }

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
