package com.hackathon.sentiment_api.controller;

import com.hackathon.sentiment_api.dto.SentimentRequest;
import com.hackathon.sentiment_api.dto.SentimentResponse;
import com.hackathon.sentiment_api.service.SentimentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//Esta es la capa de entrada de la API, se recibe el HTTP, se valida el input y delega la logica
@RestController
@RequestMapping({"/sentiment"})//Ruta del recurso
public class SentimentController {
    //Se crea el servicio, Spring lo inyecta
    private final SentimentService sentimentService;
    private static final Logger log = LoggerFactory.getLogger(SentimentController.class);


    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    /*Se crea POST /sentiment
    * Endpoint principal de analisis
    * Se delega la logica a service
    * si todo sale bien, devuelve HTTP 200 */
    @PostMapping
    public ResponseEntity<SentimentResponse> predict(@RequestBody @Valid SentimentRequest request) {
        log.info("POST /sentiment recibido");
        SentimentResponse response = this.sentimentService.predict(request.getText());
        return ResponseEntity.ok(response);
    }

    /*Se crea GET /health
    * Es un endpoint simple de salud*/
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
