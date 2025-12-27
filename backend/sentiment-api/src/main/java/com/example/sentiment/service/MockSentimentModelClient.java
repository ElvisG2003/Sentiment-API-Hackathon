package com.example.sentiment.service;

import com.example.sentiment.dto.SentimentResponse;
import org.springframework.stereotype.Component;

@Component
public class MockSentimentModelClient
        implements SentimentModelClient {

    @Override
    public SentimentResponse predict(String text) {

        if (text.toLowerCase().contains("mal")) {
            return new SentimentResponse("Negativo", 0.85);
        }

        return new SentimentResponse("Positivo", 0.85);
    }
}
