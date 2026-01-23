package com.hackathon.sentiment_api.controller;

import com.hackathon.sentiment_api.dto.SentimentRequest;
import com.hackathon.sentiment_api.dto.SentimentResponse;
import com.hackathon.sentiment_api.model.SentimentModelClient;
import com.hackathon.sentiment_api.service.SentimentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;


import java.util.LinkedHashMap;
import java.util.Map;

//Esta es la capa de entrada de la API, se recibe el HTTP, se valida el input y delega la logica
@RestController
@RequestMapping({"/sentiment"})//Ruta del recurso
public class SentimentController {
    //Se crea el servicio, Spring lo inyecta
    private final SentimentService sentimentService;

    private final SentimentModelClient modelClient; // Cliente del modelo

    @Value("${sentiment.ds.health-check.enabled:true}")
    private boolean dsHealthCheckEnabled;


    // Se inyecta la property de la URL del DS
    @Value("${sentiment.ds.base-url:http://localhost:8000}")
    private String dsBaseUrl; 


    private static final Logger log = LoggerFactory.getLogger(SentimentController.class);


    public SentimentController(SentimentService sentimentService, SentimentModelClient modelClient) {
        this.sentimentService = sentimentService;
        this.modelClient = modelClient;
    }

    /*Se crea POST /sentiment
    * Endpoint principal de analisis
    * Se delega la logica a service
    * si todo sale bien, devuelve HTTP 200 */
    @PostMapping
    public ResponseEntity<SentimentResponse> predict(@RequestBody @Valid SentimentRequest request) {
        log.debug("POST /sentiment recibido");
        SentimentResponse response = this.sentimentService.predict(request.getText());
        return ResponseEntity.ok(response);
    }

    /*Se crea GET /health
    * Es un endpoint simple de salud*/
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    /* Se crea GET /health/deps
    * Endpoint de salud extendido que chequea dependencias
    */
    @GetMapping("/health/deps")
    public ResponseEntity<Map<String, Object>> healthDeps() {
        // Mapa de salida
        Map<String, Object> out = new LinkedHashMap<>(); // Mantiene el orden de inserción
        out.put("backend", "ok");  
        out.put("modelClient", modelClient.getClass().getSimpleName());
        
        if (!dsHealthCheckEnabled) {
            out.put("ds", "skipped");
            return ResponseEntity.ok(out);
        }
        // Comprobamos la salud del servicio FastAPI 
        try {
            var requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(2)); // 2 segundos
            requestFactory.setReadTimeout(java.time.Duration.ofSeconds(2)); // 2 segundos

            RestClient rc = RestClient.builder()
                    .requestFactory(requestFactory)
                    .baseUrl(dsBaseUrl)
                    .build();
            Map<?, ?> ds = rc.get().uri("/health").retrieve().body(Map.class);
            out.put("ds", ds != null ? ds : Map.of("status", "unknown"));
            return ResponseEntity.ok(out);
        } catch (Exception ex) {
            out.put("ds", "down");
            out.put("error", "DS unreachable");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(out);
        }
    }

}
