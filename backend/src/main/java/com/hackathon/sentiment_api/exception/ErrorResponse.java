package com.hackathon.sentiment_api.exception;

import java.time.OffsetDateTime;
import java.util.Map;

//Se trabaja las respuestas de error para que salgan con un mismo formato
public class ErrorResponse {
    private final String timestamp; //Se entrega fechas y horas del error
    private final int status;//Error entregado en formato HTTP
    private final String error;//Tipo de error
    private final String path;//Endpoint fallido
    private final Map<String, String> details;//Detalles con campo de texto

    public ErrorResponse(int status, String error, String path, Map<String, String> details){//Formato que imprimer JSON
        this.timestamp = OffsetDateTime.now().toString();
        this.status = status;
        this.error = error;
        this.path = path;
        this.details = details;
    }

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getPath() { return path; }
    public Map<String, String> getDetails() { return details; }
}
