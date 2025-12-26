package com.hackathon.sentiment_api.exception;

//¿Porque usar InvalidTextException(ITE) y no IllegalArgumentException(IAE)?
//IAE es generico, por lo que no distingue errores de negocio con tecnicos
//Crear ITE permite un codigo mas legible y enviamos mensajes controlados

//Excepcion para textos invalidos durante el analisis de sentimiento
public class InvalidTextException extends RuntimeException {
    //RuntimeException permite no forzar try/catch
    //Esto sera capturado por GlobalExceptionHandler

    public InvalidTextException(String message){
        super(message);
    }
}
