package com.example.chatapp;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_invites")
public class RoomInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String inviterUsername;

    @Column(nullable = false)
    private String inviteeUsername;

    /** PENDING | ACCEPTED | DECLINED */
    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    public RoomInvite() {}

    public RoomInvite(String roomName, String inviterUsername, String inviteeUsername) {
        this.roomName = roomName;
        this.inviterUsername = inviterUsername;
        this.inviteeUsername = inviteeUsername;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getRoomName() { return roomName; }
    public String getInviterUsername() { return inviterUsername; }
    public String getInviteeUsername() { return inviteeUsername; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime v) { this.respondedAt = v; }
}
