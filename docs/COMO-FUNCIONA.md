# COMO-FUNCIONA.md — Mapa general del proyecto

Guia rapida para orientarte: que pieza hace que, y a que archivo ir segun
lo que necesites cambiar.

## Las 4 piezas del proyecto

```
┌─────────────┐      HTTP       ┌──────────────────┐     JDBC     ┌──────────┐
│  frontend/  │ ───────────────▶│  Backend Spring   │──────────────▶│    BD    │
│  (Angular)  │  /v1/api/...    │  Boot (hexagonal) │               │ H2/MySQL │
└─────────────┘                 └──────────────────┘               │ /Postgres│
                                                                     └──────────┘
```

- **`frontend/`**: interfaz web (Angular). No sabe nada de Java/Spring; solo
  llama a una URL REST que le dices en `frontend/public/config.json`.
- **backend (raiz del repo, `src/`)**: microservicio Spring Boot,
  arquitectura hexagonal, expone `/v1/api/student`.
- **BD**: H2 en memoria por defecto (no requiere instalar nada), o MySQL/
  Postgres segun el perfil activo.
- **Docker/Kubernetes**: formas de empaquetar y correr las piezas de
  arriba, no agregan logica nueva.

## Donde ir segun lo que quieras hacer

| Quiero... | Archivo / doc |
|---|---|
| Cambiar el puerto de algo (backend, docker, k8s, frontend) | `docs/PUERTOS.md` |
| Cambiar el recurso `Student` por otro (Product, Order, etc.) | `docs/CUSTOMIZE.md` |
| Agregar un campo nuevo al modelo | `docs/CUSTOMIZE.md` seccion 2 |
| Cambiar de base de datos (H2 → MySQL → Postgres → Mongo) | `docs/CUSTOMIZE.md` seccion 4 |
| Agregar/quitar una dependencia (seguridad, swagger, redis...) | `pom.xml` (todas comentadas ahi) + `docs/CUSTOMIZE.md` seccion 5 |
| Construir/subir la imagen Docker del backend | `docs/DOCKER.md` |
| Levantar backend+BD+nginx+frontend juntos | `docker-compose.yml` (`docker compose up -d --build`) |
| Desplegar en Kubernetes local (Docker Desktop) | `docs/KUBERNETES.md` |
| Publicar el backend en Render/Railway/Koyeb | `docs/DEPLOY.md` |
| Conectar el frontend a otro backend / cambiar sus campos | `docs/FRONTEND.md` |
| Entender la arquitectura hexagonal del backend | `docs/CUSTOMIZE.md` (tabla de capas al inicio) |

## Flujo tipico de una peticion (para entender el codigo)

Ejemplo: el usuario crea un estudiante desde el frontend.

1. `frontend/src/app/app.component.ts` → `submit()` arma el JSON y llama a
   `CrudService.create()`.
2. `frontend/src/app/core/crud.service.ts` → hace `POST` a `apiBaseUrl`
   (definido en `config.json`).
3. Backend: `infrastructure/adapter/in/web/StudentController.java` recibe
   el `POST /v1/api/student`, valida el body (`StudentRequest.java`).
4. `application/service/StudentService.java` ejecuta la logica de negocio
   (verifica que no exista el `dni`, pone la fecha, imprime el log).
5. `infrastructure/adapter/out/persistence/StudentRepositoryAdapter.java`
   guarda el registro via JPA en la BD activa (H2 por defecto).
6. La respuesta vuelve como `StudentResponse.java` → JSON → el frontend
   refresca la tabla.

Si algo falla en cualquier punto de esta cadena, revisa en ese orden:
Controller (¿llega la peticion?) → Service (¿logs de la accion?) →
Repository/BD (¿se guardo?) → Response/mapper (¿el JSON de salida es el
esperado?).

## Variables de entorno mas usadas (resumen)

| Variable | Para que sirve | Donde se usa |
|---|---|---|
| `SERVER_PORT` | Puerto del backend | `application.yml`, `docker-compose.yml`, `k8s/*-deployment.yml` |
| `SPRING_PROFILES_ACTIVE` | Que perfil de BD usar: `h2`\|`mysql`\|`postgres` | igual que arriba |
| `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` | Conexion a MySQL | perfil `mysql` en `application.yml` |
| `DATABASE_URL` | Conexion a Postgres (cloud) | perfil `postgres` en `application.yml` |
| `API_BASE_URL`/`ID_FIELD`/`APP_TITLE` | A que backend apunta el frontend | `frontend/public/config.json` (dev) o env de Docker |

Todas estas variables estan documentadas con ejemplos en `.env.example`
(raiz del repo).
