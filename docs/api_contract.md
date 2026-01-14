# API Contract

Este proyecto tiene **2 APIs**

1) **Backend (Spring Boot)**: API pública
2) **Data Science (FastAPI)**: Microservicio interno

## 1) BACKEND

### 1.1 POST `/sentiment`
- Clasifica un texto en **binario**: `positive / negative`.

#### Request (JSON)
```json
{
  "text": "The delivery was late, and the product was in a bad state"
}
```
#### Validaciones

* `text` :no puede estar vacio
* `text`: debe tener mínimo 3 caracteres

#### Response 200(JSON)
```json
{
  "label": 0,
  "prediction": "NEGATIVE"
  "probability": 0.82
}
```
* prediction: "positive" o "negative"
* probability: probabilidad asociada a la clase positiva (0.0 a 1.0)

---

### 1.2 Get `/sentiment/health`
- Health simple para verificar que el backend esta sano.

#### Response 200
```
{
  OK
}
```
---

### 1.3 Errores
- Formato de erores:
```json
{
  "timestamp": "2026-01-10T12:34:56.789-03:00",
  "status": 400,
  "error": "Validation error",
  "path": "/sentiment",
  "details": {
    "text": "El texto debe tener al menos 3 caracteres"
  }
}

```
- Campos:
  * status: codigo HTTP
  * error: mensaje resumido
  * path: endpoint que falló
  * details: mensaje del error

#### 400 Validation error
- Cuando falla `@Valid` en `SentimentRequest`. 

#### 400 Malformed JSON request
- Cuando el JSON viene roto 

#### 500 internal server error
- Error inesperado (no exponemos datos sensibles)


## 2) Data Science (FastAPI)

### 2.1 POST `/predict`
- Devuelve etiqueta binaria y probabilidad.

#### Request(JSON)
```json
{
  "error": "DS_UNAVAILABLE",
  "message": "Data Science service not reachable"
}
```

#### Validaciones
* `text`: minimo 3 caracteres, máximo 5000
*  si luego de limpiar el texto queda vacio -> error 422

#### Response 200(JSON)
```JSON
{
  "label": 1,
  "probability": 0.93
}
```
* label: 1 = positivo; 0 = negativo
* probability: probabilidad asociada a la clase positiva 

---

#### 2.2 GET `/health`
- Devuelve el estado del servicio.

* Aun no carga el modelo
```JSON
{
  "status": "starting"
}
```
* En caso de estar funcionando
```JSON
{
  "status": "OK"
}
```
---

#### 2.3 GET `/health-check`

-Chequeo simple 

```JSON
{
  "status": "ok"
}
```
* 422: texto inválido 
* 503: modelo aún no cargado
* 500: error durante la prediccion