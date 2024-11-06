package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmeteo.OpenMeteoService;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.*;
import java.util.stream.Collectors;

@LambdaHandler(
        tracingMode = TracingMode.Active,
        layers = "open_meteo_sdk_layer",
        lambdaName = "processor",
        roleName = "processor-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
        layerName = "open_meteo_sdk_layer",
        libraries = "lib/original-open-meteo-1.0.0.jar",
        runtime = DeploymentRuntime.JAVA11
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "target_table", value = "${target_table}"),
        @EnvironmentVariable(key = "region", value = "${region}")
})
public class Processor implements RequestHandler<Object, String> {

    AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(System.getenv("region")) // Set your region
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String handleRequest(Object request, Context context) {
        String weatherForecast = OpenMeteoService.getWeatherForecast();

        // Parse input JSON
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(weatherForecast);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Generate UUID
        String id = UUID.randomUUID().toString();

        // Extract fields for forecast

        Map<String, AttributeValue> forecast = new HashMap<>();
        forecast.put("elevation", new AttributeValue().withN(String.valueOf(rootNode.get("elevation").asDouble())));
        forecast.put("generationtime_ms", new AttributeValue().withN(String.valueOf(rootNode.get("generationtime_ms").asDouble())));
        forecast.put("latitude", new AttributeValue().withN(String.valueOf(rootNode.get("latitude").asDouble())));
        forecast.put("longitude", new AttributeValue().withN(String.valueOf(rootNode.get("longitude").asDouble())));
        forecast.put("timezone", new AttributeValue(rootNode.get("timezone").asText()));
        forecast.put("timezone_abbreviation", new AttributeValue(rootNode.get("timezone_abbreviation").asText()));
        forecast.put("utc_offset_seconds", new AttributeValue().withN(String.valueOf(rootNode.get("utc_offset_seconds").asInt())));

        // Add nested hourly data
        JsonNode hourlyNode = rootNode.path("hourly");
        forecast.put("hourly", new AttributeValue().withM(Map.of(
                "temperature_2m", new AttributeValue().withL(convertToAttributeValueList(hourlyNode.path("temperature_2m"), false)),
                "time", new AttributeValue().withL(convertToAttributeValueList(hourlyNode.path("time"), true))
        )));

        // Add nested hourly_units data
        JsonNode hourlyUnitsNode = rootNode.path("hourly_units");
        forecast.put("hourly_units", new AttributeValue().withM(Map.of(
                "temperature_2m", new AttributeValue(hourlyUnitsNode.path("temperature_2m").asText()),
                "time", new AttributeValue(hourlyUnitsNode.path("time").asText())
        )));

        // Prepare main item map for DynamoDB
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", new AttributeValue(id));
        itemMap.put("forecast", new AttributeValue().withM(forecast));

        PutItemRequest requestDb = new PutItemRequest()
                .withTableName(System.getenv("target_table"))
                .withItem(itemMap);

        dynamoDB.putItem(requestDb);

        System.out.println("Data successfully stored with ID: " + id);

        return id;

    }

    private List<AttributeValue> convertToAttributeValueList(JsonNode arrayNode, boolean isString) {
        List<AttributeValue> attributeValues = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode element : arrayNode) {
                if (isString) {
                    attributeValues.add(new AttributeValue(element.asText()));
                } else {
                    attributeValues.add(new AttributeValue().withN(element.asText())); // For numeric values
                }
            }
        }
        return attributeValues;
    }
}
