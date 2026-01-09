# API Contract

POST /sentiment

Request:
{
  "text": "The delivery was late, and the product was in a bad state"
}

Response:
{
<<<<<<< HEAD
  "label": 0,
  "prediction": "NEGATIVE"
  "probability": 0.82
}

ERROR 400
cuando text es corto o viene vacio.
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid request",
  "details": [
     { "field": "text", "message": "text must not be blank"}
  ]
}

ERROR 502
Cuando backend falla al llamar al servicio DS

{
  "error": "DS_UNAVAILABLE",
  "message": "Data Science service not reachable"
=======
  "prediction": "positive",
  "probability": 0.82
>>>>>>> origin/backend-api
}
