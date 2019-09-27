package com.magit.webLogic.utils.notifications;

public class SingleNotification {
    private final String message;
    private final String username;
    private final long time;

    public SingleNotification(String message, String username) {
        this.message = message;
        this.username = username;
        this.time = System.currentTimeMillis();
    }

    public String getChatString() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return (username != null ? username + ": " : "") + message;
    }
}
