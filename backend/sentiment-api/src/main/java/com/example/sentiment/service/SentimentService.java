package com.example.sentiment.service;

import com.example.sentiment.dto.SentimentResponse;
import com.example.sentiment.exception.InvalidTextException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





@Service
public class SentimentService {

    private static final Logger log =
            LoggerFactory.getLogger(SentimentService.class);

    private final SentimentModelClient modelClient;

    public SentimentService(SentimentModelClient modelClient) {
        this.modelClient = modelClient;
    }

    public SentimentResponse analyze(String text) {
        log.info("Analizando texto");

        if (text == null || text.trim().length() < 5) {
            log.warn("Texto inválido recibido");
            throw new InvalidTextException(
                    "El texto debe tener al menos 5 caracteres");
        }
        SentimentResponse response = modelClient.predict(text);
        log.info("Resultado del análisis: {}", response.getPrevision());
        return response;
    }
}
