package com.hackathon.sentiment_api.exception;

/*
    * Excepción para representar errores en el servicio del modelo
    * message: mensaje de error detallado
 */

public class ModelServiceExceptionMesagge extends RuntimeException {
    public ModelServiceExceptionMesagge(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelServiceExceptionMesagge(String message) {
        super(message);
    }

    
}