# =====================================================================
# Dockerfile MULTISTAGE -> imagen final "scratch"
#   - Compila un binario NATIVO y ESTATICO con GraalVM (musl), por eso
#     puede correr sobre "scratch" (que no tiene ni libc).
#   - Corre como usuario NO ROOT (UID 10001, definido a mano porque
#     scratch no tiene /etc/passwd).
#   - Tamano final objetivo: < 60 MB.
#
# Build:   docker build -t usuariodockerhub/ht-232-01-nombre-apellido .
# Run:     docker run -p 8091:8091 -e SERVER_PORT=8091 usuariodockerhub/ht-232-01-nombre-apellido
#
# Si el build nativo falla en tu maquina (falta de RAM, etc.), revisa
# docs/DOCKER.md seccion "Alternativa distroless" para un Dockerfile
# alterno mas simple (no cumple 100% "scratch" pero sirve para probar).
# =====================================================================

# ---------- STAGE 1: build nativo con GraalVM ----------
FROM ghcr.io/graalvm/native-image-community:21-muslib AS build

# Maven no viene incluido en esta imagen base
RUN microdnf install -y maven || (curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -o /tmp/mvn.tar.gz \
    && tar -xzf /tmp/mvn.tar.gz -C /opt && ln -s /opt/apache-maven-3.9.9/bin/mvn /usr/bin/mvn)

WORKDIR /workspace

# Copiamos solo el pom primero para cachear dependencias
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true

COPY src src

# Compila el binario nativo estatico (perfil "native" del pom.xml)
RUN mvn -Pnative -DskipTests native:compile -B

# El binario queda en target/app
RUN ls -la target/

# ---------- STAGE 2: imagen final (vacia) ----------
FROM scratch

# Usuario no root (UID/GID arbitrarios, no requieren /etc/passwd)
USER 10001:10001

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/target/app /app/app

# Puerto flexible via variable de entorno (ver docs/CUSTOMIZE.md)
ENV SERVER_PORT=8091
EXPOSE 8091

ENTRYPOINT ["/app/app"]
