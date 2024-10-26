package com.task05.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;

import java.time.Instant;
import java.util.UUID;

@DynamoDBTable(tableName = "cmtr-396a65b2-Events")
public class EventEntity {

    private String id;
    private Integer principalId;
    private String createdAt;
    @DynamoDBTypeConvertedJson
    private Content body;

    public EventEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
    }

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public EventEntity(String id, int principalId, String createdAt, Content body) {
        this.id = id;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.body = body;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "principalId")
    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    @DynamoDBAttribute(attributeName = "createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDBAttribute(attributeName = "body")
    @DynamoDBTypeConvertedJson
    public Content getBody() {
        return body;
    }

    public void setBody(Content body) {
        this.body = body;
    }
}
