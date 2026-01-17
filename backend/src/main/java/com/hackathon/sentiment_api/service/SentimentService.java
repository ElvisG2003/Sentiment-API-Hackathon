package com.hackathon.sentiment_api.service;

import com.hackathon.sentiment_api.dto.SentimentResponse;

import com.hackathon.sentiment_api.exception.InvalidTextException;

import com.hackathon.sentiment_api.exception.ModelServiceExceptionMesagge;

import com.hackathon.sentiment_api.model.SentimentModelClient;

import org.slf4j.Logger; // Logger es de SLF4J(Simple Logging Facade for Java), se usa para escribir logs

import org.slf4j.LoggerFactory; // Fabrica para crear instancias de Logger 

import org.springframework.stereotype.Service; // Anotacion de Spring para marcar una clase como servicio

/*
    * Servicio para la prediccion de sentimiento:
    * Normalización de texto
    * Llamada al modelo 
    * Adapta el resultado al SentimentResponse DTO
 */
@Service
public class SentimentService {

    private static final Logger log = LoggerFactory.getLogger(SentimentService.class);

    private final SentimentModelClient modelClient;

    public SentimentService(SentimentModelClient modelClient) {
        this.modelClient = modelClient;
    }

    public SentimentResponse predict(String text) {

        // Normalizacion del texto
        String normalized = normalize(text);

        // Se usa isBlank para verificar si el texto es nulo o solo espacios en blanco, isEmpty no cubre el caso nulo
        if (normalized.isBlank()){
            throw new InvalidTextException("El texto de entrada no puede estar vacio");
        }

        
        boolean hasLetterOrDigit = normalized.codePoints().anyMatch(Character::isLetterOrDigit); // Verifica si hay al menos una letra o digito
        
        if (!hasLetterOrDigit) { // Si no hay letras ni digitos, lanza excepcion
            throw new InvalidTextException("El texto debe contener al menos una letra o un número.");
        }
        

        SentimentModelClient.ModelResult result; // Clase interna para representar el resultado del modelo
        try {
            result = modelClient.predict(normalized);
        } catch (Exception e) {
            // Captura cualquier excepcion al llamar al servicio del modelo y lo vuelve 503
            log.error("Error al llamar al servicio del modelo DS", e);
            throw new ModelServiceExceptionMesagge("Error al comunicarse con el servicio, informar o intenar mas tarde.", e);
        }

        String prediction = mapLabelToPrediction(result.label());

        return new SentimentResponse(prediction, result.probability(), result.label());
    }



    private String normalize(String text) {
        return (text == null) ? "" : text.trim();
    }

    private String mapLabelToPrediction(int label) {
        return switch (label) {
            case 1 -> "positive";
            case 0 -> "negative";
            default -> throw new IllegalArgumentException("Etiqueta de predicción desconocida: " + label);
        };
    }
}