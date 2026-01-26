# Importamos FastAPI que es el framework que usaremos para crear la API
from fastapi import FastAPI, HTTPException
# Importamos BaseModel y Field de pydantic para definir los modelos de datos
from pydantic import BaseModel, Field
from pathlib import Path
# Necesario para el modelo multi
from typing import Optional, Dict, Any
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

# Definimos la ruta de los modelos para escoger
MODELS_PATH = ARTIFACT_DIR / "models.json"

# Ruta al archivo threshold
THRESHOLD_PATH = ARTIFACT_DIR / "threshold.txt"

# Inicializamos variables globales para el modelo y el umbral
# None, es vacio porque se cargaran despues
MODEL = None
THRESHOLD = None

REGISTRY: Dict[str,Any] = {}
DEFAULT_MODEL_KEY: str = "logreg"

MODEL_CACHE: Dict [str, Any] = {}
THRESHOLD_CACHE: Dict[str, float] = {}

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

def load_registry():
    """
    Carga models.json si existe.
    Si no existe, cae a modo legacy (model_config.json).
    """
    global REGISTRY, DEFAULT_MODEL_KEY

    if MODELS_PATH.exists():
        REGISTRY = json.loads(MODELS_PATH.read_text(encoding="utf-8"))

        if "models" not in REGISTRY or not isinstance(REGISTRY["models"], dict):
            raise RuntimeError("models.json inválido: falta campo 'models'")
        
        DEFAULT_MODEL_KEY = REGISTRY.get("default", "logreg")

        # Validamos 
        if DEFAULT_MODEL_KEY  not in REGISTRY["models"]:
            raise RuntimeError(f"default=`{DEFAULT_MODEL_KEY}` no existe dentro de models")
    else:
        # Legacy: un solo modelo (el de model_config.json)
        REGISTRY = {
            "default": "logreg",
            "models": {
                "logreg": {"config": "model_config.json", "description": "Legacy single-model"}
            }
        }
        DEFAULT_MODEL_KEY = "logreg"

def load_model_from_config(config_path: Path):
    """
    Carga (model, threshold) desde un config JSON.
    """
    if not config_path.exists():
        raise RuntimeError(f"Falta config: {config_path}")

    cfg = json.loads(config_path.read_text(encoding="utf-8"))
    model_name = cfg.get("model_path")

    if not model_name:
        raise RuntimeError(f"model_path faltante en {config_path.name}")

    model_path = ARTIFACT_DIR / model_name
    if not model_path.exists():
        raise RuntimeError(f"Falta modelo: {model_path}")

    model = joblib.load(model_path)

    # Threshold: preferimos el del JSON
    if "threshold" in cfg:
        threshold = float(cfg["threshold"])
    else:
        # fallback legacy por si algún config no lo tiene
        if not THRESHOLD_PATH.exists():
            raise RuntimeError(f"Falta {THRESHOLD_PATH} y el config no trae threshold")
        threshold = float(THRESHOLD_PATH.read_text(encoding="utf-8").strip())

    if not hasattr(model, "predict_proba"):
        raise RuntimeError(
            f"El modelo '{model_name}' no tiene predict_proba. "
            f"(SVM debe ser probability=True o estar calibrado)"
        )

    return model, threshold

def get_model_and_threshold(model_key: str):
    """
    Devuelve (model, threshold) desde cache.
    Si no está cacheado, lo carga desde el config definido en models.json.
    """
    models_map = REGISTRY.get("models", {})
    if model_key not in models_map:
        raise HTTPException(status_code=404, detail=f"Unknown model '{model_key}'")

    if model_key in MODEL_CACHE and model_key in THRESHOLD_CACHE:
        return MODEL_CACHE[model_key], THRESHOLD_CACHE[model_key]

    cfg_name = models_map[model_key].get("config")
    if not cfg_name:
        raise RuntimeError(f"models.json inválido: '{model_key}' sin 'config'")

    model, threshold = load_model_from_config(ARTIFACT_DIR / cfg_name)
    MODEL_CACHE[model_key] = model
    THRESHOLD_CACHE[model_key] = threshold
    return model, threshold

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
    load_registry()
    ## Cargamos default para que el primer /health no demore
    global MODEL, THRESHOLD
    MODEL, THRESHOLD = get_model_and_threshold(DEFAULT_MODEL_KEY)

    print(f"Modelo default cargado con exito: {DEFAULT_MODEL_KEY}")



# Definimos el formato JSon en el request de la prediccion
class PredictRequest(BaseModel):
    """
    Campo "text": texto a analizar, minimo 3 caracteres, maximo 5000 caracteres
    """
    text: str = Field(min_length=3, max_length=5000, example="I love this product!")
    model: Optional[str] = Field(default=None, example="svm")

@app.get("/models")
def models():
    out = {"default": DEFAULT_MODEL_KEY, "models": {}}

    for key, meta in REGISTRY.get("models", {}).items():
        cfg_file = meta.get("config")
        desc = meta.get("description", "")
        cfg_path = ARTIFACT_DIR / cfg_file if cfg_file else None

        info = {"description": desc, "config": cfg_file, "ready": False}

        if cfg_path and cfg_path.exists():
            try:
                cfg = json.loads(cfg_path.read_text(encoding="utf-8"))
                mp = cfg.get("model_path", "")
                model_path = ARTIFACT_DIR / mp if mp else None
                info["model_path"] = mp
                info["threshold"] = cfg.get("threshold", None)
                info["ready"] = bool(model_path and model_path.exists())
            except Exception:
                info["ready"] = False

        out["models"][key] = info

    return out

# Definimos el formato Json en la respuesta de la prediccion
class PredictResponse(BaseModel):
    # label: 0 o 1 
    label: int

    # probability: probabilidad asociada a la clase positiva
    probability: float
    # Examinar que modelo se usa
    model: str

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
    
    selected = (req.model or DEFAULT_MODEL_KEY).strip()
    if not selected:
        selected = DEFAULT_MODEL_KEY
    
    model,threshold = get_model_and_threshold(selected)
    # Si por cualquier motivo cambia el orden de las clases, nos aseguramos de obtener la probabilidad correcta
    try:
        # Probabilidad de la clase positiva
        proba_lis = model.predict_proba([cleaned])
        # Aseguramos que las clases esten definidas
        if proba_lis is None or len(proba_lis) == 0:
            raise RuntimeError("predict_proba devolvió vacío")

        classes = getattr(model, "classes_", None)

        # Fallback robusto para identificar la clase positiva
        idx_pos = -1  # por defecto usamos la última columna si no podemos mapear clases

        if classes is not None:
            classes = classes.tolist() if hasattr(classes, "tolist") else list(classes)

            # tolerancia a tipos (int, str, bool)
            if 1 in classes:
                idx_pos = classes.index(1)
            elif "1" in classes:
                idx_pos = classes.index("1")
            elif True in classes:
                idx_pos = classes.index(True)
            elif "positive" in classes:
                idx_pos = classes.index("positive")
            # si no calza nada, cae al default (-1)

        proba_pos = float(proba_lis[0][idx_pos])
    except Exception as e:
        logger.exception("Error during model prediction")
        raise HTTPException(status_code=500, detail=f"{type(e).__name__}: {e}")
    
    # Aplicamos el threshold para obtener la etiqueta
    label = 1 if proba_pos >= threshold else 0

    # Devolvemos JSON con Probability
    return PredictResponse(label=label, probability=proba_pos, model=selected)