package com.task05.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.task05.config.DynamoDbClient;
import com.task05.model.Event;

public class EventRepository {

    private final DynamoDBMapper mapper;

    public EventRepository(DynamoDbClient dbClient) {
        this.mapper = dbClient.getMapper();
    }

    public void saveEvent(Event event) {
        mapper.save(event);
    }

    public Event getEventById(String id) {
        return mapper.load(Event.class, id);
    }

}
