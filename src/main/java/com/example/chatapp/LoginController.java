package com.example.chatapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {


    // Show login page
    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("login"); // maps to login.html
    }

    @PostMapping("/login")
    public ModelAndView doLogin(
            @RequestParam String username,
            @RequestParam String room,
            HttpSession session) {

        ModelAndView mv = new ModelAndView();

        if (username == null || username.isEmpty()
                || room == null || room.isEmpty()) {

            mv.setViewName("login");
            mv.addObject("error", "Username and room are required");
            return mv;
        }

        session.setAttribute("username", username);
        session.setAttribute("room", room);

        mv.setViewName("chat");
        mv.addObject("username", username);
        mv.addObject("room", room);

        return mv;
}


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // destroys session
        return "redirect:/login"; // go back to login page
    }
}