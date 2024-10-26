package com.task05.model;

public class EventResponseDto {

    private String id;
    private Integer principalId;
    private String createdAt;
    private Content body;

    @Override
    public String toString() {
        return "EventResponseDto{" +
                "id='" + id + '\'' +
                ", principalId=" + principalId +
                ", createdAt='" + createdAt + '\'' +
                ", body=" + body +
                '}';
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

    public Content getBody() {
        return body;
    }
}
