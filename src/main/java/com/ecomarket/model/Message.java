package com.ecomarket.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One chat bubble. Direction is derived from the current user at render time. */
public class Message {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");

    private String id;
    private String senderId;
    private String text;
    private LocalDateTime timestamp;

    public Message() {
    }

    public Message(String id, String senderId, String text, LocalDateTime timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isMine(User currentUser) {
        return currentUser != null && senderId != null && senderId.equals(currentUser.getId());
    }

    public String timeLabel() {
        return timestamp == null ? "" : timestamp.format(TIME);
    }
}
