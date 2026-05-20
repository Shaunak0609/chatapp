package com.example.chatapp;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = """
        SELECT * FROM messages
        WHERE room = :room
        ORDER BY timestamp DESC
        LIMIT 50
    """, nativeQuery = true)
    List<Message> findLast50MessagesByRoom(@Param("room") String room);

    @Query(value = """
        SELECT * FROM messages
        WHERE room = :room AND timestamp >= :joinedAt
        ORDER BY timestamp DESC
        LIMIT 100
    """, nativeQuery = true)
    List<Message> findMessagesAfterJoinTime(@Param("room") String room,
                                            @Param("joinedAt") LocalDateTime joinedAt);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM messages WHERE room = ?1", nativeQuery = true)
    void deleteByRoom(String room);
}
