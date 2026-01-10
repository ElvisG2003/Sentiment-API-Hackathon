# Arquitectura

## Componentes
### Backend — Spring Boot (API pública)
- Recibe requests HTTP (JSON)
- Valida el texto
- Devuelve `{prediction, probability}`
- Puede usar:
  - **Mock client** (sin DS)
  - **FastAPI client** (llama microservicio real)

### Data Science — FastAPI (microservicio interno)
- Carga artefactos de modelo desde `data-science/artifacts/`
- Expone `POST /predict` para inferencia

## Flujo
1) Cliente llama `POST /sentiment`
2) Backend valida el request
3) Backend calcula predicción:
   - modo mock: regla simple
   - modo fastapi: POST al DS `/predict`
4) Backend adapta respuesta al formato público

## Configuración clave
- `sentiment.model.client`:
  - `mock` (default recomendado para desarrollo)
  - `fastapi` (integración real)
- `sentiment.ds.base-url`: URL del microservicio (por defecto `http://localhost:8000`)

## Nota sobre DB
Actualmente **no existe persistencia implementada** en el backend.  
Si se agrega en una fase 2, se documentará aquí.
