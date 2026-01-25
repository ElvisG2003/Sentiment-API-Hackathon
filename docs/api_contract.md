# API Contract

Este documento busca definir el contrato entre 2 APIs:

- **Web UI** -> **Backend**(Spring Boot)
- **Backend** -> **Model Service** (FastAPI)

---

## Convenciones (importante)

### Semántica de `probability`
En **todo el sistema** (DS y Backend):
- `probability` = **Probabilidad Positiva**
- `positiveProbability` == `probability`
- `negativeProbability` == 1 -`probability`

### Content-Type
- Requests: `application/json`
- Responses: `application/json`

---

## 1) BACKEND

### Url Base (local)

-  `http://localhost:8080`

### 1.1 Health
- Health simple para verificar que el backend esta sano.

**GET** `/sentiment/health`

#### Response 200 (text)

```json
{
  "Status": "OK"
}
```

---

### **GET** `/sentiment/health/deps`
- Verifica el estado del backend y la conectividad del FastAPI.

**Notas**
- Si el cliente es `Mock`, el estado de DS no es relevante 
- Si el cliente es `FastAPI` y el `sentiment.ds.health-check.enabled=true`, se debe consultar `Get {sentiment.ds.base-url}/health`
- Si Ds no responde, el endpoint deberia reflejarlo

#### Response 200 (application/json) - Activado Health-check

```json
{
  "backend":"ok",
  "modelClient": "FastApiSentimentModelClient",
  "ds":{"status": "ok"}
}
```

#### Response 200 (application/json) - Desactivado Health-check

```json
{
  "backend":"ok",
  "modelClient": "FastApiSentimentModelClient",
  "ds":"skipped"
}
```

#### Response 503 (application/json) - DS Apagado/inaccesible

```json
{
  "backend":"ok",
  "modelClient": "FastApiSentimentModelClient",
  "ds":{"down"},
  "error":"DS unreachable"
}
```

---

### 1.2 Predict Sentiment 
- Clasifica un texto en **binario**: `positive / negative`.

**POST** `/sentiment`

**Request Headers**

- `Content-Type: application/json`

#### Request Body (JSON)
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
  "prediction": "negative",
  "probability": 0.82,
  "positiveProbability": 0.18,
  "negativeProbability": 0.82
}
```

* label: clasificacion 0 o 1 en caso de ser negative o positive correspondientemente
* prediction: "positive" o "negative"
* probability: probabilidad asociada a la clase positiva (0.0 a 1.0)

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
- Ejemplo: Texto corto o vacío
- Tenemos error especifico:

```json
{
  "timestamp": "2026-01-24T12:40:03.942089100-03:00",
	"status": 400,
	"error": "Validation error",
	"path": "/sentiment",
	"details": {
		"text": "El texto debe tener al menos 3 caracteres"
	}
}
```

#### 400 Invalid text 
- Cuando el texto trae emojis o simbolos 
- Tenemos un error especifico para este caso:

```json
{
  "timestamp": "2026-01-24T12:40:31.620880300-03:00",
	"status": 400,
	"error": "El texto debe contener al menos una letra o un número.",
	"path": "/sentiment",
	"details": {
		"text": "El texto debe contener al menos una letra o un número."
  }
}
```

#### 400 Malformed JSON request
- Cuando el JSON viene roto 

#### 503 Model service error
- FastAPI caído/no disponible

#### 500 internal server error
- Error inesperado (no exponemos datos sensibles)
- Error especifico:

```json
{
  "timestamp": "2026-01-24T09:58:23.342304200-03:00",
	"status": 503,
	"error": "Error al conectar con el servicio DS, verifique que DS este conectado.",
	"path": "/sentiment",
	"details": {}
}
```

## 2) Data Science (FastAPI)

### Url Base (local)

-  `http://localhost:8000`


### 2.1 POST `/predict`
- Devuelve etiqueta binaria y probabilidad.

#### Request(JSON)
```json
{
  "text": "I love this product"
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
  "Status": "ok"
}
```
## Errores de DS se ven en profundidad en:
- `docs/testing.md`

* 422: texto inválido 
* 503: modelo aún no cargado
* 500: error durante la prediccion