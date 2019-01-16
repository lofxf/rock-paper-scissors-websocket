package com.websocket.model;

public class Player {
    private String sessionId;
    private String name;
    private String status;
    private String gesture;

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGesture(String gesture) {
        this.gesture = gesture;
    }

    public String getGesture() {
        return gesture;
    }

}
