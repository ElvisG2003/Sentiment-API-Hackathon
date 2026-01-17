
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
// Importamos para logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Importamos para configurar timeouts
import org.springframework.http.client.SimpleClientHttpRequestFactory;
// Importamos para manejar unidades de tiempo
import java.util.concurrent.TimeUnit;


@Component
@ConditionalOnProperty(name = "sentiment.model.client", havingValue = "fastapi")
public class FastApiSentimentModelClient implements SentimentModelClient {
    
    private static final Logger log = LoggerFactory.getLogger(FastApiSentimentModelClient.class);
    private final String baseUrl;
    // Guardamos un RestClient con una base url
    private final RestClient restClient;
 

    // Constructo Spring que da la property
    // En caso de no existur property usa http://localhost:8000
    public FastApiSentimentModelClient(@Value("${sentiment.ds.base-url:http://localhost:8000}") String baseUrl) {
        // Asignamos la baseUrl
        this.baseUrl = baseUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(8000);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl) // Base URL del servicio FastAPI
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE) // Tipo de contenido JSON
                .build(); // Construimos el Cliente
    }


// Implementamos el metodo predict definido en la interfaz
@Override
public ModelResult predict(String text){
    long t0 = System.nanoTime();
    // Hacemos la llamada al servicio FastAPI
    try {
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

    }finally{
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        log.info("DS FastAPI call to {}/predict took {} ms", baseUrl, ms);
    }
}

    private record DsPredictRequest(String text){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DsPredictResponse(int label, double probability){}

}
