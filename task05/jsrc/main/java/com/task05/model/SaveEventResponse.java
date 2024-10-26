package com.task05.model;

public class SaveEventResponse {

    private final Integer statusCode;
    private final EventResponseDto event;

    public SaveEventResponse(int statusCode, Event eventEntity) {
        this.statusCode = statusCode;
        this.event = new EventResponseDto(eventEntity);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "SaveEventResponse{" +
                "statusCode=" + statusCode +
                ", event=" + event +
                '}';
    }

    public EventResponseDto getEvent() {
        return event;
    }

}
