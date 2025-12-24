# Sentiment-API-Hackathon
API para el análisis de comentarios basada en la satisfacción de usuarios.
Permite clasificar comentarios según su sentimiento (positivo o negativo) y filtrar a preferencia del cliente.

---
## 📌Descripción del Proyecto
El proyecto consiste en una arquitectura de microservicios que combina
Data Science y Back-End para procesar comentarios de usuarios y devolver
una clasificación de sentimiento mediante una API REST.

Está pensado como solución para análisis de feedback de usuarios en contextos
de productos digitales, servicios o plataformas.

---
## 🧠Arquitectura General

- **Data Science** 
	- Lenguaje: Python
	- Librerías: Pandas, Scikit-learn
	- Modelo: TF-IDF + Logistic Regression
	- Microservicio: FastAPI

- **Backend**
	- Lenguaje: Java
	- Framework: Spring Boot
	- Función: Orquestación, valoración y exposición de la API
	
- **Base de Datos**
	- Por definir

---
## 📂Estructura del Repositorio

- /data-science -> Desarrollo y entrenamiento del modelo
- /backend -> API principal en Spring Boot
- /docs-> Documentación del proyecto

---
## 📊 Estado del Proyecto

**Semana 0**
- Definición de roles
- Definición de arquitectura
- Setup inicial de repositorio
- Limpieza y preparación del dataset
---
## Reglas básicas de Git

- No pushear directamente a "main"
- Usar branches por área de trabajo:
	- ds-cleaning
	- ds-model
	- backend-api
	- web-ui

### Convención de commits
- `feat:` nueva funcionalidad
- `fix:` correcion de errores
- `docs:` cambios de documentacion
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
| Emmanuel Cabrera | Por Definir          | Por Definir                              |
