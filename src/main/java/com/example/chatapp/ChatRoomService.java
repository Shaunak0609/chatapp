package com.example.chatapp;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           MessageRepository messageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
    }

    public ChatRoom getOrCreateRoom(String name) {
        return chatRoomRepository
                .findByName(name)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(name)));
    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    public boolean exists(String name) {
        return chatRoomRepository.findByName(name).isPresent();
    }

    @Transactional
    public void deleteRoom(String room) {
        messageRepository.deleteByRoom(room);
        chatRoomRepository.deleteByName(room);
    }
}
