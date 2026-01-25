package com.example.chatapp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private static final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String room = getParam(session, "room", "general");
        String username = getParam(session, "username", "Anonymous");

        session.getAttributes().put("room", room);
        session.getAttributes().put("username", username);

        rooms.putIfAbsent(room, new CopyOnWriteArraySet<>());
        rooms.get(room).add(session);

        List<Message> history = messageService.findLast50ByRoom(room);
        Collections.reverse(history);

        for (Message msg : history) {
            session.sendMessage(
                new TextMessage(msg.getUsername() + ": " + msg.getContent())
            );
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String room = (String) session.getAttributes().get("room");
        String username = (String) session.getAttributes().get("username");

        Message msg = new Message(username, message.getPayload(), room);
        messageService.saveMessage(msg);

        for (WebSocketSession s : rooms.get(room)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(username + ": " + message.getPayload()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        rooms.values().forEach(set -> set.remove(session));
    }

    private String getParam(WebSocketSession session, String key, String def) {
        if (session.getUri() == null) return def;
        String query = session.getUri().getQuery();
        if (query == null) return def;

        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return def;
    }
}
