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

## Que se hace
Se usan 3 componentes principales para crear un sistema de clasificacion binaria:

- **Web UI** (frontend estático) -> `web-ui/
- **Backend** (Spring Boot, API publica) -> `backend/`
- **Data Science** (FastAPI + modelo sciki-learn) -> `data-science/service/`

Con esto, se entrega:

- `prediction`: `"positive"` / `"negative"`
- `label`: `1` (positive) / `1` (negative)
- `probability`: probabilidad **de que el resultado sea positivo** (0.0-1.0)

> Como extra el backend expone dos datos extras para un mejor entendimiento
> - `positiveProbability` (== `probability`) -> Probabilidad de resultados positivos
> - `negativeProbability` (== 1- `probability`) -> El restante de probability para exponer probabilidad de respuesta negativa

---

## Arquitectura

Cliente → **Spring Boot API** → **FastAPI (DS)** → Modelo (TF-IDF + Logistic Regression)

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
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

#### Chequeos

- `py -0p` -> Python 3.12
- En caso de estar roto -> `Remove-Item -Recurse -Force .\.venv -ErrorAction SilentlyContinue`
- `py -3.12 -m venv .venv` -> Crear venv con Python 3.12
- Repetir Comandos

#### Probar (DS)

- Health:
```md
  GET http://localhost:8000/health

  POST http://localhost:8000/predict
```
* Get -> `{"status":"ok"}` / `{"status":"starting"}` si no funciono
* Post -> se envia con `{ "text": "I love this product" }`

* Response:
```JSON
  { 
    "label": 1, "probability": 0.93 
  }
```
---

### 2) Levantar Backend (Spring Boot)

#### Comandos (terminal de VS)
```bash
cd backend
./mvnw spring-boot:run
```

#### Probar (Backend) (Mejor desde intellij)

- Health:
```md
  GET http://localhost:8080/sentiment/health

  POST http://localhost:8080/sentiment
```
* Get -> `{"status":"ok"}` 
* Post -> se envia con `{ "text": "I love this product" }`

* Response:
```JSON
  { 
    "label": 1, "probability": 0.93 
    "prediction": 
    "probability":
  }
```
---

### Levantar web-UI
Usar Live Server en VSCode

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
