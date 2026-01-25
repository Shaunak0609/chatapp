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
    private final ChatWebSocketHandler chatWebSocketHandler;

    public LoginController(ChatRoomService chatRoomService,
                           ChatWebSocketHandler chatWebSocketHandler) {
        this.chatRoomService = chatRoomService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("login");
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, HttpSession session) {
        session.setAttribute("username", username);
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public ModelAndView chat(@RequestParam(defaultValue = "general") String room,
                             HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) return new ModelAndView("redirect:/login");

        if (!chatRoomService.exists(room)) {
            return new ModelAndView("redirect:/chat?room=general");
        }

        chatRoomService.getOrCreateRoom(room);

        ModelAndView mv = new ModelAndView("chat");
        mv.addObject("username", username);
        mv.addObject("room", room);
        mv.addObject("rooms", chatRoomService.findAll());
        return mv;
    }

    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam String room) throws Exception {
        if (!room.equals("general")) {
            chatRoomService.deleteRoom(room);
            chatWebSocketHandler.closeRoom(room);
        }
        return "redirect:/chat";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
