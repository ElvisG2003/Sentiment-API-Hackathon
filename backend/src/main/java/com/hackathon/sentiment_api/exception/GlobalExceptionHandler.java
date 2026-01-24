package com.hackathon.sentiment_api.exception;

import com.hackathon.sentiment_api.exception.ModelServiceExceptionMesagge;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.print.DocFlavor.STRING;

// Manejo general de excepciones: convierte errores en respuestas JSON consistentes
@RestControllerAdvice //Controller intercepta las excepciones de la API
public class GlobalExceptionHandler {

    //Preparamos un logger para mas adelante
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //A) Error de validación de @Valid en DTO (NotBlank, Size, etc.)
    //Este es el famoso error 400 (Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)//<- Se recibe sentimentRequest pero falla, ya sea blanck o size
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {

        // details es el "detalle del campo"
        // details = "El texto debe tener al menos 3 caracteres"
        Map<String, String> details = new LinkedHashMap<>();

        //Puede surgir que mas de un error aparezca, para evitar spam, solo imprimimos el primero
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        //El cuerpo de la respuesta
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),//-> El "400" no se ve, pero es este bad request
                "Validation error",// resumen del error
                request.getRequestURI(),//el path que fallo
                details// errores por campo
        );

        return ResponseEntity.badRequest().body(body);
    }

    // 2) Errores del dominio (InvalidTextException)
    // Este error es cuando la validacion basica se supera, pero no cumple una regla del dominio
    //Ej: "El texto solo tiene emojis", "Texto generico sin analisis", etc.
    @ExceptionHandler(InvalidTextException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidText(InvalidTextException ex,
                                                          HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", 400);
        body.put("error", ex.getMessage());
        body.put("path", request.getRequestURI());

        // antes: Map.of()
        body.put("details", Map.of("text", ex.getMessage()));

        return ResponseEntity.badRequest().body(body);
    }

    // 3) JSON mal echo (comillas mal, falta llave, etc.)
    // Por algun motivo el Sprong no paso a JSON
    /* Ej:
        * - { "text": "hola"   (faltó la llave de cierre)
        * - { text: "hola" }  (faltan comillas)
        * - { "text": 123 }   (si se esperaba String, a veces cae aquí)
    / */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {


        log.warn("Malformed JSON request en {}", request.getRequestURI()); // Alerta en logs
        log.debug("Detalle técnico de Json malformado", ex); // Para debuguear
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON request", //El mensaje es claro
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.badRequest().body(body);
    }

    // 4) Catch-all: error inesperado (500)
    // Arriba conteplamos errores basicos; sin embargo, pueden nacer otros, hasta que no logremos categorizarlos, saltara este.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {

        // Debido a que no tenemos detalles del error, no damos datos reales
        // Para mas profesionalismo usamos un logger
        log.error("Error interno del servidor en {}", request.getRequestURI(), ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), //500
                "Internal server error",                  //Mensaje seguro
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 5) Error al llamar al servicio del modelo (503)
    // Cuando el servicio del modelo (FastAPI) no responde o da error
    @ExceptionHandler(ModelServiceExceptionMesagge.class)
    public ResponseEntity<ErrorResponse> handleModelServiceException(ModelServiceExceptionMesagge ex,
                                                                    HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(), //503
                ex.getMessage(),                        //Mensaje controlado
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
