# =====================================================================
# Dockerfile MULTISTAGE del frontend Angular.
#   Stage 1: compila el proyecto Angular (npm ci + ng build).
#   Stage 2: sirve el build estatico con nginx (imagen final chica).
#
# El backend a usar se configura en RUNTIME via variables de entorno
# (API_BASE_URL, ID_FIELD, APP_TITLE) sin recompilar Angular:
# docker-entrypoint.sh genera config.json a partir de config.template.json
# cada vez que arranca el contenedor. Ver docs/FRONTEND.md.
#
# Build: docker build -t frontend-crud .
# Run:   docker run -p 8095:80 -e API_BASE_URL=http://localhost:8092/v1/api/student frontend-crud
# =====================================================================

# ---------- STAGE 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# ---------- STAGE 2: runtime (nginx) ----------
FROM nginx:1.27-alpine

RUN apk add --no-cache gettext   # provee envsubst

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY config.template.json /usr/share/nginx/html/config.template.json
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /docker-entrypoint.d/40-inject-config.sh
RUN chmod +x /docker-entrypoint.d/40-inject-config.sh

EXPOSE 80
