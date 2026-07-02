# DOCKER.md — Dockerfile, Docker Compose y Docker Hub

## 1. Dockerfile principal (`Dockerfile`) — imagen `scratch`

Es multistage:
1. **Stage build**: usa `ghcr.io/graalvm/native-image-community` para
   compilar un **binario nativo estatico** (GraalVM + musl). Es lo que
   permite correr sobre `scratch`, que no tiene ni siquiera libc.
2. **Stage final**: `FROM scratch`, copia solo el binario, corre con
   `USER 10001:10001` (no root — scratch no tiene `/etc/passwd`, por eso
   se usa un UID numerico directamente).

```bash
docker build -t usuariodockerhub/ht-232-01-nombre-apellido .
docker run -p 8091:8091 -e SERVER_PORT=8091 usuariodockerhub/ht-232-01-nombre-apellido
docker images usuariodockerhub/ht-232-01-nombre-apellido   # revisa que pese < 60MB
```

### Si el build nativo falla (RAM insuficiente, GraalVM lento, etc.)

Usa el Dockerfile de respaldo, **mas rapido y confiable**, aunque no cumple
`scratch` al 100% (usa distroless):

```bash
docker build -f Dockerfile.distroless-fallback -t mi-imagen .
```

Documenta en tu entrega cual Dockerfile usaste finalmente.

### Que tocar si cambias el nombre del artefacto

`pom.xml` tiene `<finalName>app</finalName>`, por eso el binario/jar se
llama siempre `target/app` o `target/app.jar`. Si cambias ese `finalName`,
actualiza también la referencia en `Dockerfile` (`/workspace/target/app`)
y en `Dockerfile.distroless-fallback` (`target/app.jar`).

---

## 2. Docker Compose (`docker-compose.yml`)

3 servicios:

| Servicio | Puerto host | Descripcion |
|---|---|---|
| `app`   | 8091 | Tu microservicio, perfil `mysql` |
| `mysql` | 3306 | Base de datos persistente (volumen `mysql_data`) |
| `nginx` | 8092 | Proxy inverso hacia `app` |

```bash
docker compose up -d --build
docker compose ps          # los 3 deben quedar "Up"
curl http://localhost:8091/v1/api/student   # directo
curl http://localhost:8092/v1/api/student   # via nginx
```

### Que tocar para cambiar puertos

Edita `docker-compose.yml`:
```yaml
ports:
  - "8091:8091"   # host:contenedor -> cambia el primero libremente
```
Y la variable `SERVER_PORT` del servicio `app` debe coincidir con el
puerto de contenedor que expusiste.

### Que tocar para usar tu propia imagen

En `docker-compose.yml`, la linea:
```yaml
image: ${DOCKERHUB_USER:-usuariodockerhub}/ht-${PROMOTION:-232}-${NUM:-01}-${NOMBRE:-nombre}-${APELLIDO:-apellido}
```
lee esos valores desde tu archivo `.env` (copia `.env.example` a `.env` y
llena tus datos reales).

---

## 3. Subir la imagen a Docker Hub

Formato exigido: `usuariodockerhub/ht-{promotion}-{##}-nombre-apellido`

```bash
docker login
docker build -t usuariodockerhub/ht-232-01-nombre-apellido:latest .
docker push usuariodockerhub/ht-232-01-nombre-apellido:latest
```

Reemplaza `usuariodockerhub`, `232`, `01`, `nombre-apellido` por tus datos
reales — son los mismos valores que usas en `.env` y en
`k8s/*-deployment.yml` (campo `image:`).

---

## 4. `.dockerignore`

Evita copiar `target/`, `.git/`, `docs/`, `k8s/`, `*.md`, `.env` dentro de
la imagen. Si agregas carpetas nuevas de trabajo (assets, scripts), añadelas
aqui para mantener la imagen liviana.
