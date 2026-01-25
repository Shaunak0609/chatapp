package com.example.chatapp;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;

    // room -> active sessions
    private static final Map<String, Set<WebSocketSession>> roomSessions =
            new ConcurrentHashMap<>();

    public ChatWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String[] parts = path.split("/");
        if (parts.length < 4) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String room = parts[3];

        roomSessions.putIfAbsent(room, new CopyOnWriteArraySet<>());
        roomSessions.get(room).add(session);

        // Load last 50 messages
        List<Message> history =
                messageRepository.findLast50MessagesByRoom(room);

        Collections.reverse(history);

        for (Message msg : history) {
            session.sendMessage(
                new TextMessage(msg.getUsername() + ": " + msg.getContent())
            );
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        String path = session.getUri().getPath();
        String room = path.split("/")[3];

        String username =
                (String) session.getAttributes().get("username");

        String content = message.getPayload();

        // 🔹 THIS constructor MUST exist
        Message msg = new Message(username, content, room);
        messageRepository.save(msg);

        for (WebSocketSession s : roomSessions.get(room)) {
            if (s.isOpen()) {
                s.sendMessage(
                    new TextMessage(username + ": " + content)
                );
            }
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session, CloseStatus status) {

        roomSessions.values().forEach(set -> set.remove(session));
    }
}
