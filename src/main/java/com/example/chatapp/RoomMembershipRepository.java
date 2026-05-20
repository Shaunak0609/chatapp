package com.example.chatapp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {

    Optional<RoomMembership> findByUsernameAndRoomName(String username, String roomName);

    boolean existsByUsernameAndRoomName(String username, String roomName);

    boolean existsByUsernameAndRoomNameAndRole(String username, String roomName, String role);

    @Modifying
    @Transactional
    @Query("DELETE FROM RoomMembership m WHERE m.roomName = :roomName")
    void deleteByRoomName(@Param("roomName") String roomName);
}
