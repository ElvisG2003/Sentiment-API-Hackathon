# Project Overview - Sentiment-API-Hackaton

## 1. Objetivo
Construir una solución que analice comentarios de usuarios y los clasifique por **sentimiento** para apoyar decisiones de empresas (soporte, producto, calidad, etc.).
El sistema entrega una clasificación **binaria**: **positivo / negativo**, usando un modelo de ML entrenado desde un dataset CSV.

---
## 2. Alcance

### Incluye
	-Clasificacion de texto **binaria**:
		- `0` = Negativo
		- `1` = Positivo
	- Microservicio de Data Science para inferencia (prediccion) vía API
	- Backend en Spring Boot para exponer la API publica, validar requests y orquestar llamadas
	- Web UI estatica para demo end-to-end
	- Documentacion de arquitectura, contrato API y roadmap

### No incluye (Por ahora)
	- Autenticacion y autorizacion
	- Persistencia/DB
	- Entrenamiento automatico/continuo
	- Dashboard analitico completo

---

## 3. Arquitectura 

**Componentes principales**
- **Data Science Service (FastAPI)**
	- Carga el modelo entrenado
	- Expone endpoints de predicción
	- Devuelve: `label` (0/1) + `probability`
- **Backend (Spring Boot)**
	- Expone endpoints públicos para el cliente
	- Valida input y formatea respuestas 
	- Llama al microservicio DS y devuelve el resultado al cliente 
- **Web UI**
	- Interfaz demo para enviar texto y visualizar resultados

-> Detalle: `docs/arquitectura.md`

---

## 4. Flujo end-to-end 

1) Cliente envía un comentario al Backend
2) Backend valida el input (texto no vacío, largo máximo, etc.)
3) Backend llama al DS Service (FastAPI) con el texto
4) DS Service devuelve:
	- `label` (0/1)
	- `probability`
5) Backend responde al cliente con un JSON consistente
6) (Opcional) Se registra en DB o logs para auditoría/analítica

---

## 5. Clasificación binaria y uso de probabilidad 

### Etiquetas 
El dataset se normalizo a valores **0/1** para simplificar y evitar ambigüedades.
- `0` -> Negativo
- `1` -> Positivo

### Probabilidad
Ademas de la clase, el sistema entrega la **probabilidad** de la clase positiva, util para:

- Priorizar revision manual
- umbral de "confianza" para reducir falsos positivos/negativos

---

## 6. Data Science / Modelo
- Vectorización: **TF-IDF**
- Modelo: **Logistic Regression**
- Experimentos: modelos alternos (ej. Svm, Naive Bayes)

---

## 7. Contrato de API
-> Documento único: `docs/api_contract.md`

Debe incluir:
- Endpoints
- Request/Response JSON
- Validaciones
- Errores estándar
- Ejemplos

---
## 8. Roadmap / Cronograma
El avance Y planificación viven en: `docs/roadmap.md`  
