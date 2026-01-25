package com.example.chatapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final ChatRoomService chatRoomService;

    public LoginController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("login");
    }

    @PostMapping("/login")
    public ModelAndView doLogin(@RequestParam String username, HttpSession session) {
        if (username == null || username.isBlank()) {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("error", "Username required");
            return mv;
        }

        session.setAttribute("username", username);
        return new ModelAndView("redirect:/chat");
    }

    @GetMapping("/chat")
    public ModelAndView chat(
            @RequestParam(defaultValue = "general") String room,
            HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) return new ModelAndView("redirect:/login");

        chatRoomService.getOrCreateRoom(room);

        ModelAndView mv = new ModelAndView("chat");
        mv.addObject("username", username);
        mv.addObject("room", room);
        mv.addObject("rooms", chatRoomService.findAll());
        return mv;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam String room) {
        if (!room.equals("general")) {
            chatRoomService.deleteRoom(room);
        }
        return "redirect:/chat";
    }

}
