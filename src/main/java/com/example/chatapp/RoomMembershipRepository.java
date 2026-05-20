package com.example.chatapp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
    Optional<RoomMembership> findByUsernameAndRoomName(String username, String roomName);
}
