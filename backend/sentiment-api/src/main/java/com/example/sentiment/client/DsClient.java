package com.example.sentiment.client;

import com.example.sentiment.dto.SentimentResponse;
import com.example.sentiment.exception.DsServiceUnavailableException;
import com.example.sentiment.service.SentimentModelClient;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;


@Primary
@Component
public class DsClient implements SentimentModelClient {

    private final RestClient restClient;

    public DsClient() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(500);   // ms
        factory.setReadTimeout(2000);     // ms

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .requestFactory(factory)
                .build();
    }

    @Override
    public SentimentResponse predict(String text) {
        try {
            DsResponse dsResponse = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", text))
                    .retrieve()
                    .body(DsResponse.class);

            if (dsResponse == null) {
                throw new DsServiceUnavailableException(
                        "Respuesta inválida desde DS"
                );
            }

            //Mapeo DS → API
            String prevision =
                    dsResponse.getLabel() == 1 ? "Positivo" : "Negativo";

            return new SentimentResponse(
                    prevision,
                    dsResponse.getProbability()
            );

        } catch (RestClientException ex) {
            throw new DsServiceUnavailableException(
                    "DS service unavailable", ex
            );
        }
    }
}
