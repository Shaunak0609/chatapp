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

    @Transactional
    public LocalDateTime getOrCreateJoinTime(String username, String roomName) {
        return repo.findByUsernameAndRoomName(username, roomName)
                .map(RoomMembership::getJoinedAt)
                .orElseGet(() -> repo.save(new RoomMembership(username, roomName)).getJoinedAt());
    }
}
