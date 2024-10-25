package com.task05.model;

public class SaveEventResponse {

    private final int statusCode;
    private final EventResponseDto event;

    public SaveEventResponse(int statusCode, Event event) {
        this.statusCode = statusCode;
        this.event = new EventResponseDto(event);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public EventResponseDto getEvent() {
        return event;
    }

}
