package com.example.chatapp;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomMembershipService {

    private final RoomMembershipRepository repo;

    public RoomMembershipService(RoomMembershipRepository repo) {
        this.repo = repo;
    }

    /**
     * Records that a user first entered a room (for message-history filtering).
     * Idempotent — returns the original joinedAt if called again.
     */
    @Transactional
    public LocalDateTime getOrCreateJoinTime(String username, String roomName) {
        return repo.findByUsernameAndRoomName(username, roomName)
                .map(RoomMembership::getJoinedAt)
                .orElseGet(() -> repo.save(new RoomMembership(username, roomName)).getJoinedAt());
    }

    /** Explicitly adds a member with a role (OWNER or MEMBER). Idempotent. */
    @Transactional
    public void addMember(String username, String roomName, String role) {
        if (!repo.existsByUsernameAndRoomName(username, roomName)) {
            repo.save(new RoomMembership(username, roomName, role));
        }
    }

    /** Returns true if a membership row exists for this user+room (any role). */
    public boolean isMember(String username, String roomName) {
        return repo.existsByUsernameAndRoomName(username, roomName);
    }

    /** Returns true if the user is the OWNER of this room. */
    public boolean isOwner(String username, String roomName) {
        return repo.existsByUsernameAndRoomNameAndRole(username, roomName, "OWNER");
    }
}
