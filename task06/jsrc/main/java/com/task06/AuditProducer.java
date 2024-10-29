package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
        lambdaName = "audit_producer",
        roleName = "audit_producer-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "target_table", value = "${target_table}"),
        @EnvironmentVariable(key = "region", value = "${region}")
})
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 100)
public class AuditProducer implements RequestHandler<DynamodbEvent, Object> {


    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(System.getenv("region")) // Set your region
            .build();

    @Override
    public Object handleRequest(DynamodbEvent dynamoEvent, Context context) {
        List<DynamodbEvent.DynamodbStreamRecord> records = dynamoEvent.getRecords();


        for (DynamodbEvent.DynamodbStreamRecord record : records) {
            String eventName = record.getEventName();
            var newImage = record.getDynamodb().getNewImage();
            var oldImage = record.getDynamodb().getOldImage();

            String itemKey = newImage != null && newImage.containsKey("key")
                    ? newImage.get("key").getS()
                    : (oldImage != null && oldImage.containsKey("key") ? oldImage.get("key").getS() : null);

            System.out.println("item key: " + itemKey);

            if (itemKey == null) {
                continue;
            }

            String auditId = UUID.randomUUID().toString();
            Instant modificationTime = Instant.now();

            Map<String, AttributeValue> auditItem = new HashMap<>();
            auditItem.put("id", new AttributeValue(auditId));
            auditItem.put("itemKey", new AttributeValue(itemKey));
            auditItem.put("modificationTime", new AttributeValue(modificationTime.toString()));

            System.out.println("EVENT NAME: " + eventName);

            if ("INSERT".equals(eventName)) {
                Map<String, AttributeValue> map = toMap(newImage);
                Map<String, AttributeValue> mapO = toMap(oldImage);
                if (!map.isEmpty()) {
                    map.forEach((k, v) -> {
                        System.out.println("key: " + k + " value: " + (v == null ? "null" : v.getN()));
                    });
                    auditItem.put("newValue", new AttributeValue().withM(map));
                } else {
                    System.out.println("INSERT newValue is empty");
                }

                if (!mapO.isEmpty()) {
                    mapO.forEach((k, v) -> {
                        System.out.println("key: " + k + " value: " + (v == null ? "null" : v.getN()));
                    });
                    auditItem.put("newValue", new AttributeValue().withM(mapO));
                } else {
                    System.out.println("INSERT newValue is empty");
                }

            } else if ("MODIFY".equals(eventName)) {
                String updatedAttribute = null;
                Integer oldValue = null;
                Integer newValue = null;

                if (oldImage != null && newImage != null) {

                    var oldValueI = oldImage.get("value");
                    var newValueI = newImage.get("value");
                    if (oldValueI != null && newValueI != null) {
                        System.out.println("value: " + oldValueI.getN());

                        if (!oldValueI.getN().equals(newValueI.getN())) {
                            updatedAttribute = "value";
                            oldValue = Integer.parseInt(oldValueI.getN());
                            newValue = Integer.parseInt(newValueI.getN());
                        }
                    }
                }

                if (updatedAttribute != null) {
                    auditItem.put("updatedAttribute", new AttributeValue(updatedAttribute));
                    auditItem.put("oldValue", new AttributeValue().withN(String.valueOf(oldValue)));
                    auditItem.put("newValue", new AttributeValue().withN(String.valueOf(newValue)));
                }
            }

            PutItemRequest requestDb = new PutItemRequest()
                    .withTableName(System.getenv("target_table"))
                    .withItem(auditItem);

            dynamoDB.putItem(requestDb);
        }

        return "ok";
    }

    private Map<String, AttributeValue> toMap(Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage) {
        if (newImage == null || newImage.isEmpty()) {
            return Map.of();
        }
        Map<String, AttributeValue> bodyMap = new HashMap<>();
        for (Map.Entry<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> entry : newImage.entrySet()) {

            System.out.println("INSERT S: " + entry.getValue().toString());
            bodyMap.put(entry.getKey(), new AttributeValue(entry.getValue().toString()));

        }
        return bodyMap;
    }

}
