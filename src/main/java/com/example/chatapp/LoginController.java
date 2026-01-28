package com.example.chatapp;

import java.security.Principal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatRoomService chatRoomService;

    public LoginController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ChatRoomService chatRoomService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.chatRoomService = chatRoomService;
    }

    // ---------- LOGIN ----------
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ---------- REGISTER ----------
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error";
        }

        userRepository.save(
            new User(username, passwordEncoder.encode(password))
        );

        return "redirect:/login";
    }

    // ---------- CHAT ----------
    @GetMapping("/chat")
    public String chat(@RequestParam String room,
                       Principal principal,
                       Model model) {

        chatRoomService.getOrCreateRoom(room);

        model.addAttribute("room", room);
        model.addAttribute("username", principal.getName());
        model.addAttribute("rooms", chatRoomService.findAll());

        return "chat";
    }

    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam String room) {

        if (!room.equals("general")) {
            chatRoomService.deleteRoom(room);

           
            ChatWebSocketHandler.notifyRoomDeleted(room);
        }

        return "redirect:/chat?room=general";
    }

}
