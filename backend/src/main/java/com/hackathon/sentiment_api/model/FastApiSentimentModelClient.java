
// Definimos donde vive el paquete
package com.hackathon.sentiment_api.model;
// Importamos para ignorar campos extras cuando parseamos JSON

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.sentiment_api.exception.InvalidTextException;
import com.hackathon.sentiment_api.exception.ModelServiceExceptionMesagge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.TimeUnit;


@Component
@ConditionalOnProperty(name = "sentiment.model.client", havingValue = "fastapi")
public class FastApiSentimentModelClient implements SentimentModelClient {
    
    private static final Logger log = LoggerFactory.getLogger(FastApiSentimentModelClient.class);
    private final String baseUrl;
    // Guardamos un RestClient con una base url
    private final RestClient restClient;
    private static final ObjectMapper MAPPER = new ObjectMapper(); // Mapeador JSON

    private String extractDetail (String body){
        if (body == null || body.isBlank()) return "Sin detalles";
        try {
            JsonNode node = MAPPER.readTree(body);
            // Extraemos el campo "detail" si existe
            if (node.has("detail") && !node.get("detail").isNull()) return node.get("detail").asText();
            else return "Sin detalles";
        } catch (Exception e){ // Si hay error al parsear, devolvemos null
            return null;
        }
    }
 

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
public ModelResult predict(String text, String model){
    long t0 = System.nanoTime();
    // Hacemos la llamada al servicio FastAPI
    try {
        // Hacemos la llamada POST al endpoint /predict de FastAPI
        DsPredictResponse resp = restClient.post() // Post Request
            .uri("/predict") // path del endpoint
            .contentType(MediaType.APPLICATION_JSON) // Enviamos JSON
            .body(new DsPredictRequest(text, model)) // body: {"text": "texto a analizar"}
            .retrieve() // Ejecutamos la llamada
            .body(DsPredictResponse.class); // Parseamos la respuesta a DsPredictResponse


        // si no hay respuesta
        if (resp == null){
            throw new ModelServiceExceptionMesagge("El servicio DS devolvio una respuesta vacía");
        }
        // Convertimos la respuesta al formato de nuestra interfaz
        return new ModelResult(resp.label(), resp.probability());

    } catch (RestClientResponseException ex) {
        int statusCode = ex.getStatusCode().value();// Codigo de estado HTTP
        String detail = extractDetail(ex.getResponseBodyAsString());

        if (statusCode == 404) {
            String msg = (detail != null) ? detail :"Modelo desconocido";
            throw new InvalidTextException(msg);
        }

        // Manejo de errores basado en el código de estado HTTP
        if (statusCode == 422){
            String msg = (detail != null)
             ? detail 
             : "Texto inválido para análisis de sentimiento.";
            throw new InvalidTextException(msg);
        }
        if (statusCode == 503){
             String msg = (detail != null) 
             ? detail 
             : "El modelo DS aún no esta disponible, intente más tarde.";
            throw new ModelServiceExceptionMesagge(msg);
        }

        String msg = (detail != null)
             ? detail 
             : "Error del servicio DS.(HTTP " + statusCode +").";
        throw new ModelServiceExceptionMesagge(msg);
    }catch(Exception ex){
        throw new ModelServiceExceptionMesagge("Error al conectar con el servicio DS, verifique que DS este conectado."  );
    }finally {
        long passMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        log.info("Tiempo de respuesta de DS: {}/predict {} ms", baseUrl, passMs);

    }
}

    private record DsPredictRequest(String text, String model){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DsPredictResponse(int label, double probability){}

}
