package com.example.chatapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 👤 TEMP USERS (FOR DEVELOPMENT)
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {

        UserDetails shaunak = User.builder()
                .username("shaunak")
                .password(encoder.encode("password"))
                .roles("USER")
                .build();

        UserDetails test = User.builder()
                .username("test")
                .password(encoder.encode("test123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(shaunak, test);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ⚠️ Disable CSRF (required for WebSockets)
            .csrf(csrf -> csrf.disable())

            // 🔓 PUBLIC ROUTES
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/login",
                            "/styles.css",
                            "/js/**",
                            "/images/**",
                            "/ws/**"
                    ).permitAll()

                   
                    .anyRequest().authenticated()
            )

          
            .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/chat?room=general", true)
                    .permitAll()
            )

            
            .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            );

        return http.build();
    }
}
