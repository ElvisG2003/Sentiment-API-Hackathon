// Definimos donde vive el paquete
package com.hackathon.sentiment_api.model;
// Importamos para ignorar campos extras cuando parseamos JSON
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// Importamos para leer valores de application.properties
import org.springframework.beans.factory.annotation.Value;
// Importamos para activar la clase solo si una propiedad esta activa
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// Importamos para decir que enviamos JSON
import org.springframework.http.MediaType;
// Component para que Spring lo detecte como un Bean
import org.springframework.stereotype.Component;
// RestClient para hacer llamadas HTTP
import org.springframework.web.client.RestClient;


@Component
@ConditionalOnProperty(name = "sentiment.model.client", havingValue = "fastapi")
public class FastApiSentimentModelClient implements SentimentModelClient {
    // Guardamos un RestClient con una base url
    private final RestClient restClient;

    // Constructo Spring que da la property
    // En caso de no existur property usa http://localhost:8000
    public FastApiSentimentModelClient(@Value("${sentiment.ds.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = org.springframework.web.client.RestClient.builder()
                .baseUrl(baseUrl) // Base URL del servicio FastAPI
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE) // Tipo de contenido JSON
                .build(); // Construimos el Cliente
    }

// Implementamos el metodo predict definido en la interfaz
@Override
public ModelResult predict(String text){
    // Hacemos la llamada POST al endpoint /predict de FastAPI
    DsPredictResponse resp = restClient.post() // Post Request
            .uri("/predict") // path del endpoint
            .contentType(MediaType.APPLICATION_JSON) // Enviamos JSON
            .body(new DsPredictRequest(text)) // body: {"text": "texto a analizar"}
            .retrieve() // Ejecutamos la llamada
            .body(DsPredictResponse.class); // Parseamos la respuesta a DsPredictResponse

    // si no hay respuesta
    if (resp == null){
        throw new RuntimeException("Ds service returned empty response");
    }
    // Convertimos la respuesta al formato de nuestra interfaz
    return new ModelResult(resp.label(), resp.probability());
}

private record DsPredictRequest(String text){}

@JsonIgnoreProperties(ignoreUnknown = true)

private record DsPredictResponse(int label, double probability){}

}