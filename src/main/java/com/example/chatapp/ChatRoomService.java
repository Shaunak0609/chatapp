package com.example.chatapp;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final RoomMembershipRepository membershipRepository;
    private final RoomInviteRepository inviteRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           MessageRepository messageRepository,
                           RoomMembershipRepository membershipRepository,
                           RoomInviteRepository inviteRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.membershipRepository = membershipRepository;
        this.inviteRepository = inviteRepository;
    }

    /** Find-or-create a public room. Never creates a private room. */
    public ChatRoom getOrCreateRoom(String name) {
        return chatRoomRepository
                .findByName(name)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(name)));
    }

    /**
     * Create a private room. Returns empty if a room with that name already exists.
     * Also creates the OWNER membership so the creator can enter immediately.
     */
    @Transactional
    public Optional<ChatRoom> createPrivateRoom(String name, String ownerUsername) {
        if (chatRoomRepository.findByName(name).isPresent()) {
            return Optional.empty();
        }
        ChatRoom room = chatRoomRepository.save(new ChatRoom(name, true, ownerUsername));
        membershipRepository.save(new RoomMembership(ownerUsername, name, "OWNER"));
        return Optional.of(room);
    }

    public Optional<ChatRoom> findRoom(String name) {
        return chatRoomRepository.findByName(name);
    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    public boolean exists(String name) {
        return chatRoomRepository.findByName(name).isPresent();
    }

    /** Delete a room and all associated messages, memberships, and invites. */
    @Transactional
    public void deleteRoom(String room) {
        messageRepository.deleteByRoom(room);
        membershipRepository.deleteByRoomName(room);
        inviteRepository.deleteByRoomName(room);
        chatRoomRepository.deleteByName(room);
    }
}
