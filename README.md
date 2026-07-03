# student-service — Backend hexagonal para hackaton

Microservicio Spring Boot con arquitectura **hexagonal (Ports & Adapters)**,
CRUD completo de `Student`, base de datos flexible (H2 / MySQL / Postgres),
puerto configurable por variable de entorno, Docker multistage a `scratch`,
Docker Compose y manifiestos de Kubernetes — todo pensado para clonar este
mismo esqueleto en cada backend nuevo del hackaton y solo tocar lo que
cambia (ver `docs/CUSTOMIZE.md`).

## Estructura

```
.
├── pom.xml                        Dependencias (todas las opcionales comentadas)
├── src/main/java/pe/edu/vallegrande/student/
│   ├── domain/                    Modelo + puertos (sin dependencias de framework)
│   ├── application/               Casos de uso (logica + logs de auditoria)
│   └── infrastructure/            REST, JPA, config, manejo de errores
├── src/main/resources/application.yml   Puerto y perfiles de BD (h2/mysql/postgres)
├── Dockerfile                     Multistage -> scratch (GraalVM nativo, no root, <60MB)
├── Dockerfile.distroless-fallback Alternativa simple si el build nativo falla
├── docker-compose.yml             app (8091) + mysql (3306) + nginx (8092)
├── nginx/nginx.conf
├── k8s/                           namespace + deployment (3 replicas, 8093) + service NodePort
├── frontend/                      App Angular 18 generica de CRUD (ver docs/FRONTEND.md)
│   ├── public/config.json         URL del backend + campos del formulario (runtime, sin rebuild)
│   ├── src/app/core/               ConfigService + CrudService (HTTP generico)
│   └── Dockerfile                 Multistage: build Angular -> nginx
└── docs/
    ├── COMO-FUNCIONA.md           Mapa general: que pieza hace que, y a donde ir
    ├── PUERTOS.md                 Tabla unica de TODOS los puertos del proyecto
    ├── CUSTOMIZE.md               Como cambiar el recurso, campos, BD
    ├── DOCKER.md                  Build de la imagen, Docker Compose, Docker Hub
    ├── KUBERNETES.md              Namespace/Deployment/Service, port-forward
    ├── DEPLOY.md                  Publicar en Render/Railway/Koyeb con Postgres
    ├── FRONTEND.md                Conectar el frontend a CUALQUIER backend, cambiar campos
    └── PIPELINES.md               CORS, comandos y pipelines CI/CD (GitHub Actions, Docker Hub, K8s)
```

**Si no sabes por donde empezar**, lee `docs/COMO-FUNCIONA.md` primero — es
el mapa que te manda al archivo correcto segun lo que necesites cambiar.

## Endpoints

Base: `/v1/api/student`

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/v1/api/student` | Lista todos |
| GET | `/v1/api/student/{dni}` | Obtiene uno |
| POST | `/v1/api/student` | Crea (valida `dni` de 8 digitos, campos obligatorios) |
| PUT | `/v1/api/student/{dni}` | Actualiza |
| DELETE | `/v1/api/student/{dni}` | Elimina |

Respuesta:
```json
{
  "dni": "87654321",
  "firstName": "tus nombres",
  "lastName": "tus apellidos",
  "promotion": 232,
  "date": "2026-07-02T10:15:30"
}
```

Cada operacion imprime un log con la accion realizada
(`ACCION=INVOCAR|REGISTRAR|ACTUALIZAR|ELIMINAR`), ver
`application/service/StudentService.java`.

## Correr en local

```bash
mvn spring-boot:run
# o con puerto/perfil custom:
SERVER_PORT=9000 SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run
```

Abrir: `http://localhost:8090/v1/api/student`

Consola H2 (mientras el perfil `h2` este activo): `http://localhost:8090/h2-console`
(JDBC URL: `jdbc:h2:mem:studentdb`, user `sa`, password vacio).

## Frontend (Angular, generico para cualquier backend)

```bash
cd frontend
npm install
npm start        # http://localhost:4200
```
Edita `frontend/public/config.json` para apuntarlo a este backend o a
cualquiera de tus otros 2 backends, y para definir los campos del CRUD.
Ver `docs/FRONTEND.md`.

## Docker / Docker Compose / Kubernetes / Deploy

Ver `docs/DOCKER.md`, `docs/KUBERNETES.md` y `docs/DEPLOY.md` — cada uno
explica paso a paso que archivo tocar y donde. `docker compose up -d --build`
levanta backend + mysql + nginx + frontend juntos (puertos 8091/3306/8092/8095).

## Adaptar este proyecto a otro backend del hackaton

Lee `docs/CUSTOMIZE.md` primero: ahi esta la tabla completa de que archivo
cambiar segun lo que necesites (nuevo recurso, nuevo campo, otra base de
datos, otro puerto, agregar/quitar dependencias).
