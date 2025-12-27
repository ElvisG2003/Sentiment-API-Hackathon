package com.example.sentiment.service;

import com.example.sentiment.dto.SentimentResponse;

public interface SentimentModelClient {

    SentimentResponse predict(String text);
}
