package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task05.config.DynamoDbClient;
import com.task05.model.Event;
import com.task05.model.EventRequestDto;
import com.task05.model.SaveEventResponse;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_table", value = "${target_table}"),
		@EnvironmentVariable(key = "region", value = "${region}")
})
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
	ObjectMapper objectMapper = new ObjectMapper();

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		String body = requestEvent.getBody();


		EventRequestDto request;
		try {
			request = objectMapper.readValue(body, EventRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

		System.out.println("request: " +request);


        String region = System.getenv("region");

		System.out.println("region: "+region);
		DynamoDbClient dbClient = new DynamoDbClient(region);
		AmazonDynamoDB dynamoDB = dbClient.getDynamoDB();

		Event event = new Event();
		event.setPrincipalId(request.getPrincipalId());
		event.setContent(request.getContent());

		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", new AttributeValue(event.getId()));
		item.put("principalId", new AttributeValue().withN(String.valueOf(event.getPrincipalId())));
		item.put("createdAt", new AttributeValue(event.getCreatedAt()));
        try {
            item.put("body", new AttributeValue(objectMapper.writeValueAsString(event.getContent())));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

		String targetTable = System.getenv("target_table");

		System.out.println("target table: "+targetTable);
		PutItemRequest requestDb = new PutItemRequest()
				.withTableName(targetTable)
				.withItem(item);

		System.out.println("event: " + event);
		dynamoDB.putItem(requestDb);

		boolean b = validateEvent(event.getId(), dynamoDB, targetTable);
		System.out.println("item exists: "+b);

        try {
            return buildResponse(201, new SaveEventResponse(201, event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

	private boolean validateEvent(String eventId, AmazonDynamoDB dynamoDB, String targetTable) {
		// Step 1: Create the key for the item
		Map<String, AttributeValue> key = new HashMap<>();
		key.put("id", new AttributeValue(eventId));

		// Step 2: Create the GetItemRequest
		GetItemRequest request = new GetItemRequest()
				.withTableName(targetTable)
				.withKey(key);

		// Step 3: Retrieve the item
		GetItemResult result = dynamoDB.getItem(request);
		result.getItem().forEach((k, v) -> {
			System.out.println("key: " + k);
			System.out.println("value: " +v);
		});

		// Step 4: Check if the item exists
		return result.getItem() != null;
	}

	private APIGatewayV2HTTPResponse buildResponse(int statusCode, Object body) throws JsonProcessingException {
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withHeaders(responseHeaders)
				.withBody(objectMapper.writeValueAsString(body))
				.build();
	}
}
