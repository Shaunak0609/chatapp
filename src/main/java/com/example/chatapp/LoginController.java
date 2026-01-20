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

    // Handle login form submission
    @PostMapping("/login")
    public ModelAndView doLogin(@RequestParam String username, HttpSession session) {
        ModelAndView mv = new ModelAndView();
        if (username == null || username.isEmpty()) {
            mv.setViewName("login");
            mv.addObject("error", "Username cannot be empty");
            return mv;
        }
        // Save username in session
        session.setAttribute("username", username);

        mv.setViewName("chat");
        mv.addObject("username", username);
        return mv;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // destroys session
        return "redirect:/login"; // go back to login page
    }
}