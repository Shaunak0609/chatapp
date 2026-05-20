package com.example.chatapp;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RoomController {

    private final ChatRoomService chatRoomService;
    private final RoomInviteService roomInviteService;
    private final UserRepository userRepository;

    public RoomController(ChatRoomService chatRoomService,
                          RoomInviteService roomInviteService,
                          UserRepository userRepository) {
        this.chatRoomService = chatRoomService;
        this.roomInviteService = roomInviteService;
        this.userRepository = userRepository;
    }

    @PostMapping("/rooms/create")
    public String createRoom(
            @RequestParam String roomName,
            @RequestParam(defaultValue = "false") boolean privateRoom,
            @RequestParam(required = false) List<String> invitees,
            @RequestParam(required = false, defaultValue = "") String inviteByUsername,
            Principal principal,
            RedirectAttributes ra) {

        String trimmed = roomName == null ? "" : roomName.trim();

        if (trimmed.isEmpty() || !trimmed.matches("[a-zA-Z0-9_-]{1,50}")) {
            ra.addFlashAttribute("chatError",
                    "Invalid room name. Use letters, numbers, hyphens, or underscores (max 50 chars).");
            return "redirect:/chat?room=general";
        }

        if (!privateRoom) {
            chatRoomService.getOrCreateRoom(trimmed);
            return "redirect:/chat?room=" + trimmed;
        }

        // Private room
        Optional<ChatRoom> created = chatRoomService.createPrivateRoom(trimmed, principal.getName());
        if (created.isEmpty()) {
            ra.addFlashAttribute("chatError",
                    "A room named \"" + trimmed + "\" already exists.");
            return "redirect:/chat?room=general";
        }

        // Collect invited usernames (from checkboxes + manual field), deduplicated
        Set<String> toInvite = new LinkedHashSet<>();
        if (invitees != null) toInvite.addAll(invitees);
        if (!inviteByUsername.isBlank()) toInvite.add(inviteByUsername.trim());
        toInvite.remove(principal.getName()); // can't invite yourself

        List<String> errors = new ArrayList<>();
        for (String uname : toInvite) {
            if (userRepository.findByUsername(uname).isEmpty()) {
                errors.add("\"" + uname + "\" does not exist");
                continue;
            }
            roomInviteService.sendInvite(trimmed, principal.getName(), uname);
        }

        if (!errors.isEmpty()) {
            ra.addFlashAttribute("chatError",
                    "Room created, but some users were not found: " + String.join(", ", errors));
        } else {
            ra.addFlashAttribute("chatSuccess",
                    "Private room \"" + trimmed + "\" created.");
        }

        return "redirect:/chat?room=" + trimmed;
    }

    @PostMapping("/invites/accept")
    public String acceptInvite(@RequestParam Long inviteId,
                               Principal principal,
                               RedirectAttributes ra) {

        Optional<String> roomName = roomInviteService.acceptInvite(inviteId, principal.getName());
        if (roomName.isPresent()) {
            ra.addFlashAttribute("chatSuccess", "You joined #" + roomName.get() + "!");
            return "redirect:/chat?room=" + roomName.get();
        }

        ra.addFlashAttribute("chatError", "Could not accept invite.");
        return "redirect:/chat?room=general";
    }

    @PostMapping("/invites/decline")
    public String declineInvite(@RequestParam Long inviteId,
                                Principal principal,
                                RedirectAttributes ra) {

        roomInviteService.declineInvite(inviteId, principal.getName());
        ra.addFlashAttribute("chatSuccess", "Invite declined.");
        return "redirect:/chat?room=general";
    }
}
