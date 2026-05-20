package com.example.chatapp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface RoomInviteRepository extends JpaRepository<RoomInvite, Long> {

    List<RoomInvite> findByInviteeUsernameAndStatus(String inviteeUsername, String status);

    boolean existsByRoomNameAndInviteeUsername(String roomName, String inviteeUsername);

    @Modifying
    @Transactional
    @Query("DELETE FROM RoomInvite i WHERE i.roomName = :roomName")
    void deleteByRoomName(@Param("roomName") String roomName);
}
