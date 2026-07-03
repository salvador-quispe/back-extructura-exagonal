# PIPELINES.md — Como conectar todo y automatizarlo (CI/CD)

Este documento cubre 2 cosas: (1) el error de CORS que bloquea al frontend
y como ya quedo resuelto, y (2) los comandos/pipelines para automatizar
build, tests, Docker y despliegue.

---

## 1. CORS — por que el frontend no conectaba

El navegador bloquea peticiones `fetch`/`XMLHttpRequest` de un origen
(`http://localhost:4200`, `http://localhost:59753`, etc.) hacia otro
(`http://localhost:8090`) si el servidor no responde explicitamente que lo
permite. Spring Boot **no habilita CORS por defecto**, por eso el error:

```
Access to XMLHttpRequest ... has been blocked by CORS policy:
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

### Solucion ya aplicada

`src/main/java/pe/edu/vallegrande/student/infrastructure/config/CorsConfig.java`
habilita CORS para toda la API (`/**`), permitiendo `GET/POST/PUT/DELETE/OPTIONS`
desde cualquier origen por defecto (`*`), controlable por la variable
`CORS_ALLOWED_ORIGINS`.

### Como probarlo

1. Reinicia el backend (`mvn spring-boot:run`) para que cargue el nuevo bean.
2. Vuelve a cargar el frontend (`npm start` en `frontend/`, o refresca la
   pestana si ya estaba abierto).
3. La tabla del CRUD deberia cargar sin errores en la consola del navegador.

### Si sigue sin conectar, revisa en este orden

| Sintoma | Causa probable | Solucion |
|---|---|---|
| `CORS policy: No 'Access-Control-Allow-Origin'` | El backend no tiene `CorsConfig` cargado (¿reiniciaste?) o `CORS_ALLOWED_ORIGINS` esta mal escrito | Reinicia el backend; revisa que no haya espacios extra en la variable |
| `ERR_CONNECTION_REFUSED` / `Failed to fetch` | El backend no esta corriendo, o el puerto de `config.json` no coincide con el puerto real | Ver `docs/PUERTOS.md` |
| `404 Not Found` | La ruta de `apiBaseUrl` no coincide con `@RequestMapping` del controller | Revisa `StudentController.java` vs `config.json` |
| Angular en `https://...` (Codespaces, Docker con TLS) llamando a un backend `http://...` | Mixed content bloqueado por el navegador | Sirve ambos por `https` o usa el mismo esquema en los dos |

### Restringir CORS para produccion (opcional, recomendado)

En vez de `CORS_ALLOWED_ORIGINS=*`, pon el dominio real de tu frontend:
```
CORS_ALLOWED_ORIGINS=https://mi-frontend.onrender.com,http://localhost:4200
```

---

## 2. Comandos: correr todo localmente (sin pipeline)

```bash
# Backend (perfil H2, sin instalar nada)
mvn spring-boot:run

# Frontend (otra terminal)
cd frontend
npm install
npm start
```
Abre `http://localhost:4200`. El `config.json` por defecto ya apunta a
`http://localhost:8090/v1/api/student`.

### Todo junto con Docker Compose

```bash
cp .env.example .env      # ajusta valores si quieres
docker compose up -d --build
docker compose ps          # deben quedar 4 servicios "Up"
```
Abre `http://localhost:8095` (frontend) — apunta por defecto a
`http://localhost:8092` (nginx → backend), ver `FRONTEND_API_BASE_URL` en `.env`.

---

## 3. Pipeline de ejemplo: GitHub Actions (backend)

Crea `.github/workflows/backend-ci.yml`:

```yaml
name: backend-ci

on:
  push:
    branches: [main]
    paths:
      - "src/**"
      - "pom.xml"
  pull_request:

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven

      - name: Compilar
        run: mvn -B -q compile

      - name: Correr tests
        run: mvn -B -q test

      - name: Empaquetar jar
        run: mvn -B -q -DskipTests package
```

## 4. Pipeline de ejemplo: build + push a Docker Hub

Crea `.github/workflows/docker-publish.yml`. Requiere 2 secrets en el repo
(Settings → Secrets and variables → Actions): `DOCKERHUB_USERNAME` y
`DOCKERHUB_TOKEN` (token generado en hub.docker.com → Account Settings →
Security).

```yaml
name: docker-publish

on:
  push:
    branches: [main]

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Login a Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Build y push (backend)
        uses: docker/build-push-action@v6
        with:
          context: .
          file: ./Dockerfile.distroless-fallback   # o ./Dockerfile si el build nativo te funciona en CI
          push: true
          tags: ${{ secrets.DOCKERHUB_USERNAME }}/ht-232-01-nombre-apellido:latest

      - name: Build y push (frontend)
        uses: docker/build-push-action@v6
        with:
          context: ./frontend
          file: ./frontend/Dockerfile
          push: true
          tags: ${{ secrets.DOCKERHUB_USERNAME }}/ht-232-01-nombre-apellido-frontend:latest
```

Equivalente manual (sin CI, desde tu maquina):
```bash
docker login
docker build -f Dockerfile.distroless-fallback -t usuariodockerhub/ht-232-01-nombre-apellido .
docker push usuariodockerhub/ht-232-01-nombre-apellido

docker build -t usuariodockerhub/ht-232-01-nombre-apellido-frontend ./frontend
docker push usuariodockerhub/ht-232-01-nombre-apellido-frontend
```

## 5. Pipeline de ejemplo: deploy automatico a Kubernetes local

No hay "CI" real para tu propio Docker Desktop (no es un servidor remoto),
pero puedes automatizar los pasos en un solo script. Crea `deploy-k8s.sh`
en la raiz:

```bash
#!/bin/sh
set -e
kubectl apply -f k8s/salvador-namespace.yml
kubectl apply -f k8s/salvador-deployment.yml
kubectl apply -f k8s/salvador-service.yml
kubectl rollout status deployment/salvador-deployment -n salvador-namespace
echo "Listo. Prueba: http://localhost:30093/v1/api/student"
```
```bash
chmod +x deploy-k8s.sh
./deploy-k8s.sh
```

## 6. Pipeline de deploy a Render/Railway/Koyeb

Estas plataformas ya traen su propio "pipeline" integrado: al conectar el
repo de GitHub, cada `git push` a `main` dispara un build+deploy automatico
usando tu `Dockerfile`. No necesitas configurar nada adicional aparte de
las variables de entorno (ver `docs/DEPLOY.md` seccion 2).

```bash
git add .
git commit -m "deploy: actualizar backend"
git push origin main
# La plataforma cloud detecta el push y redepliega sola.
```
