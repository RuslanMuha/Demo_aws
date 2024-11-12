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

public class GetReservationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final AuthHandler authHandler = new AuthHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonDynamoDB dynamoDB;

    public GetReservationHandler(AmazonDynamoDB dynamoDB) {
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
        Table reservationsTable = new DynamoDB(dynamoDB).getTable(System.getenv("reservations_table"));
        Iterator<Item> itemsIterator = reservationsTable.scan(scanSpec).iterator();

        // Prepare the response
        List<Map<String, Object>> reservationsList = new ArrayList<>();

        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();

            // Extract the necessary fields for each reservation
            Map<String, Object> reservation = new HashMap<>();
            reservation.put("tableNumber", item.getInt("tableNumber"));
            reservation.put("clientName", item.getString("clientName"));
            reservation.put("phoneNumber", item.getString("phoneNumber"));
            reservation.put("date", item.getString("date"));
            reservation.put("slotTimeStart", item.getString("slotTimeStart"));
            reservation.put("slotTimeEnd", item.getString("slotTimeEnd"));

            reservationsList.add(reservation);
        }

        // Construct the final response map
        Map<String, Object> response = new HashMap<>();
        response.put("reservations", reservationsList);

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
