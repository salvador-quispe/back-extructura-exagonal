# FRONTEND.md — Frontend Angular generico (CRUD)

`frontend/` es un proyecto **Angular 18 real** (generado con `@angular/cli`,
standalone components, sin routing porque es de una sola pantalla). Hace
CRUD completo (listar, crear, editar, eliminar) contra **cualquier backend**
que exponga las rutas REST estandar (`GET/POST/PUT/DELETE` sobre una URL
base), sin importar si es este mismo backend Spring Boot u otro de tus
otros 2 backends del hackaton.

## La idea clave: configuracion en RUNTIME, no en build

Angular normalmente resuelve la URL del backend en tiempo de compilacion
(`environment.ts`). Aqui en cambio se usa **`public/config.json`**, que se
descarga con `fetch` cuando la app arranca (`core/config.service.ts` +
`APP_INITIALIZER` en `app.config.ts`). Ventaja: **puedes cambiar de backend
sin recompilar Angular**, incluso en un contenedor Docker ya construido.

```
frontend/
├── public/config.json          <- EDITA ESTO para desarrollo local
├── config.template.json        <- plantilla usada solo dentro de Docker
├── docker-entrypoint.sh        <- genera config.json desde variables de entorno
├── nginx.conf                  <- sirve el build + nunca cachea config.json
├── Dockerfile                  <- multistage: build Angular -> nginx
└── src/app/
    ├── core/
    │   ├── config.model.ts     <- forma de config.json (TypeScript)
    │   ├── config.service.ts   <- lo descarga una vez al iniciar
    │   └── crud.service.ts     <- cliente HTTP generico (list/create/update/remove)
    ├── app.component.ts        <- logica de la pantalla (tabla + formulario)
    ├── app.component.html      <- template
    └── app.component.css       <- estilos
```

---

## 1. Conectar a OTRO backend (el caso mas comun)

### En desarrollo local (`ng serve` / `npm start`)
Edita `frontend/public/config.json`:
```json
{
  "apiBaseUrl": "http://localhost:5000/api/products",
  "idField": "id",
  "appTitle": "CRUD Productos",
  "fields": [ ... ]
}
```
Guarda y recarga el navegador. No hace falta recompilar nada.

### En Docker (imagen ya construida)
```bash
docker run -p 8095:80 \
  -e API_BASE_URL=http://localhost:5000/api/products \
  -e ID_FIELD=id \
  -e APP_TITLE="CRUD Productos" \
  frontend-crud
```
Estas 3 variables se inyectan en `config.json` al arrancar el contenedor
(`docker-entrypoint.sh`). El campo `fields` **no** se pasa por variable de
entorno (seria un JSON incomodo de escribir en `docker run`/compose); para
cambiarlo en Docker edita `frontend/config.template.json` y reconstruye la
imagen, o monta tu propio `config.json` con un volumen:
```bash
docker run -p 8095:80 -v ./mi-config.json:/usr/share/nginx/html/config.json:ro frontend-crud
```

### En `docker-compose.yml` (raiz del repo)
El servicio `frontend` ya lee estas variables desde tu `.env`:
```
FRONTEND_API_BASE_URL=http://localhost:8092/v1/api/student
FRONTEND_ID_FIELD=dni
FRONTEND_APP_TITLE=CRUD Student
```
Cambialas ahi y corre `docker compose up -d --build frontend`.

---

## 2. Cambiar los campos del formulario/tabla (otro modelo de datos)

En `config.json` (o `config.template.json` para Docker), el array `fields`
define TODO el formulario y la tabla, sin tocar el codigo Angular:

```json
{
  "key": "email",
  "label": "Correo",
  "type": "email",
  "required": true,
  "editable": true,
  "showInTable": true
}
```

| Campo | Que hace |
|---|---|
| `key` | nombre exacto tal cual lo espera/devuelve tu backend en el JSON |
| `label` | texto visible en la UI |
| `type` | `"text"` \| `"number"` \| `"date"` \| `"email"` |
| `required` | si se valida como obligatorio en el formulario reactivo |
| `editable` | `false` = el input queda deshabilitado al editar (tipico para el ID) |
| `showInTable` | `false` = no se muestra como columna (pero se sigue enviando en el payload) |

`idField` en `config.json` debe coincidir con el `key` que uses como
identificador (el que va en la URL `/recurso/{id}` para GET/PUT/DELETE).

**No necesitas tocar** `app.component.ts/html` para agregar/quitar campos —
solo si cambias el comportamiento visual (orden de columnas fijo,
formateo especial de fechas, etc.).

---

## 3. Si tu backend no sigue el estandar REST (`GET/POST/PUT/DELETE`)

Edita `src/app/core/crud.service.ts` — es el unico lugar que arma las
peticiones HTTP. Por ejemplo, si tu backend usa `/recurso/create` en vez de
`POST /recurso`, cambia el metodo `create()` ahi.

---

## 4. Correr en local (sin Docker)

```bash
cd frontend
npm install
npm start          # abre http://localhost:4200
```

Angular hace proxy directo al backend que pusiste en `public/config.json`.
Si tu backend tiene CORS restringido, agrega `http://localhost:4200` a sus
origenes permitidos, o corre el backend con CORS abierto durante la demo.

## 5. Build de produccion

```bash
npm run build -- --configuration production
# salida en dist/frontend/browser/
```

## 6. Docker

```bash
docker build -t frontend-crud ./frontend
docker run -p 8095:80 -e API_BASE_URL=http://localhost:8092/v1/api/student frontend-crud
```
Ver `docs/DOCKER.md` para el resto de convenciones de Docker del hackaton
(Docker Hub, docker-compose).
