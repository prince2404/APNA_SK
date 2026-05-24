package com.ask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Apna Swasthya Kendra (ASK) application.
 * A healthcare retail management platform serving 200+ stores across Bihar, UP, and Jharkhand.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AskApplication.class, args);
    }
}
