package com.hackathon.sentiment_api.dto;

/*
    * Dto para la respuesta de la prediccion
    * prediction: "positive"/"negative"
    * probability: confianza del modelo (0.0 - 1.0)
    * label: entero 0/1
*/
public class SentimentResponse {
    private String prediction;
    private double probability;
    private Integer label; 

    public SentimentResponse() {
        // Constructor vacio
    }

    public SentimentResponse(String prediction, double probability, Integer label) {
        this.prediction = prediction;
        this.probability = probability;
        this.label = label;
    }

    /*
        * Constructor legacy sin label
        * Se deja para compatibilidad hacia atras
     */
    public SentimentResponse(String prediction, double probability) {
        this(prediction, probability, null);
    }

    public String getPrediction() {
        return this.prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public double getProbability() {
        return this.probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public Integer getLabel() {
        return this.label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }
}
