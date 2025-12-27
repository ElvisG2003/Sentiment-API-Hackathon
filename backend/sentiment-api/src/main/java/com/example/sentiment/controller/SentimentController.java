package com.example.sentiment.controller;

import com.example.sentiment.dto.SentimentRequest;
import com.example.sentiment.dto.SentimentResponse;
import com.example.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sentiment")
public class SentimentController {

    private final SentimentService sentimentService;

    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @PostMapping
    public ResponseEntity<SentimentResponse> analyze(
            @Valid @RequestBody SentimentRequest request) {

        return ResponseEntity.ok(
                sentimentService.analyze(request.getText())
        );
    }
}
