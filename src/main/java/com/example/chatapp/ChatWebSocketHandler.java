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

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private static final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String room = (String) session.getAttributes().get("room");
        String username = (String) session.getAttributes().get("username");

        System.out.println("WebSocket connected: room=" + room + ", username=" + username);

        roomSessions.putIfAbsent(room, new CopyOnWriteArraySet<>());
        roomSessions.get(room).add(session);

        List<Message> history = messageService.findLast50ByRoom(room);
        Collections.reverse(history);
        for (Message msg : history) {
            session.sendMessage(new TextMessage(msg.getUsername() + ": " + msg.getContent()));
        }
    }

    @Override
    @Transactional
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String room = (String) session.getAttributes().get("room");
        String username = (String) session.getAttributes().get("username");
        if (username == null) username = "Anonymous";

        String content = message.getPayload();
        System.out.println("Saving message: " + username + ": " + content);

        Message msg = new Message(username, content, room);
        messageService.saveMessage(msg);

        for (WebSocketSession s : roomSessions.get(room)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(username + ": " + content));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomSessions.values().forEach(sessions -> sessions.remove(session));
    }
}
