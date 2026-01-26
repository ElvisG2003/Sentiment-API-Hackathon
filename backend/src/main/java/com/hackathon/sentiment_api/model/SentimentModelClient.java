package com.hackathon.sentiment_api.model;

// Interfaz (contrato) para obtener predicciones
public interface SentimentModelClient {

    /*predict es la entrada de texto que se analiza
    * label(0/1) + prohability*/
    default ModelResult predict(String text){
        return predict(text,null);
    }

    ModelResult predict(String text, String model);

    //Label es un numero entero para los sentimientos trabajados por DS
    //probability: confianza del modelo para la clase 0.0 y 1.0
    record ModelResult(int label, double probability){}
}
