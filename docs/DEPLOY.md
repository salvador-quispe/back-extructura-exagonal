# DEPLOY.md — Publicar en Render / Railway / Koyeb

Este backend esta listo para desplegarse en cualquier plataforma que reciba
un contenedor Docker o un build Java/Maven. Usa el perfil `postgres` para
conectarte a una base de datos cloud gratuita.

## 1. Elegir base de datos cloud

- Render: "New +" → "PostgreSQL" (crea un `Internal Database URL` y uno
  `External`).
- Railway: plugin "PostgreSQL" dentro del proyecto.
- Koyeb: puedes usar Neon.tech o Supabase (Postgres gratis) como BD externa.

Copia la cadena de conexion, usuario y password.

## 2. Variables de entorno a configurar en la plataforma

```
SERVER_PORT=8080                # casi todas las plataformas inyectan su propio PORT; usa esa var si aplica (ver seccion 4)
SPRING_PROFILES_ACTIVE=postgres
DATABASE_URL=jdbc:postgresql://<host>:<puerto>/<db>
DB_USER=<usuario>
DB_PASSWORD=<password>
```

Estas variables ya estan mapeadas en `application.yml` (perfil `postgres`),
no necesitas tocar codigo.

## 3. Desplegar

### Opcion A: Build automatico desde el repo (Render "Web Service")
1. Conecta tu repositorio de GitHub.
2. Runtime: Docker (usa el `Dockerfile` de la raiz, o el
   `Dockerfile.distroless-fallback` si el nativo te da problemas en su CI).
3. Agrega las variables de entorno de la seccion 2.
4. Deploy.

### Opcion B: Push de la imagen ya construida
```bash
docker build -f Dockerfile.distroless-fallback -t usuariodockerhub/ht-232-01-nombre-apellido .
docker push usuariodockerhub/ht-232-01-nombre-apellido
```
Luego en Render/Railway/Koyeb eliges "Deploy from image" y pegas la imagen
de Docker Hub.

## 4. Puerto: la plataforma manda

Render/Railway/Koyeb inyectan su propia variable `PORT` y esperan que tu
app escuche ahi. Como nuestro `server.port` ya usa `${SERVER_PORT:8090}`,
simplemente define en la plataforma:
```
SERVER_PORT=${PORT}
```
(en Render esto se hace automatico si usas su variable de entorno especial;
revisa la doc de la plataforma elegida, algunas requieren que definas
`SERVER_PORT` con el mismo valor numerico que exponen).

## 5. Checklist para la evidencia que pide el enunciado

- [ ] **Registro exitoso con validaciones**: prueba `POST /v1/api/student`
      con un `dni` invalido (menos de 8 digitos) y confirma que responde
      `400 Bad Request` (ver `GlobalExceptionHandler.java`). Luego un
      registro valido y confirma `201 Created`.
- [ ] **Listar y editar con validaciones**: `GET /v1/api/student` (lista) y
      `PUT /v1/api/student/{dni}` con datos validos/invalidos.
- [ ] **Eliminar / restaurar**: `DELETE /v1/api/student/{dni}`. Si tu
      proyecto real (PRS) requiere "restaurar" (soft delete), agrega un
      campo `active: boolean` en `Student`/`StudentEntity` en vez de borrar
      la fila — ver `docs/CUSTOMIZE.md` seccion 2 para el patron a seguir.

Adjunta el link publico de tu servicio desplegado + capturas o un video
corto probando los 3 puntos de arriba.
