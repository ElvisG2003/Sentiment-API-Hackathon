# Data Science — SentimentAPI (Twitter Airline)

Este directorio contiene todo el avance de **Data Science**: desde la carga del dataset y limpieza del texto, hasta el entrenamiento del modelo, evaluación y exportación de artefactos para producción (FastAPI).

> Objetivo: clasificar textos como **positivo (1)** vs **negativo (0)** y exponer el modelo mediante una API para que el backend lo consuma.

---

## ✅ Resultado esperado

Al finalizar, deberías tener:

1. Un notebook reproducible end-to-end:
   - EDA → limpieza → split → entrenamiento → evaluación → exportación
2. Un modelo baseline funcional:
   - **TF-IDF + Logistic Regression** (scikit-learn)
3. Métricas y validaciones:
   - Accuracy / Precision / Recall / F1-score
   - Matriz de confusión
   - Sanity check anti-leakage (label shuffle)
4. Artefactos exportados para producción:
   - `sentiment_pipeline_balanced.joblib`
   - `threshold.txt`
   - `model_config.json`
5. Microservicio **FastAPI** listo para inferencia:
   - `/health`
   - `/predict`

---

## 📁 Estructura del directorio

- data-science/
  * README.md
  * Customer_Sentiment.csv
  * Customer_Sentiment_final.csv
  * Customer_Sentiment_final2.csv
  * Sentiment_Final.ipynb

- artifacts/
  * sentiment_pipeline_balanced.joblib
  * threshold.txt
  * model_config.json

- service/
  * main.py
  * requirements.txt


---

## 🔎 Conceptos clave (explicado simple)

### 1) ¿Qué es `texto_de_review` vs `texto_clean`?
- **`texto_de_review`**: texto crudo original (como viene en Twitter).
- **`texto_clean`**: texto normalizado para modelado (minúsculas, sin URLs, sin menciones, etc.).

✅ El modelo se entrena y predice usando **`texto_clean`**.

---

### 2) ¿Qué es `joblib` y por qué existe `sentiment_pipeline_balanced.joblib`?
`joblib` es una herramienta para **guardar y cargar objetos de Python** (serialización).

El archivo:
- `sentiment_pipeline_balanced.joblib`

contiene el **pipeline completo** ya entrenado:
- TF-IDF (vectorizador)
- Logistic Regression (clasificador)

✅ Esto permite usar el modelo en producción sin re-entrenar.

---

### 3) ¿Qué es el `threshold` (umbral) y por qué existe `threshold.txt`?
El modelo entrega probabilidades (ej. 0.73 = 73% de ser positivo).
Para convertir eso en 0/1, usamos un umbral:

- si `probabilidad >= threshold` → **positivo (1)**
- si `probabilidad < threshold` → **negativo (0)**

En este proyecto se eligió:
- `threshold = 0.40`

porque mejora el **recall de positivos** (detecta más positivos reales), a costa de más falsos positivos.

✅ Guardamos el valor en `threshold.txt` para que FastAPI y backend usen el mismo criterio.

---

### 4) ¿Qué es `model_config.json`?
Es un archivo de configuración/documentación del modelo (metadatos), por ejemplo:
- threshold elegido
- política de labels (positive=1, neutral/negative=0)
- configuración del pipeline (ngram_range, max_features, etc.)

✅ No es el modelo. Es un “manual” reproducible de cómo se construyó.

---

### 5) ¿Qué es `main.py` y `requirements.txt`?
- **`main.py`**: el microservicio FastAPI que carga el modelo y expone endpoints.
- **`requirements.txt`**: lista de dependencias para instalar el entorno:
  - fastapi, uvicorn, scikit-learn, joblib

✅ Esto permite ejecutar el servicio igual en cualquier PC.

---

### 6) ¿Que es Customer_sentiment...?
- Primera version del proyecto; un intento de sentiment el cual ayudo a plasmar el camino y aprender lo basico.
- Si bien resulto ser poco practico, sirvio para profundizar en la via a seguir y ser una buena prueba

✅ A pesar de los contratiempos, hubo aprendizaje valioso.

---

## 🧪 Notebook (Colab) — Cómo correrlo

### Requisitos
- Google Colab o Jupyter Notebook con Python 3.10+.
- Acceso al dataset (HuggingFace datasets vía `hf://...`).

### Pasos recomendados
1. Abrir `Sentiment_Final.ipynb`
2. Ejecutar:
   - **Runtime → Restart and run all**
3. Verificar que al final se generen los artefactos exportados:
   - `sentiment_pipeline_balanced.joblib`
   - `threshold.txt`
   - `model_config.json`

---

## 📦 Exportación de artefactos (desde Colab)

Al final del notebook se exportan archivos. Para descargarlos en Colab:

```python
from google.colab import files

files.download("sentiment_pipeline_balanced.joblib")
files.download("threshold.txt")
files.download("model_config.json")
