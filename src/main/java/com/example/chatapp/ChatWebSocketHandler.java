package com.example.chatapp;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions =
            new CopyOnWriteArraySet<>();

    private final MessageRepository messageRepository;

    public ChatWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // 🔹 Send message history ONLY to this user
        List<Message> history = messageRepository.findLatest25Messages();
        Collections.reverse(history); // oldest → newest

        for (Message msg : history) {
            session.sendMessage(
                new TextMessage(msg.getUsername() + ": " + msg.getContent())
            );
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        String username = (String) session.getAttributes().get("username");
        if (username == null) username = "Guest";

        String content = message.getPayload();

        // Save to DB
        Message msg = new Message(username, content);
        messageRepository.save(msg);

        String formatted = username + ": " + content;

        // Broadcast to everyone
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(formatted));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
