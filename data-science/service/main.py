# Importamos FastAPI que es el framework que usaremos para crear la API
from fastapi import FastAPI, HTTPException
# Importamos BaseModel y Field de pydantic para definir los modelos de datos
from pydantic import BaseModel, Field
from pathlib import Path
# Importamos joblib para cargar el modelo
import joblib
# Json para manejar datos en formato JSON
import json
# Re es de regex, para limpiar texto
import re
# Logging para mensajes de log
import logging


# Creamos la API con nombre y versión
app = FastAPI(title="Sentiment Analysis API", version="1.0.0")

# Configuramos el logger para usar el de uvicorn
logger = logging.getLogger("uvicorn.error")

#Obtenemos el directorio actual
SERVICE_DIR = Path(__file__).resolve().parent

# Definimos la ruta a la carpeta de artifacts
ARTIFACT_DIR = SERVICE_DIR.parent / "artifacts"

# Definimos la ruta al modelo entrenado
CONFIG_PATH = ARTIFACT_DIR / "model_config.json"

# Ruta al archivo threshold
THRESHOLD_PATH = ARTIFACT_DIR / "threshold.txt"

# Inicializamos variables globales para el modelo y el umbral
# None, es vacio porque se cargaran despues
MODEL = None
THRESHOLD = None

def clean_text(s:str) -> str:
    """
    Esta funcion imita una limpieza como la del notebook
    - pasar a minusculas
    - borrar URLS
    - borrar menciones de @Usuarios
    - normalizar 
    """
    s =str(s)
    s = s.lower()
    s = re.sub(r"http\S+|www\.+|https\S+", '', s)
    s = re.sub(r"@\w+", '', s)
    s = re.sub(r"\s+", " ", s).strip()
    return s

def load_artifacts():
    """
    Esta funcion carga y valida los artefactos:
    - model_config.json (threshold y nombre del modelo)
    - .joblib (el modelo entrenado)
    - threshold.txt (el umbral para clasificar)
    """
    # Cargamos la configuracion del modelo
    global MODEL, THRESHOLD

    # Validamos que exista el archivo de configuracion
    if not CONFIG_PATH.exists():
        raise RuntimeError(f"Hace falta {CONFIG_PATH}. Debe existir en data-science/artifacts")
    
    # Leemos el config como texto y lo parseamos
    cfg = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))

    # En el config puede venir el nombre del modelo
    model_name = cfg.get("model_path", "sentiment_pipeline_balanced.joblib")

    # Construimos la ruta al modelo
    model_path = ARTIFACT_DIR / model_name

    # Validamos que exista de .joblib
    if not model_path.exists():
        raise RuntimeError(f"Hace falta {model_path}. Debe existir en data-science/artifacts")
    
    # CCargamos el pipeline
    MODEL = joblib.load(model_path)

    # El config trae threshold
    if "threshold" in cfg:
        THRESHOLD = float(cfg["threshold"])
    else:
        # Si no viene en el config, lo leemos del archivo
        if not THRESHOLD_PATH.exists():
            raise RuntimeError(f"Hace falta {THRESHOLD_PATH}. Debe existir en data-science/artifacts")
        THRESHOLD = float(THRESHOLD_PATH.read_text(encoding="utf-8").strip())

    # Validaciones basicas
    # probs y aplicar threshold
    if not hasattr(MODEL, "predict_proba"):
        raise RuntimeError("El modelo cargado no tiene el metodo predict_proba")
    
    return cfg


@app.on_event("startup")
def startup_event():
    """
    Evento que se ejecuta al iniciar el servicio
    Carga los artefactos necesarios
    """
    load_artifacts()
    print(f"Modelo cargado con exito")


# Definimos el formato JSon en el request de la prediccion
class PredictRequest(BaseModel):
    """
    Campo "text": texto a analizar, minimo 3 caracteres, maximo 5000 caracteres
    """
    text: str = Field(min_length=3, max_length=5000, example="I love this product!")

# Definimos el formato Json en la respuesta de la prediccion
class PredictResponse(BaseModel):
    # label: 0 o 1 
    label: int

    # probability: probabilidad asociada a la clase positiva
    probability: float

## Checkeo de health del servicio
@app.get("/health-check")
def health_check():
    return {"status": "ok"}

@app.get("/health")
def health():
    # Si todavía no cargo el modelo devuelve starting
    if MODEL is None or THRESHOLD is None:
        return {"status": "starting"}
    # Si todo esta ok, lo retornamos
    return {"status": "ok"}

@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    """
    Endpoint para predecir el sentimiento de un texto
    """
    if MODEL is None or THRESHOLD is None:
        raise HTTPException(status_code=503, detail="Modelo not loaded yet. Try again later.")
    
    # Limpiamos el texto con la funcion
    cleaned = clean_text(req.text)

    # Si despues de limpiar queda vacio, respondemos con error 422 (invalido)
    if not cleaned:
        # Probamos que proba devuevla los indices correctos
        raise HTTPException(status_code=422, detail="Text is empty after cleaning. Please provide valid text.")
    
    try:
        # Si cualquier error es dado, devolvemos 500 pero sin exponer datos
        proba_pos = MODEL.predict_proba([cleaned])[0][1]
    except Exception as e:
        logger.exception("Error during model prediction")
        raise HTTPException(status_code=500, detail=f"Error during prediction.")
    
    # Aplicamos el threshold para obtener la etiqueta
    label = 1 if proba_pos >= THRESHOLD else 0

    # Devolvemos JSON con Probability
    return PredictResponse(label=label, probability=proba_pos)