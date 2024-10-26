package com.task05.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.task05.config.DynamoDbClient;
import com.task05.model.EventEntity;

public class EventRepository {

    private final DynamoDBMapper mapper;

    public EventRepository(DynamoDbClient dbClient) {
        this.mapper = dbClient.getMapper();
    }

    public void saveEvent(EventEntity eventEntity) {
        mapper.save(eventEntity);
    }

    public EventEntity getEventById(String id) {
        return mapper.load(EventEntity.class, id);
    }

}
