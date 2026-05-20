package com.example.chatapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Nullable in DB so existing rows are not broken by the schema update.
     * Treat null as false (public room).
     */
    @Column(name = "private_room")
    private Boolean privateRoom = false;

    @Column(name = "owner_username")
    private String ownerUsername;

    public ChatRoom() {}

    public ChatRoom(String name) {
        this.name = name;
        this.privateRoom = false;
    }

    public ChatRoom(String name, boolean privateRoom, String ownerUsername) {
        this.name = name;
        this.privateRoom = privateRoom;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    /** Returns true only if this room is explicitly marked private. */
    public boolean isPrivateRoom() {
        return Boolean.TRUE.equals(privateRoom);
    }

    public void setPrivateRoom(boolean privateRoom) { this.privateRoom = privateRoom; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
