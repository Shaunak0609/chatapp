package com.example.chatapp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // room -> sessions
    private static final Map<String, Set<WebSocketSession>> roomSessions =
            new ConcurrentHashMap<>();

    public ChatWebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String room = (String) session.getAttributes().get("room");
        String username = (String) session.getAttributes().get("username");

        if (room == null || username == null) {
            session.close();
            return;
        }

        roomSessions.putIfAbsent(room, new CopyOnWriteArraySet<>());
        roomSessions.get(room).add(session);

        // Load last 50 messages for this room
        List<Message> history = messageService.findLast50ByRoom(room);
        Collections.reverse(history);

        for (Message msg : history) {
            session.sendMessage(
                new TextMessage(objectMapper.writeValueAsString(msg))
            );
        }
    }

    @Override
    @Transactional
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        String room = (String) session.getAttributes().get("room");
        String username = (String) session.getAttributes().get("username");

        if (room == null || username == null) {
            return;
        }

        String content = message.getPayload().trim();
        if (content.isEmpty()) {
            return;
        }

        // Persist message
        Message msg = new Message(username, content, room);
        messageService.saveMessage(msg);

        String json = objectMapper.writeValueAsString(msg);

        // Broadcast to everyone in the room
        for (WebSocketSession s : roomSessions.get(room)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomSessions.values().forEach(set -> set.remove(session));
    }
}
