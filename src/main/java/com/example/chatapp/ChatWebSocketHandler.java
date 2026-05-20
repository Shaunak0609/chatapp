package com.example.chatapp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String ROOM_DELETED_JSON =
            "{\"type\":\"system\",\"content\":\"__ROOM_DELETED__\"}";

    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final RoomMembershipService roomMembershipService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Map<String, Set<WebSocketSession>> roomSessions =
            new ConcurrentHashMap<>();

    public ChatWebSocketHandler(MessageRepository messageRepository,
                                AttachmentRepository attachmentRepository,
                                RoomMembershipService roomMembershipService) {
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.roomMembershipService = roomMembershipService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String room = roomFromSession(session);
        String username = usernameFromSession(session);

        if (room == null || username == null) {
            session.close();
            return;
        }

        roomSessions.putIfAbsent(room, new CopyOnWriteArraySet<>());
        roomSessions.get(room).add(session);

        LocalDateTime joinedAt = roomMembershipService.getOrCreateJoinTime(username, room);

        List<Message> history = messageRepository.findMessagesAfterJoinTime(room, joinedAt);
        Collections.reverse(history);

        for (Message msg : history) {
            session.sendMessage(new TextMessage(toJson(msg)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String room = roomFromSession(session);
        String username = usernameFromSession(session);
        if (room == null || username == null) return;

        String content = null;
        Long attachmentId = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            content = (String) payload.get("content");
            Object attIdRaw = payload.get("attachmentId");
            if (attIdRaw instanceof Number n) {
                attachmentId = n.longValue();
            }
        } catch (Exception e) {
            content = message.getPayload();
        }

        if ((content == null || content.isBlank()) && attachmentId == null) return;

        String attachmentUrl = null, attachmentName = null, attachmentType = null;
        if (attachmentId != null) {
            Optional<Attachment> attOpt = attachmentRepository.findById(attachmentId);
            if (attOpt.isPresent()) {
                Attachment att = attOpt.get();
                attachmentUrl = att.getFileUrl();
                attachmentName = att.getOriginalFileName();
                attachmentType = att.getFileType();
            }
        }

        Message msg = new Message(username, content, room);
        msg.setAttachmentId(attachmentId);
        msg.setAttachmentUrl(attachmentUrl);
        msg.setAttachmentOriginalName(attachmentName);
        msg.setAttachmentType(attachmentType);
        messageRepository.save(msg);

        String json = toJson(msg);
        for (WebSocketSession s : roomSessions.getOrDefault(room, Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomSessions.values().forEach(set -> set.remove(session));
    }

    public static void notifyRoomDeleted(String room) {
        Set<WebSocketSession> sessions = roomSessions.get(room);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(ROOM_DELETED_JSON));
                        s.close();
                    }
                } catch (Exception ignored) {}
            }
            roomSessions.remove(room);
        }
    }

    private String toJson(Message msg) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", "message");
            map.put("username", msg.getUsername());
            map.put("content", msg.getContent());
            map.put("timestamp", msg.getTimestamp().toString());
            map.put("room", msg.getRoom());
            map.put("attachmentId", msg.getAttachmentId());
            map.put("attachmentUrl", msg.getAttachmentUrl());
            map.put("attachmentName", msg.getAttachmentOriginalName());
            map.put("attachmentType", msg.getAttachmentType());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"type\":\"message\",\"content\":\"[error rendering message]\"}";
        }
    }

    private String roomFromSession(WebSocketSession session) {
        Object v = session.getAttributes().get("room");
        return v instanceof String s ? s : null;
    }

    private String usernameFromSession(WebSocketSession session) {
        if (session.getPrincipal() != null) return session.getPrincipal().getName();
        Object v = session.getAttributes().get("username");
        return v instanceof String s ? s : null;
    }
}
