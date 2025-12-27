package com.example.sentiment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SentimentRequest {

    @NotBlank(message = "El texto es obligatorio")
    @Size(min = 5, message = "El texto debe tener al menos 5 caracteres")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
