package com.example.chatapp;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(length = 1000)
    private String content;

    private LocalDateTime timestamp;

    private String room;

    // Nullable attachment fields — null for plain text messages
    private Long attachmentId;
    private String attachmentUrl;
    private String attachmentOriginalName;
    private String attachmentType;

    public Message() {}

    public Message(String username, String content, String room) {
        this.username = username;
        this.content = content;
        this.room = room;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getRoom() { return room; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long v) { this.attachmentId = v; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String v) { this.attachmentUrl = v; }

    public String getAttachmentOriginalName() { return attachmentOriginalName; }
    public void setAttachmentOriginalName(String v) { this.attachmentOriginalName = v; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String v) { this.attachmentType = v; }
}
