package com.hackathon.sentiment_api.service;

import com.hackathon.sentiment_api.dto.SentimentResponse;
import org.springframework.stereotype.Service;

@Service
public class SentimentService {
    public SentimentService() {
    }

    public SentimentResponse predict(String text) {
        String loweredText = text.toLowerCase();
        if (!loweredText.contains("good") && !loweredText.contains("happy") && !loweredText.contains("great") && !loweredText.contains("excellent") && !loweredText.contains("love")) {
            return !loweredText.contains("bad") && !loweredText.contains("sad") && !loweredText.contains("terrible") && !loweredText.contains("awful") && !loweredText.contains("stopped working") ? new SentimentResponse("NEGATIVE", 0.6) : new SentimentResponse("NEGATIVE", 0.85);
        } else {
            return new SentimentResponse("POSITIVE", 0.85);
        }
    }
}
