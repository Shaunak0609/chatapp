package com.example.chatapp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query(value = "SELECT * FROM messages ORDER BY timestamp DESC LIMIT 25", nativeQuery= true)
    List<Message> findLatest25Messages();

}
