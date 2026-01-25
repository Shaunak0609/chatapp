package com.example.chatapp;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;

    public MessageService(MessageRepository messageRepository,
                          ChatRoomService chatRoomService) {
        this.messageRepository = messageRepository;
        this.chatRoomService = chatRoomService;
    }

    @Transactional
    public void saveMessage(Message message) {
        // Ensure room exists
        chatRoomService.getOrCreateRoom(message.getRoom());

        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> findLast50ByRoom(String room) {
        return messageRepository.findLast50MessagesByRoom(room);
    }
}

