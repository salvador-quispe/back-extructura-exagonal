#!/bin/sh
set -e

# La imagen oficial de nginx ejecuta automaticamente todos los scripts
# de /docker-entrypoint.d/*.sh ANTES de arrancar nginx. Este script
# genera config.json desde la plantilla, inyectando las variables de
# entorno del contenedor. Asi puedes apuntar el frontend a CUALQUIERA
# de tus backends con "docker run -e API_BASE_URL=..." SIN reconstruir
# la imagen. Ver docs/FRONTEND.md en la raiz del repo.
export API_BASE_URL="${API_BASE_URL:-http://localhost:8090/v1/api/student}"
export ID_FIELD="${ID_FIELD:-dni}"
export APP_TITLE="${APP_TITLE:-CRUD Student}"

envsubst '${API_BASE_URL} ${ID_FIELD} ${APP_TITLE}' \
  < /usr/share/nginx/html/config.template.json \
  > /usr/share/nginx/html/config.json
