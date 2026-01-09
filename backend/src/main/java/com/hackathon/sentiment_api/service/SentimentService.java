package com.hackathon.sentiment_api.service;

import com.hackathon.sentiment_api.model.SentimentModelClient;
import com.hackathon.sentiment_api.dto.SentimentResponse;
import org.springframework.stereotype.Service;

@Service
public class SentimentService {

    // Dependencia al cliente del modelo
    private final SentimentModelClient modelClient;

    // Contructor para inyectar la dependencia
    public SentimentService(SentimentModelClient modelClient) {
        this.modelClient = modelClient;
    }

    // Metodo que hace la prediccion
    public SentimentResponse predict(String text) {
        
        // Llamamos al modelo
        SentimentModelClient.ModelResult result = modelClient.predict(text);

        // Convertimos la respuesta al formato de la API
        String prediction = (result.label() == 1) ? "positive" : "negative";

        // Creamos y retornamos el DTO
        return new SentimentResponse(prediction, result.probability());
    }
}
