package com.example.chatapp;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatRoomService chatRoomService;
    private final RoomMembershipService roomMembershipService;
    private final RoomInviteService roomInviteService;

    public LoginController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ChatRoomService chatRoomService,
                           RoomMembershipService roomMembershipService,
                           RoomInviteService roomInviteService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.chatRoomService = chatRoomService;
        this.roomMembershipService = roomMembershipService;
        this.roomInviteService = roomInviteService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error";
        }
        userRepository.save(new User(username, passwordEncoder.encode(password)));
        return "redirect:/login";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String room,
                       Principal principal,
                       Model model,
                       RedirectAttributes ra) {

        // Ensure the room exists (creates it as public if brand new)
        chatRoomService.getOrCreateRoom(room);

        // Private room access check
        Optional<ChatRoom> roomOpt = chatRoomService.findRoom(room);
        if (roomOpt.isPresent() && roomOpt.get().isPrivateRoom()) {
            if (!roomMembershipService.isMember(principal.getName(), room)) {
                ra.addFlashAttribute("chatError",
                        "\"" + room + "\" is a private room — you need an invite to join.");
                return "redirect:/chat?room=general";
            }
        }

        // Record join time (idempotent — also creates membership row for public rooms)
        roomMembershipService.getOrCreateJoinTime(principal.getName(), room);

        // Rooms visible to this user: all public + private rooms they are a member of
        List<ChatRoom> allRooms = chatRoomService.findAll();
        List<ChatRoom> visibleRooms = allRooms.stream()
                .filter(r -> !r.isPrivateRoom()
                          || roomMembershipService.isMember(principal.getName(), r.getName()))
                .collect(Collectors.toList());

        // Other usernames for the invite picker (exclude self)
        List<String> allUsernames = userRepository.findAll().stream()
                .map(User::getUsername)
                .filter(name -> !name.equals(principal.getName()))
                .collect(Collectors.toList());

        model.addAttribute("room", room);
        model.addAttribute("username", principal.getName());
        model.addAttribute("rooms", visibleRooms);
        model.addAttribute("currentRoom", roomOpt.orElse(null));
        model.addAttribute("pendingInvites",
                roomInviteService.getPendingInvites(principal.getName()));
        model.addAttribute("allUsernames", allUsernames);

        return "chat";
    }

    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam String room,
                             Principal principal,
                             RedirectAttributes ra) {

        if (room.equals("general")) {
            return "redirect:/chat?room=general";
        }

        Optional<ChatRoom> roomOpt = chatRoomService.findRoom(room);
        if (roomOpt.isPresent() && roomOpt.get().isPrivateRoom()) {
            if (!roomMembershipService.isOwner(principal.getName(), room)) {
                ra.addFlashAttribute("chatError",
                        "Only the owner of \"" + room + "\" can delete it.");
                return "redirect:/chat?room=" + room;
            }
        }

        chatRoomService.deleteRoom(room);
        ChatWebSocketHandler.notifyRoomDeleted(room);
        return "redirect:/chat?room=general";
    }
}
