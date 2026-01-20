package com.example.chatapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "i think this works! hope it does. now it shud work with auto restar";
    }
    
}
