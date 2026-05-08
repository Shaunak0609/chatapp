package com.example.chatapp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = """
        SELECT * FROM messages
        WHERE room = :room
        ORDER BY timestamp DESC
        LIMIT 50
    """, nativeQuery = true)
    List<Message> findLast50MessagesByRoom(String room);
    
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM messages
        WHERE room = ?
    """, nativeQuery = true)
    void deleteByRoom(String room);
}
