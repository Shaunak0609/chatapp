package com.example.chatapp;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedRooms(ChatRoomService chatRoomService) {
        return args -> {
            List.of("general", "gaming", "music", "random")
                .forEach(chatRoomService::getOrCreate);
        };
    }
}
