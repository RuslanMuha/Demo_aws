package com.task05.model;

import java.io.Serializable;
import java.util.Map;

public class EventRequestDto  implements Serializable {

    private Integer principalId;
    private Map<String, String> content;

    // Getters and Setters
    public int getPrincipalId() {
        return principalId;
    }

    public EventRequestDto() {
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setPrincipalId(Integer principalId) {
        this.principalId = principalId;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "EventRequestDto{" +
                "principalId=" + principalId +
                ", content=" + content +
                '}';
    }


    public EventRequestDto(int principalId, Map<String, String>  content) {
        this.principalId = principalId;
        this.content = content;
    }


}
