package com.oees.oees;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordHashRunner {

    @Bean
    CommandLineRunner printHash() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            System.out.println("HASHED_PASSWORD = " + encoder.encode("password"));
        };
    }
}