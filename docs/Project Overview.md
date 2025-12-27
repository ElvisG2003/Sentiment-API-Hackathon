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
	- Documentacion de arquitectura, contrato API y roadmap

### No incluye (Por ahora)
	- Autenticacion y autorizacion
	- Panel web completo
	- Entrenamiento automatico/continuo

---
## 3. Arquitectura 

**Componentes principales**
- **Data Science Service (FastAPI)**
	- Carga el modelo entrenado
	- Expone endpoints de predicción
	- Devuelve: clase (0/1) + probabilidad 
* **Backend (Spring Boot)**
	* Expone endpoints públicos para el cliente
	* Valida input y formatea respuestas 
	* Llama al microservicio DS y devuelve el resultado al cliente 
-> Detalle: `docs/Arquitectura.md`

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
Además de la clase, el sistema puede usar la **probabilidad**  del modelo para filtrar casos, ej:
	- Negativo con probabilidad muy alta -> "negativo fuerte" por ende, sin feedback
	- Negativo con probabilidad media -> "posible constructivo" prioridad de revisión

## 6. Data Science / Modelo
- Vectorización: **TF-IDF**
- Modelo: **Logistic Regression**
- Dataset: CSV con etiquetas 0/1

-> Documentar: fuentes del dataset, tamaño aproximado, balance de clases, y reglas de limpieza

---
## 7. Contrato de API
-> Documento único de verdad: `docs/API_CONTRACT.md`
Debe incluir:
- Endpoints
- Request/Response JSON
- Validaciones
- Errores estándar
- Ejemplos

---
## 8. Roadmap / Cronograma
El avance semanal y planificación viven en: `docs/ROADMAP.md`  
