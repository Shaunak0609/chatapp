package com.example.chatapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❌ CSRF breaks WebSockets
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // allow static files
                .requestMatchers("/styles.css").permitAll()

                // allow login page
                .requestMatchers("/login").permitAll()

                // 🔥 allow WebSocket handshake
                .requestMatchers("/chat").permitAll()

                // everything else requires auth
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/chat", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }
}
