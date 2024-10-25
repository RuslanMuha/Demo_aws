package com.task05.model;

public class EventRequestDto {

    private int principalId;
    private Content content;

    // Getters and Setters
    public int getPrincipalId() {
        return principalId;
    }

    public EventRequestDto() {
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    @Override
    public String toString() {
        return "EventRequestDto{" +
                "principalId=" + principalId +
                ", content=" + content +
                '}';
    }

    public Content getContent() {
        return content;
    }

    public EventRequestDto(int principalId, Content content) {
        this.principalId = principalId;
        this.content = content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
