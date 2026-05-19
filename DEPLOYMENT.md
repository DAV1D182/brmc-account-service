# Despliegue BRMC Account Service

Esta guia prepara el proyecto para desplegarlo como aplicacion Spring Boot con PostgreSQL en una plataforma tipo Render, Railway, Fly.io o Cloud Run.

## Recomendacion

Para este proyecto no conviene desplegar directamente en Vercel porque la aplicacion es un backend Spring Boot tradicional con JPA, sesiones, login y PostgreSQL. Vercel funciona mejor para frontends y funciones serverless.

Usa una de estas opciones:

- Render Web Service con Docker.
- Railway con Docker.
- Fly.io.
- Google Cloud Run.

## Variables de entorno

Configura estas variables en la plataforma:

| Variable | Ejemplo | Descripcion |
| --- | --- | --- |
| `PORT` | `8080` o el valor que entregue la plataforma | Puerto HTTP usado por Spring Boot. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://host:5432/brmc_db` | URL JDBC de PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | `brmc_user` | Usuario de base de datos. |
| `SPRING_DATASOURCE_PASSWORD` | `********` | Contrasena de base de datos. |
| `BRMC_APP_USERNAME` | `admin` | Usuario administrador inicial. |
| `BRMC_APP_PASSWORD` | `********` | Contrasena administrador inicial. |
| `BRMC_EXCHANGE_REMOTE_ENABLED` | `false` | Desactiva consulta remota de TRM si el hosting no permite salida estable. |
| `BRMC_USD_COP_FALLBACK` | `4000.00` | TRM de respaldo. |

Tambien se soporta `DATABASE_URL` con formato `postgres://user:pass@host:port/db`, comun en algunas plataformas. La aplicacion lo convierte automaticamente a JDBC.

## Despliegue en Render

1. Sube el repositorio a GitHub.
2. En Render crea un PostgreSQL.
3. Crea un Web Service conectado al repositorio.
4. Selecciona deploy con Docker.
5. Configura las variables de entorno.
6. Si Render entrega una URL `postgres://...`, puedes ponerla como `DATABASE_URL`.
7. Despliega.

## Despliegue en Railway

1. Sube el repositorio a GitHub.
2. En Railway crea un proyecto desde el repositorio.
3. Agrega PostgreSQL al proyecto.
4. Usa Dockerfile como metodo de build.
5. Configura `DATABASE_URL` o las variables `SPRING_DATASOURCE_*`.
6. Configura `BRMC_APP_USERNAME` y `BRMC_APP_PASSWORD`.
7. Despliega.

## Prueba local con Docker

Primero genera una base PostgreSQL local o usa una remota. Luego ejecuta:

```powershell
docker build -t brmc-account-service .
docker run --rm -p 8080:8080 `
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/brmc_db" `
  -e SPRING_DATASOURCE_USERNAME="postgres" `
  -e SPRING_DATASOURCE_PASSWORD="postgres" `
  -e BRMC_APP_USERNAME="admin" `
  -e BRMC_APP_PASSWORD="admin123" `
  brmc-account-service
```

Abre:

```text
http://localhost:8080/login
```

## Notas importantes

- `spring.jpa.hibernate.ddl-auto=update` crea/actualiza tablas automaticamente. Sirve para este proyecto educativo.
- En produccion real se recomienda migrar a Flyway o Liquibase.
- Cambia siempre `BRMC_APP_PASSWORD`; no uses `admin123` en internet.
- Si ya existia el usuario `admin`, la aplicacion no reemplaza su password automaticamente.
