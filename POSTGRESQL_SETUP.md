# Configuracion PostgreSQL - BRMC Account Service

Esta guia explica como preparar PostgreSQL para que la aplicacion guarde cuentas, pagos, reembolsos y cierres de cuenta en base de datos.

## 1. Instalar PostgreSQL

Descarga e instala PostgreSQL para Windows.

Durante la instalacion:

- Guarda la clave del usuario `postgres`.
- Mantén el puerto por defecto: `5432`.
- Puedes instalar pgAdmin si quieres una interfaz visual para ver la base.

## 2. Crear la base de datos

Abre pgAdmin o SQL Shell `psql` y crea la base:

```sql
CREATE DATABASE brmc_db;
```

## 3. Configurar usuario y clave en Spring Boot

El archivo de configuracion esta en:

```text
C:\Users\David G\Documents\BRMC\brmc-account-service\src\main\resources\application.properties
```

Configuracion actual:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brmc_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

Si tu clave de PostgreSQL no es `postgres`, cambia esta linea:

```properties
spring.datasource.password=TU_CLAVE_REAL
```

## 4. Ejecutar la aplicacion

```powershell
cd "C:\Users\David G\Documents\BRMC\brmc-account-service"
mvn spring-boot:run
```

Al iniciar, Spring Boot crea o actualiza las tablas automaticamente porque está activo:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## 5. Tablas creadas

La aplicacion crea estas tablas:

- `accounts`
- `account_transactions`

## 6. Flujo de prueba

1. Entrar a:

```text
http://localhost:8080
```

2. Crear una cuenta.
3. Registrar un pago.
4. Registrar un reembolso.
5. Cerrar y volver a abrir la aplicacion.
6. Validar que la cuenta sigue existiendo.

## 7. Ver datos en PostgreSQL

Puedes consultar:

```sql
SELECT * FROM accounts;
SELECT * FROM account_transactions;
```

## 8. Nota sobre pruebas

Las pruebas automatizadas usan H2 en memoria para no depender de PostgreSQL. La aplicacion real usa PostgreSQL.
