package com.example.doantn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DoAnTnApplication {
    public static void main(String[] args) {
        SpringApplication.run(DoAnTnApplication.class, args);
    }
} 