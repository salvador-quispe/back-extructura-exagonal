# CUSTOMIZE.md — Como adaptar este backend a otro proyecto del hackaton

Este proyecto sigue **arquitectura hexagonal (Ports & Adapters)**. La idea:
el "dominio" (las reglas de negocio) no sabe nada de HTTP ni de bases de
datos; los "adaptadores" son los que traducen HTTP <-> dominio <-> BD.

```
src/main/java/pe/edu/vallegrande/student/
├── domain/
│   ├── model/              Entidad de negocio pura (Student.java)
│   └── port/
│       ├── in/              Interfaces que la app EXPONE (casos de uso)
│       └── out/              Interfaces que la app NECESITA (repositorio)
├── application/
│   └── service/             Implementacion del caso de uso (logica + logs)
└── infrastructure/
    ├── adapter/in/web/       Controller REST + DTOs (entrada)
    ├── adapter/out/persistence/  JPA Entity + Repository + mapeos (salida)
    └── config/               Seed de datos, manejo global de errores
```

Regla de oro: **el dominio nunca importa nada de `infrastructure`**. Todo lo
que cruza esa frontera pasa por una interfaz (`port`).

---

## 1. Cambiar de "Student" a otro recurso (ej. "Product", "Order")

Usa buscar-y-reemplazar en todo el proyecto (`Student` -> `Product`,
`student` -> `product`, `dni` -> tu campo identificador):

| Archivo a editar | Que cambiar |
|---|---|
| `domain/model/Student.java` | Renombra la clase y sus campos (`dni`, `firstName`, etc.) |
| `domain/port/in/StudentUseCase.java` | Renombra metodos si tu identificador no es `dni` |
| `domain/port/out/StudentRepositoryPort.java` | Igual que arriba |
| `application/service/StudentService.java` | Logica de negocio + los `log.info(...)` de auditoria |
| `infrastructure/adapter/out/persistence/StudentEntity.java` | Anotaciones `@Entity`/`@Table`/`@Column` — aqui defines el esquema real de BD |
| `infrastructure/adapter/out/persistence/StudentJpaRepository.java` | Metodos derivados de Spring Data |
| `infrastructure/adapter/out/persistence/StudentPersistenceMapper.java` | Conversion Entity <-> dominio |
| `infrastructure/adapter/in/web/StudentController.java` | Rutas REST (`@RequestMapping`) |
| `infrastructure/adapter/in/web/dto/StudentRequest.java` | Validaciones del body de entrada (`@NotBlank`, `@Pattern`, etc.) |
| `infrastructure/adapter/in/web/dto/StudentResponse.java` | Forma del JSON de salida |
| `infrastructure/adapter/in/web/mapper/StudentWebMapper.java` | Conversion DTO <-> dominio |
| `infrastructure/config/DataSeeder.java` | Dato inicial que se carga al arrancar |

No necesitas tocar `application.yml` ni el `Dockerfile` para este cambio.

---

## 2. Agregar un campo nuevo (ejemplo: `email`)

1. `Student.java` → agrega el campo + getter/setter.
2. `StudentEntity.java` → agrega `@Column(name = "email")` + getter/setter.
3. `StudentPersistenceMapper.java` → copia el campo en `toEntity`/`toDomain`.
4. `StudentRequest.java` → agrega el campo + validacion si aplica.
5. `StudentResponse.java` → agrega el campo si debe verse en el JSON de salida.
6. `StudentWebMapper.java` → copia el campo en `toDomain`/`toResponse`.

Con `ddl-auto: update` (ver `application.yml`) H2/MySQL/Postgres crean la
columna solos, no necesitas escribir SQL a mano durante el hackaton.

---

## 3. Cambiar el puerto de ejecucion

El puerto **nunca** debe quedar fijo en el codigo. Ya esta resuelto via
variable de entorno en `src/main/resources/application.yml`:

```yaml
server:
  port: ${SERVER_PORT:8090}
```

Formas de cambiarlo:
- Local: `SERVER_PORT=9000 mvn spring-boot:run` (bash) o
  `$env:SERVER_PORT=9000; mvn spring-boot:run` (PowerShell)
- Docker: variable `SERVER_PORT` en `docker-compose.yml` o `docker run -e SERVER_PORT=...`
- Kubernetes: variable `SERVER_PORT` en `k8s/*-deployment.yml`

---

## 4. Cambiar de base de datos (H2 -> MySQL -> Postgres -> Mongo)

`application.yml` tiene 3 perfiles listos: `h2` (default), `mysql`,
`postgres`. Cambias de uno a otro con la variable `SPRING_PROFILES_ACTIVE`,
**sin tocar codigo Java** (JPA es agnostico al motor SQL).

Si quieres Mongo u otra BD no relacional:
1. En `pom.xml`, comenta/quita `spring-boot-starter-data-jpa`, `h2`,
   `mysql-connector-j`, `postgresql`, y descomenta
   `spring-boot-starter-data-mongodb` (ya viene comentado con instrucciones).
2. Cambia `StudentEntity.java` (quita anotaciones JPA, usa `@Document` de
   Spring Data Mongo) y `StudentJpaRepository.java` (extiende
   `MongoRepository` en vez de `JpaRepository`).
3. El resto del proyecto (domain, application, controller) **no cambia**,
   porque solo dependen de los `port`, no de JPA. Esa es la ventaja de la
   arquitectura hexagonal.

---

## 5. Agregar/quitar dependencias rapido

Todas las dependencias opcionales estan en `pom.xml`, comentadas con una
explicacion de para que sirven (seguridad, swagger, redis, kafka,
webflux...). Para activarlas, quita el comentario `<!-- -->` que las
envuelve. Para desactivar una que no usas, comentala o borrala.

---

## 6. Renombrar el proyecto completo (groupId/artifactId)

En `pom.xml`:
```xml
<groupId>tu-usuario</groupId>
<artifactId>tu-proyecto</artifactId>
```
Si tambien renombras el paquete Java (`pe.edu.vallegrande.student`), debes
mover las carpetas en `src/main/java/...` y `src/test/java/...` y
actualizar el `package` en cada archivo `.java` (buscar y reemplazar).
