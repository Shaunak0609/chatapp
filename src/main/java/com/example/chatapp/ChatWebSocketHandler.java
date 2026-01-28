package com.example.chatapp;

import java.net.URI;
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

    private final MessageRepository messageRepository;

    // room -> sessions
    private static final Map<String, Set<WebSocketSession>> roomSessions =
            new ConcurrentHashMap<>();

    public ChatWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String room = extractRoom(session);
        if (room == null) {
            session.close();
            return;
        }

        roomSessions.putIfAbsent(room, new CopyOnWriteArraySet<>());
        roomSessions.get(room).add(session);

        // Send last 50 messages for THIS room only
        List<Message> history =
                messageRepository.findLast50MessagesByRoom(room);
        Collections.reverse(history);

        for (Message msg : history) {
            session.sendMessage(new TextMessage(
                    msg.getUsername() + ": " + msg.getContent()
            ));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        String room = extractRoom(session);
        if (room == null) return;

        String username = "Anonymous";
        if (session.getPrincipal() != null) {
            username = session.getPrincipal().getName();
        }

        Message msg = new Message(username, message.getPayload(), room);
        messageRepository.save(msg);

        for (WebSocketSession s : roomSessions.getOrDefault(room, Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(
                        username + ": " + message.getPayload()
                ));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomSessions.values().forEach(set -> set.remove(session));
    }

    private String extractRoom(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) return null;

        for (String param : uri.getQuery().split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals("room")) {
                return pair[1];
            }
        }
        return null;
    }

    public static void notifyRoomDeleted(String room) {
        Set<WebSocketSession> sessions = roomSessions.get(room);

        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage("__ROOM_DELETED__"));
                        s.close();
                    }
                } catch (Exception ignored) {}
            }
            roomSessions.remove(room);
        }
    }
}
