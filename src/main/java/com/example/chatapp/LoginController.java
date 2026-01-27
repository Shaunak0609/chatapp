package com.example.chatapp;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    private final ChatRoomService chatRoomService;

    public LoginController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    /**
     * Login page (handled by Spring Security)
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Main chat page (SECURED)
     */
    @GetMapping("/chat")
    public ModelAndView chat(
            @RequestParam(defaultValue = "general") String room,
            Principal principal
    ) {
        ModelAndView mv = new ModelAndView("chat");

        mv.addObject("room", room);
        mv.addObject("username", principal.getName()); 
        mv.addObject("rooms", chatRoomService.findAll());

        return mv;
    }
}
