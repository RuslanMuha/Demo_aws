package com.task05.config;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoDbClient {


    private final DynamoDBMapper mapper;

    public DynamoDbClient() {
        // Set your region
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion("eu-central-1") // Set your region
                .build();
        this.mapper = new DynamoDBMapper(dynamoDB);
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }

}
