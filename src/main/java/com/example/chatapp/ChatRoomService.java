package com.example.chatapp;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository repo;

    public ChatRoomService(ChatRoomRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ChatRoom getOrCreateRoom(String room) {
        return repo.findByName(room)
                .orElseGet(() -> repo.save(new ChatRoom(room)));
    }

    public List<ChatRoom> findAll() {
        return repo.findAll();
    }
}
