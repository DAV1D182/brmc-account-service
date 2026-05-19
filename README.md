# BRMC Account Service

<<<<<<< HEAD
API REST sencilla en Java y Spring Boot para crear una cuenta, recibir pagos y emitir reembolsos.
=======
Aplicacion Java Spring Boot inspirada en conceptos de Oracle BRM/Billing Care para gestionar cuentas, pagos, reembolsos, disputas, productos, servicios, billing, bills, items y eventos.
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)

## Funcionalidad inicial

- Crear una cuenta con nombre de titular y saldo inicial.
- Consultar una cuenta por id.
<<<<<<< HEAD
- Registrar pagos recibidos en la cuenta.
- Registrar reembolsos emitidos desde la cuenta.
- Listar las transacciones de la cuenta.
- Validar montos mayores a cero y evitar reembolsos con saldo insuficiente.

> Nota: esta primera version guarda la informacion en memoria. Al reiniciar la aplicacion se pierden las cuentas y transacciones. Es ideal para iniciar rapido; el siguiente paso natural seria agregar una base de datos.
=======
- Registrar pagos recibidos en la cuenta y marcarlos como `UNALLOCATED` hasta que exista aplicacion a bills.
- Registrar reembolsos emitidos desde la cuenta.
- Listar las transacciones de la cuenta.
- Configurar `billinfo_t` por cuenta.
- Ejecutar billing y generar `bills_t`, `items_t` y `billing_charges_t`.
- Validar montos mayores a cero y evitar reembolsos con saldo insuficiente.

> Nota: la configuracion actual usa PostgreSQL local por defecto. Para pruebas automatizadas se usa H2 en memoria desde `src/test/resources`.
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)

## Requisitos

- Java JDK 17 o superior.
- Apache Maven 3.6.3 o superior.

Para verificar:

```powershell
java -version
mvn -version
```

## Como ejecutar

Desde esta carpeta:

```powershell
cd "C:\Users\David G\Documents\BRMC\brmc-account-service"
mvn spring-boot:run
```

La API quedara disponible en:

```text
http://localhost:8080
```

## Como probar con curl

### 1. Crear una cuenta

```powershell
curl -Method POST "http://localhost:8080/api/accounts" `
  -ContentType "application/json" `
  -Body '{ "ownerName": "Cliente BRMC", "initialBalance": 100.00 }'
```

Respuesta esperada:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "ownerName": "Cliente BRMC",
  "balance": 100.00,
  "createdAt": "2026-05-07T16:00:00Z"
}
```

Guarda el valor de `id` para las siguientes pruebas.

### 2. Recibir un pago

Reemplaza `{accountId}` por el id real de la cuenta.

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/payments" `
  -ContentType "application/json" `
  -Body '{ "amount": 50.00, "description": "Pago recibido" }'
```

El saldo de la cuenta aumenta en `50.00`.

### 3. Enviar un reembolso

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/refunds" `
  -ContentType "application/json" `
  -Body '{ "amount": 20.00, "description": "Reembolso al cliente" }'
```

El saldo de la cuenta disminuye en `20.00`. Si no hay saldo suficiente, la API responde `409 Conflict`.

### 4. Consultar la cuenta

```powershell
curl "http://localhost:8080/api/accounts/{accountId}"
```

### 5. Listar transacciones

```powershell
curl "http://localhost:8080/api/accounts/{accountId}/transactions"
```

## Ejecutar pruebas

```powershell
mvn test
```

## Endpoints

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| POST | `/api/accounts` | Crea una cuenta. |
| GET | `/api/accounts/{accountId}` | Consulta una cuenta. |
| POST | `/api/accounts/{accountId}/payments` | Registra un pago recibido. |
| POST | `/api/accounts/{accountId}/refunds` | Registra un reembolso enviado. |
| GET | `/api/accounts/{accountId}/transactions` | Lista transacciones. |
<<<<<<< HEAD
=======
| GET | `/api/accounts/{accountId}/payments/unallocated` | Lista pagos no asignados de una cuenta. |
| GET | `/api/payments/unallocated` | Lista todos los pagos no asignados. |
| POST | `/api/billing/run` | Ejecuta billing general. |
| POST | `/api/billing/accounts/{accountId}/run` | Ejecuta billing para una cuenta. |
| GET | `/api/accounts/{accountId}/billinfo` | Consulta el billinfo de la cuenta. |
| GET | `/api/accounts/{accountId}/bills` | Lista bills de la cuenta. |
| GET | `/api/accounts/{accountId}/items` | Lista items de la cuenta. |
| GET | `/api/bills/{billId}` | Consulta un bill con sus items. |
| GET | `/api/bills/{billId}/items` | Lista items de un bill. |

## Tablas BRM principales

- `billinfo_t`: configuracion de facturacion de la cuenta.
- `bills_t`: bills generados por billing.
- `items_t`: items creados desde cargos de billing.
- `payments_t`: pagos con `allocated_amount`, `unallocated_amount` y `allocation_status`.
- `system_events_t`: eventos `BILLINFO_CREATED`, `BILL_CREATED`, `ITEM_CREATED` y `UNALLOCATED_PAYMENT_CREATED`, entre otros.
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)

## Estructura

```text
src/main/java/com/brmc/account
  AccountServiceApplication.java
  AccountController.java
  AccountService.java
  Account.java
  AccountTransaction.java
  ApiExceptionHandler.java
  AccountNotFoundException.java
  InsufficientBalanceException.java
  AccountResponse.java
  TransactionResponse.java
  TransactionType.java

src/test/java/com/brmc/account
  AccountControllerTest.java
```

## Siguientes pasos sugeridos

- Agregar persistencia con PostgreSQL o H2.
- Crear documentacion OpenAPI/Swagger.
- Agregar autenticacion.
- Agregar idempotencia para pagos y reembolsos.
- Separar el modelo de dominio de los DTOs si el servicio crece.
