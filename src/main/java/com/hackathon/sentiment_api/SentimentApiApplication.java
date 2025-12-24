package com.hackathon.sentiment_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SentimentApiApplication {
    public SentimentApiApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(SentimentApiApplication.class, args);
    }
}
