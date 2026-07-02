# KUBERNETES.md — Namespace, Deployment, Service y port-forward

Requiere Docker Desktop con Kubernetes habilitado (Settings → Kubernetes →
Enable Kubernetes).

Archivos en `k8s/`:

| Archivo | Que crea |
|---|---|
| `salvador-namespace.yml` | Namespace `salvador-namespace` |
| `salvador-deployment.yml` | Deployment con 3 replicas, puerto de contenedor `8093` |
| `salvador-service.yml` | Service `NodePort`, expone el deployment en `30093` |

## 1. Renombrar los archivos por tu nombre

Copia/renombra los 3 archivos reemplazando `salvador` por tu nombre:
```bash
mv k8s/salvador-namespace.yml   k8s/tunombre-namespace.yml
mv k8s/salvador-deployment.yml  k8s/tunombre-deployment.yml
mv k8s/salvador-service.yml     k8s/tunombre-service.yml
```
Y dentro de cada archivo reemplaza `salvador-namespace`, `salvador-deployment`,
`salvador-service` por `tunombre-namespace`, `tunombre-deployment`,
`tunombre-service` (metadata.name y el `namespace:` de deployment/service).

## 2. Antes de aplicar: pon tu imagen de Docker Hub

En `k8s/*-deployment.yml`, cambia:
```yaml
image: usuariodockerhub/ht-232-01-nombre-apellido:latest
```
por la imagen que subiste (ver `docs/DOCKER.md`).

## 3. Aplicar los manifiestos (orden importa)

```bash
kubectl apply -f k8s/salvador-namespace.yml
kubectl apply -f k8s/salvador-deployment.yml
kubectl apply -f k8s/salvador-service.yml

kubectl get pods -n salvador-namespace          # deben verse 3 pods Running
kubectl get svc  -n salvador-namespace          # revisa el NodePort asignado
```

## 4. Probar via NodePort directamente

```
http://localhost:30093/v1/api/student
```
(el `30093` es el `nodePort` definido en `salvador-service.yml`; si Kubernetes
te asigna otro numero automaticamente, usa `kubectl get svc -n salvador-namespace`
para verlo).

## 5. Port-forward (puente local <-> servicio) en un puerto distinto

El punto extra pide un port-forward en un puerto **distinto** al NodePort,
por ejemplo `8094`:

```bash
kubectl port-forward -n salvador-namespace svc/salvador-service 8094:80
```

Mientras ese comando siga corriendo, abre en el navegador:
```
http://localhost:8094/v1/api/student
```

Puedes cambiar `8094` por cualquier puerto libre en tu maquina; el `80` de
la derecha es el puerto del Service (no lo cambies, ya esta mapeado al
`targetPort: 8093` del Deployment).

## 6. Cambiar el puerto del microservicio dentro de Kubernetes

Si quieres correr en otro puerto que no sea `8093`, cambia 3 lugares a la vez
(deben coincidir):

1. `k8s/*-deployment.yml` → `containerPort` y la env var `SERVER_PORT`
2. `k8s/*-deployment.yml` → `readinessProbe`/`livenessProbe` (`port:`)
3. `k8s/*-service.yml` → `targetPort`

El `port: 80` del Service y el `nodePort: 30093` puedes dejarlos como estan,
o cambiar el `nodePort` a cualquier valor libre entre 30000-32767.

## 7. Comandos utiles de troubleshooting

```bash
kubectl describe pod -n salvador-namespace <nombre-del-pod>
kubectl logs -n salvador-namespace <nombre-del-pod>
kubectl rollout restart deployment/salvador-deployment -n salvador-namespace
kubectl delete -f k8s/   # limpia todo lo aplicado
```
