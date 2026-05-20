package com.example.chatapp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomInviteService {

    private final RoomInviteRepository inviteRepo;
    private final RoomMembershipService membershipService;

    public RoomInviteService(RoomInviteRepository inviteRepo,
                             RoomMembershipService membershipService) {
        this.inviteRepo = inviteRepo;
        this.membershipService = membershipService;
    }

    /** Returns true if the invite was created, false if a duplicate already exists. */
    @Transactional
    public boolean sendInvite(String roomName, String inviterUsername, String inviteeUsername) {
        if (inviteRepo.existsByRoomNameAndInviteeUsername(roomName, inviteeUsername)) {
            return false;
        }
        inviteRepo.save(new RoomInvite(roomName, inviterUsername, inviteeUsername));
        return true;
    }

    public List<RoomInvite> getPendingInvites(String username) {
        return inviteRepo.findByInviteeUsernameAndStatus(username, "PENDING");
    }

    /**
     * Accept an invite. Verifies the current user matches the invitee.
     * Returns the room name on success, empty on failure.
     */
    @Transactional
    public Optional<String> acceptInvite(Long inviteId, String currentUsername) {
        Optional<RoomInvite> opt = inviteRepo.findById(inviteId);
        if (opt.isEmpty()) return Optional.empty();

        RoomInvite invite = opt.get();
        if (!invite.getInviteeUsername().equals(currentUsername)) return Optional.empty();
        if (!"PENDING".equals(invite.getStatus())) return Optional.empty();

        invite.setStatus("ACCEPTED");
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepo.save(invite);

        membershipService.addMember(currentUsername, invite.getRoomName(), "MEMBER");

        return Optional.of(invite.getRoomName());
    }

    /** Decline an invite. Verifies the current user matches the invitee. */
    @Transactional
    public boolean declineInvite(Long inviteId, String currentUsername) {
        Optional<RoomInvite> opt = inviteRepo.findById(inviteId);
        if (opt.isEmpty()) return false;

        RoomInvite invite = opt.get();
        if (!invite.getInviteeUsername().equals(currentUsername)) return false;
        if (!"PENDING".equals(invite.getStatus())) return false;

        invite.setStatus("DECLINED");
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepo.save(invite);
        return true;
    }

    @Transactional
    public void deleteInvitesByRoom(String roomName) {
        inviteRepo.deleteByRoomName(roomName);
    }
}
