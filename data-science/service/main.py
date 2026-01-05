from fastapi import FastAPI

app = FastAPI()

## Checkeo de health del servicio
@app.get("/health")
def health_check():
    return {"status": "ok"}