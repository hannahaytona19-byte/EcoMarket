package com.ecomarket.model;

import java.util.ArrayList;
import java.util.List;

/** A chat thread about one listing. */
public class Conversation {

    private String id;
    private User participant;
    private Listing listing;
    private final List<Message> messages = new ArrayList<>();
    private int unreadCount;

    public Conversation() {
    }

    public Conversation(String id, User participant, Listing listing) {
        this.id = id;
        this.participant = participant;
        this.listing = listing;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getParticipant() { return participant; }
    public void setParticipant(User participant) { this.participant = participant; }

    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }

    public List<Message> getMessages() { return messages; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public Message lastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public String lastPreview() {
        Message last = lastMessage();
        return last == null ? "No messages yet" : last.getText();
    }

    public String lastTimeLabel() {
        Message last = lastMessage();
        return last == null ? "" : last.timeLabel();
    }
}
