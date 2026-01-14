# Sentiment-API-Hackathon
Proyecto de análisis de comentarios basados en la satifacción de usuarios que permite clasificar comentarios

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

#### Comandos (terminal de VS)
```bash
cd data-science/service
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

#### Probar (DS)

- Health:
```md
  GET http://localhost:8000/health

  GET http://localhost:8000/health-check
```

- Predict:

```bash
  POST http://localhost:8000/predict
```
```JSON
  {"text": "I love this product"}
```
* Response:
```JSON
  { "label": 1, "probability": 0.93 }
```

### 2) Levantar Backend (Spring Boot)

#### Comandos (terminal de VS)
```bash
cd backend
./mvnw spring-boot:run
```

#### Probar (Backend)

- Health:
```md
  GET /sentiment/health
```

* Response:
```JSON
  { "OK"}
```
---

##  Equipos y roles 

| Nombre           | Rol principal        | Tecnologías                              |
| ---------------- | -------------------- | ---------------------------------------- |
| Elvis Guerrero   | Back-End Lead        | Java, Spring Boot, SQL, Git, GitHub      |
| Abel Di Bella    | Back-End             | Java, Spring Boot, SQL                   |
| Yair Zuñiga      | Back-End / Front-End | JavaScript, HTML, CSS, SQL               |
| Miguel Bareiro   | Data Science         | Python, Pandas, Matplotlib, scikit-learn |
| José Mora        | Data Science         | Python, Pandas, SQL, Git                 |
| Aldo Gonzalez    | Data Science         | Python, Pandas, Git, Figma               |
| Pablo Hernandez  | Data Science         | Python, PHP, HTML, CSS, SQL              |
| Rafael Callata   | Back-End             | C#, .NET, SQL, Python                    |
| Pedro Hernandez  | Data Science         | JavaScript, PHP, HTML, CSS, SQL          |
| Emmanuel razo    | Data Science         | Python, css, html, react                 |
