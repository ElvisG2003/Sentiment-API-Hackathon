# Arquitectura

## Componentes
### Backend — Spring Boot (API pública)
- Recibe requests HTTP (JSON)
- Valida el texto
- Hace el llamado al microservicio DS (FastAPI) mediante `SentimentModelClient`
- Devuelve: 
    * `prediction`, `label`, `probability`
    * ademas de `positiveProbability` y `negativeProbability` para interpretación

- Puede usar:
  - **Mock client** (sin DS)
  - **FastAPI client** (llama microservicio real)

### Data Science — FastAPI (microservicio interno)
- Carga artefactos de modelo desde `data-science/artifacts/`
- Expone `POST /predict` para inferencia
- Expone endpoints de health

---

## Puertos y endpoints

### Backend (Spring Boot) — API pública
- Base URL: `http://localhost:8080`
- `POST /sentiment`
- `GET /sentiment/health`

### Data Science (FastAPI) — microservicio interno
- Base URL: `http://localhost:8000`
- `POST /predict`
- `GET /health`
- `GET /health-check`

---

## Flujo end-to-end

1) **Cliente** envía `POST /sentiment` al backend con:
```json
{ "text": "I love this product" }
```
2) **Controller** (Spring Boot)
* Valida el input`(@Valid)`
    * `text` no vacío
    * minimo 3 caracteres
    
3) **Service** llama a 
* `SentimentModelClient.predict(text)`

4) **`SentimentModelClient`** tiene 2 modos:
* **Modo mock**(`sentiment.model.client=mock`):
    * No llama a FastAPI
    * Sirve para avanzar sin depender del microservicio DS

* **Modo fastapi** (`sentiment.model.client=fastapi`):
    * El backend hace `POST /predict` al servicio DS con:
    ```JSON
    {"text": "..."}
    ```

    * DS responde:
    ```JSON
    {"label": 0|1, "probability": 0.xx }
    ```

5) El **backend adapta** la respuesta al formato publico:
* `label=1 -> "positive"`
* `label=0 -> "negative"`
* `probability`
* `positiveProbability = probability`
* `negativeProbability = 1 - probability`

Respuesta publica:

```json
    {
        "label": 1,
        "prediction": "positive",
        "probability": 0.93,
        "positiveProbability": 0.93,
        "negativeProbability": 0.07
    }
```

---

## Configuracion clave

Archivo:
`backend/src/main/resources/application.properties`
* `sentiment.model.client`
    * `mock` (default)
    * `fastapi` (integracionreal)

* `sentiment.ds.base-url`
    * por defecto: `http://localhost:8000`

---

## Manejo de errores 

### Backend
* **400**: validacion -> ErrorResponse con `details`
* **400**: JSON mal formado -> ErrorResponse `"Malformed JSON request"`
* **503**: DS no disponible / error de integración
* **500**: error inesperado -> ErrorResponse `"Internal server error"`

### Data Science
* **422**: el texto queda vacío después de limpieza
* **503**: el modelo aún no cargó
* **500**: error durante la predicción