# PUERTOS.md — Tabla unica de todos los puertos del proyecto

Referencia rapida: cada puerto que existe en el proyecto, en que archivo
esta definido, y como cambiarlo. Si algo "no conecta", este es el primer
archivo a revisar.

## 1. Backend Spring Boot (local, sin Docker)

| Puerto | Donde esta definido | Como cambiarlo |
|---|---|---|
| `8090` (default) | `src/main/resources/application.yml` linea `server.port: ${SERVER_PORT:8090}` | Exporta la variable de entorno `SERVER_PORT` antes de correr, ej: `SERVER_PORT=9000 mvn spring-boot:run` |

No hay ningun puerto hardcodeado en el codigo Java — todo pasa por esa
variable. Ver `docs/CUSTOMIZE.md` seccion 3 para mas detalle.

## 2. Docker Compose (`docker-compose.yml`, raiz del proyecto)

| Servicio | Puerto host | Puerto contenedor | Donde cambiarlo |
|---|---|---|---|
| `app` (backend) | `8091` | `8091` | `docker-compose.yml` → servicio `app` → `ports:` y `environment.SERVER_PORT` (deben coincidir) |
| `mysql` | `3306` | `3306` | `docker-compose.yml` → servicio `mysql` → `ports:` |
| `nginx` (proxy del backend) | `8092` | `80` | `docker-compose.yml` → servicio `nginx` → `ports:` |
| `frontend` (Angular) | `8095` | `80` | `docker-compose.yml` → servicio `frontend` → `ports:` |

Regla: el numero de la **izquierda** de `"host:contenedor"` es el que
usas en el navegador (`http://localhost:<ese numero>`). El de la
**derecha** debe coincidir con lo que la app/nginx escucha adentro del
contenedor.

Comando para verificar que los 4 servicios quedaron activos:
```bash
docker compose up -d --build
docker compose ps
```

## 3. Kubernetes (`k8s/*.yml`)

| Recurso | Puerto | Donde esta definido |
|---|---|---|
| Deployment (contenedor) | `8093` | `k8s/salvador-deployment.yml` → `containerPort`, env `SERVER_PORT`, y los `readinessProbe`/`livenessProbe` |
| Service (dentro del cluster) | `80` | `k8s/salvador-service.yml` → `port` |
| Service → Deployment | `8093` | `k8s/salvador-service.yml` → `targetPort` (debe ser igual al `containerPort` del deployment) |
| NodePort (tu maquina) | `30093` | `k8s/salvador-service.yml` → `nodePort` (rango valido: 30000-32767) |
| Port-forward (puente manual) | el que quieras, ej `8094` | se define en el comando: `kubectl port-forward svc/salvador-service 8094:80` |

Si cambias el puerto del backend dentro de Kubernetes, **actualiza los 3
juntos** (deployment `containerPort`+env, deployment probes, service
`targetPort`) — si quedan desincronizados, el pod queda "Running" pero
el Service no puede enviarle trafico. Detalle completo en
`docs/KUBERNETES.md` seccion 6.

## 4. Frontend Angular (`frontend/`)

| Contexto | Puerto | Donde cambiarlo |
|---|---|---|
| `ng serve` / `npm start` (dev local) | `4200` (fijo por Angular CLI) | `frontend/angular.json` → `serve.options.port`, o `ng serve --port 4300` |
| Docker (`frontend/Dockerfile`) | nginx interno `80` | no hace falta tocarlo, se mapea desde afuera |
| Docker Compose | `8095` (host) → `80` (contenedor) | `docker-compose.yml` → servicio `frontend` → `ports:` |

El frontend **no tiene su propio puerto de backend hardcodeado**: la URL
del backend al que llama vive en `frontend/public/config.json`
(`apiBaseUrl`), no en un puerto de Angular. Ver `docs/FRONTEND.md`.

## 5. Que puerto uso para probar cada cosa

| Quiero probar... | URL |
|---|---|
| Backend corriendo con `mvn spring-boot:run` | `http://localhost:8090/v1/api/student` |
| Backend dentro de `docker compose` (directo) | `http://localhost:8091/v1/api/student` |
| Backend dentro de `docker compose` (via nginx) | `http://localhost:8092/v1/api/student` |
| Backend en Kubernetes (via NodePort) | `http://localhost:30093/v1/api/student` |
| Backend en Kubernetes (via port-forward) | `http://localhost:8094/v1/api/student` (o el puerto que hayas puesto en el comando) |
| Frontend en desarrollo | `http://localhost:4200` |
| Frontend en `docker compose` | `http://localhost:8095` |

## 6. Checklist cuando "no conecta"

1. ¿El backend esta realmente escuchando en el puerto que crees? Revisa el
   log de arranque de Spring Boot (imprime `Tomcat started on port(s): XXXX`).
2. ¿El `apiBaseUrl` del frontend (`config.json`) usa el mismo puerto que el
   backend que esta corriendo en ese momento?
3. Si backend y frontend corren en Docker distinto network/host, usa
   `host.docker.internal` en vez de `localhost` dentro de `config.json`/env.
4. En Kubernetes: `kubectl get pods -n <namespace>` deben estar `Running`,
   y `kubectl get svc -n <namespace>` debe mostrar el `NodePort` esperado.
