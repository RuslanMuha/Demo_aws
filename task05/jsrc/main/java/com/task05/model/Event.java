package com.task05.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


public class Event implements Serializable {

    private String id;
    private Integer principalId;
    private String createdAt;
    private Content body;

    public Event() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", principalId=" + principalId +
                ", createdAt='" + createdAt + '\'' +
                ", body=" + body +
                '}';
    }

    public Event(String id, int principalId, String createdAt, Content body) {
        this.id = id;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.body = body;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Content getBody() {
        return body;
    }

    public void setBody(Content body) {
        this.body = body;
    }
}