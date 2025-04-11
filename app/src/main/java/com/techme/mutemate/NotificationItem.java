package com.techme.mutemate;

public class NotificationItem {
    private final String title;
    private final String message;
    private final long timestamp;

    public NotificationItem(String title, String message) {
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}