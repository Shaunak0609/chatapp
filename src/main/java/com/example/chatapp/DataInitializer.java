package com.example.chatapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedRooms(ChatRoomRepository repo) {
        return args -> {
            String[] rooms = { "general", "gaming", "music", "random" };

            for (String room : rooms) {
                repo.findByName(room)
                    .orElseGet(() -> repo.save(new ChatRoom(room)));
            }
        };
    }
}
