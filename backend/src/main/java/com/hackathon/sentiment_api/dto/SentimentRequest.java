package com.hackathon.sentiment_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SentimentRequest {
    private String model;

    private @NotBlank(
            message = "El texto no puede estar vacio"
    ) @Size(
            min = 3, max = 5000,
            message = "El texto debe tener al menos 3 caracteres"
    ) String text;

    public SentimentRequest() {
    }

    public SentimentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getModel(){
        return model;
    }

    public void setModel(String model){
        this.model = model;
    }

}
