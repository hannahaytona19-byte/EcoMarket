package com.ecomarket.model;

/**
 * In-app notification. Named AppNotification to avoid clashing with JavaFX's
 * own Notification class, but presented to the user simply as "Notifications".
 */
public class AppNotification {

    private String id;
    private String title;
    private String detail;
    private String timeLabel;
    private String group;          // "Today" or "Yesterday"
    private boolean read;
    private String conversationId; // navigate to a chat
    private String listingId;      // or open a listing

    public AppNotification() {
    }

    public AppNotification(String id, String title, String detail, String timeLabel,
                           String group, boolean read, String conversationId, String listingId) {
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.timeLabel = timeLabel;
        this.group = group;
        this.read = read;
        this.conversationId = conversationId;
        this.listingId = listingId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
}
