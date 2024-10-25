package com.task05.model;

public class EventResponseDto {

    private String id;
    private int principalId;
    private String createdAt;
    private Content body;

    // Constructor to map Event to Response DTO
    public EventResponseDto(Event event) {
        this.id = event.getId();
        this.principalId = event.getPrincipalId();
        this.createdAt = event.getCreatedAt();
        this.body = event.getBody();
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
