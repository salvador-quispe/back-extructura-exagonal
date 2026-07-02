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
└── docs/
    ├── CUSTOMIZE.md               Como cambiar el recurso, campos, BD, puertos
    ├── DOCKER.md                  Build de la imagen, Docker Compose, Docker Hub
    ├── KUBERNETES.md              Namespace/Deployment/Service, port-forward
    └── DEPLOY.md                  Publicar en Render/Railway/Koyeb con Postgres
```

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

## Docker / Docker Compose / Kubernetes / Deploy

Ver `docs/DOCKER.md`, `docs/KUBERNETES.md` y `docs/DEPLOY.md` — cada uno
explica paso a paso que archivo tocar y donde.

## Adaptar este proyecto a otro backend del hackaton

Lee `docs/CUSTOMIZE.md` primero: ahi esta la tabla completa de que archivo
cambiar segun lo que necesites (nuevo recurso, nuevo campo, otra base de
datos, otro puerto, agregar/quitar dependencias).
