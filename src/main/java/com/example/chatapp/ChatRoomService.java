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

    @Transactional
    public ChatRoom getOrCreateRoom(String room) {
        return chatRoomRepository.findByName(room)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(room)));
    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    @Transactional
    public void deleteRoom(String room) {
        messageRepository.deleteByRoom(room);
        chatRoomRepository.deleteByName(room);
    }
}
