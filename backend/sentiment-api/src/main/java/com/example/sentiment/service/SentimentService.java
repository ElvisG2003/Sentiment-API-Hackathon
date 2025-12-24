package com.example.sentiment.service;

import com.example.sentiment.dto.SentimentRequest;
import com.example.sentiment.dto.SentimentResponse;
import org.springframework.stereotype.Service;

@Service
public class SentimentService {

    public SentimentResponse analyze(SentimentRequest request) {

        // luego va integracion con DS
        return new SentimentResponse("Positivo", 0.85);
    }
}
