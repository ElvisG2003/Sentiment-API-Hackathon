# API Contract

## BACKEND

### POST /sentiment
```
Request:
{
  "text": "The delivery was late, and the product was in a bad state"
}

Response:
{
  "label": 0,
  "prediction": "NEGATIVE"
  "probability": 0.82
}
```
### ERROR 400 Bad Request (validacion)
```
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid request",
  "details": [
     { "field": "text", "message": "text must not be blank"}
  ]
}
```

### ERROR 502 Bad Gateway (DS caido/no responde)
```
{
  "timestamp": "...",
  "status": 502,
  "error": "DS_UNAVAILABLE",
  "path": "/sentiment",
  "details": {}
}
```

## Data Science (FastAPI)

### POST /predict

Request:
```
{ "text": "I love this product!" }
```
Response:
```
{
  "prediction": "positive",
  "probability": 0.93
}
```

### GET /health /health-check