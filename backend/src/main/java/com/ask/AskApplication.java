package com.ask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Apna Swasthya Kendra (ASK) application.
 * A healthcare retail management platform serving 200+ stores across Bihar, UP, and Jharkhand.
 */
@SpringBootApplication
@EnableAsync
public class AskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AskApplication.class, args);
    }
}
