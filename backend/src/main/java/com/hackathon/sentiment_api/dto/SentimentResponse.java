package com.hackathon.sentiment_api.dto;

public class SentimentResponse {
    private String prediction;
    private double probability;

    public SentimentResponse() {
    }

    public SentimentResponse(String prediction, double probability) {
        this.prediction = prediction;
        this.probability = probability;
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
}
