package com.task05.model;

import java.util.Map;

public class EventResponseDto {

    private String id;
    private Integer principalId;
    private String createdAt;
    private Map<String, String> body;

    @Override
    public String toString() {
        return "EventResponseDto{" +
                "id='" + id + '\'' +
                ", principalId=" + principalId +
                ", createdAt='" + createdAt + '\'' +
                ", body=" + body +
                '}';
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPrincipalId(Integer principalId) {
        this.principalId = principalId;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    // Constructor to map Event to Response DTO
    public EventResponseDto(Event eventEntity) {
        this.id = eventEntity.getId();
        this.principalId = eventEntity.getPrincipalId();
        this.createdAt = eventEntity.getCreatedAt();
        this.body = eventEntity.getBody();
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }


}
