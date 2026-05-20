package com.example.chatapp;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "room_memberships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "room_name"}))
public class RoomMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "room_name")
    private String roomName;

    private LocalDateTime joinedAt;

    /**
     * OWNER – created the private room.
     * MEMBER – accepted an invite.
     * null   – public room tracking record (role not applicable).
     */
    @Column(nullable = true)
    private String role;

    public RoomMembership() {}

    /** Used by RoomMembershipService.getOrCreateJoinTime for public rooms. */
    public RoomMembership(String username, String roomName) {
        this.username = username;
        this.roomName = roomName;
        this.joinedAt = LocalDateTime.now();
    }

    /** Used when explicitly assigning a role (OWNER / MEMBER). */
    public RoomMembership(String username, String roomName, String role) {
        this.username = username;
        this.roomName = roomName;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRoomName() { return roomName; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
