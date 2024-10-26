package com.task05.config;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoDbClient {


    private final DynamoDBMapper mapper;
    private final AmazonDynamoDB dynamoDB;

    public DynamoDbClient(String region) {
        // Set your region
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region) // Set your region
                .build();
        this.dynamoDB = dynamoDB;
        this.mapper = new DynamoDBMapper(dynamoDB);
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }

    public AmazonDynamoDB getDynamoDB() {
        return dynamoDB;
    }

}
