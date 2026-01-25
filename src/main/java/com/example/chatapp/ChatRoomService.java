package com.example.chatapp;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional
    public ChatRoom getOrCreateRoom(String roomName) {
        return chatRoomRepository
                .findByName(roomName)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(roomName)));
    }
}
