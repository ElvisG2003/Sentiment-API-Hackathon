# Sentiment-API-Hackathon
Proyecto de analisis de comentarios basados en la sastifaccion de usuarios que permite clasificar comentarios

---

## Objetivo del proyecto

Construir una API capaz de:

1. Recibir texto.
2. Clasificar el sentimiento de manera binaria (1: positive; 0: negative)
3. Entregar una respuesta consistente y validada.
4. Dejar el proyecto listo para mejorar/evolucionar: métricas, persistencia, estadisticas y despliegue.

---

## Alcance (Scope)

- Endpoint backend `POST /sentiment`
- Microservicio DS `POST /predict`
- Artefactos del modelo versionados en `/data-science/artifacts`
- Validación de input y formato estándar de errores en backend
- UI web

🚫 No incluido (por ahora):
- Persistencia/DB y estadísticas
- Autenticación / Rate limiting

---

## Arquitectura

Cliente → **Spring Boot API** → **FastAPI (DS)** → Modelo (TF-IDF + Logistic Regression)

---

## Estructura del repositorio

- `/backend` → Spring Boot API
- `/data-science` → modelo + artifacts + servicio FastAPI
- `/docs` → documentación del proyecto

---

## Quickstart (Local)

### Requisitos
- Java **21**
- Maven (o usar `./mvnw`)
- Python 3.x
- Pip

### 1) Levantar Data Science (FastAPI)
```bash
cd data-science/service
pip install -r requirements.txt
uvicorn main:app --reload --port 8000

Health:

  * GET http://localhost:8000/health

  * GET http://localhost:8000/health-check

Predict:

  * POST http://localhost:8000/predict

  * { "text": "I love this product!" }
```

### 2) Levantar Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run

Health:

  * GET http://localhost:8080/sentiment/health

Predict (backend):

  * POST http://localhost:8080/sentiment
  * { "text": "The delivery was late and support did not respond" }
