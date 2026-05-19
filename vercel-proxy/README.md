# BRMC en Vercel

Esta carpeta permite publicar una URL de Vercel para BRMC usando Vercel como proxy HTTP.

Importante: el backend Spring Boot debe estar corriendo en un hosting compatible con Java, por ejemplo Render, Railway, Fly.io o Cloud Run. Vercel recibe la peticion publica y la reenvia al backend.

## Variable requerida

Configura esta variable en Vercel:

```text
BRMC_BACKEND_URL=https://tu-backend.onrender.com
```

No incluyas slash final.

## Como desplegar

1. Sube el repositorio a GitHub.
2. En Vercel crea un nuevo proyecto.
3. Selecciona el mismo repositorio.
4. En `Root Directory`, selecciona:

```text
vercel-proxy
```

5. En Environment Variables agrega `BRMC_BACKEND_URL`.
6. Despliega.

Al terminar, abre:

```text
https://tu-proyecto.vercel.app/login
```

## Flujo esperado

```text
Browser -> Vercel -> Spring Boot backend -> PostgreSQL
```

## Limitacion

Este proxy no reemplaza el hosting Java. Si el backend esta apagado, la URL de Vercel tambien fallara.
