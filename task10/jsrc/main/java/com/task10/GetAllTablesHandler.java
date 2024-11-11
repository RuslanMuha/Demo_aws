package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class GetAllTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AuthHandler authHandler = new AuthHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDB;

    public GetAllTablesHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        String email = authHandler.getCurrentuser(requestEvent);
        if (email == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(403);
        }

        System.out.println("email: " + email);

        Map<String, Object> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":email", email);

        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("email = :email") // Filter by email field
                .withValueMap(expressionAttributeValues);

        // Use the Scan operation to fetch all reservations that match the email
        Table reservationsTable = new DynamoDB(dynamoDB).getTable(System.getenv("tables_table"));
        Iterator<Item> itemsIterator = reservationsTable.scan(scanSpec).iterator();

        // Prepare the response
        List<Map<String, Object>> tableList = new ArrayList<>();

        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();

            // Extract the necessary fields for each reservation
            Map<String, Object> reservation = new HashMap<>();
            reservation.put("id", item.getInt("id"));
            reservation.put("number", item.getInt("tableNumber"));
            reservation.put("places", item.getInt("places"));
            reservation.put("isVip", item.getBOOL("isVip"));
            if(item.hasAttribute("minOrder")){
                reservation.put("minOrder", item.getInt("minOrder"));
            }

            tableList.add(reservation);
        }

        // Construct the final response map
        Map<String, Object> response = new HashMap<>();
        response.put("tables", tableList);

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
