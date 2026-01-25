package com.example.chatapp;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> findLast50ByRoom(String room) {
        return messageRepository.findLast50MessagesByRoom(room);
    }
}
