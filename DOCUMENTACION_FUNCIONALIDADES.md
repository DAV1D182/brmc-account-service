# Documentacion de funcionalidades - BRMC Account Service

<<<<<<< HEAD
Este documento describe las funcionalidades disponibles en la primera version del servicio `BRMC Account Service`, una API REST desarrollada con Java y Spring Boot para manejar una cuenta sencilla que puede recibir pagos y emitir reembolsos.

## Objetivo del servicio

El objetivo inicial es permitir la gestion basica de una cuenta:
=======
Este documento describe las funcionalidades disponibles en `BRMC Account Service`, una aplicacion Java Spring Boot inspirada en conceptos operativos de Oracle BRM/Billing Care. El sistema permite gestionar cuentas, pagos, reembolsos, write-offs, disputas, settlements, productos, servicios, billing, bills, items, inventario y eventos de auditoria.

## Objetivo del servicio

El objetivo es permitir una gestion educativa y funcional de ciclo de vida BRM:
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)

- Crear una cuenta para un cliente.
- Consultar la informacion de una cuenta.
- Recibir pagos que aumentan el saldo.
<<<<<<< HEAD
- Enviar reembolsos que disminuyen el saldo.
- Consultar el historial de transacciones.
- Evitar reembolsos cuando no existe saldo suficiente.

La informacion se almacena en memoria, por lo que al reiniciar la aplicacion se pierden las cuentas y transacciones creadas.
=======
- Identificar pagos no asignados mediante `UNALLOCATED`.
- Enviar reembolsos que disminuyen el saldo.
- Crear productos y servicios asociados a cuentas.
- Ejecutar billing usando pin virtual time.
- Generar bills e items desde cargos de billing.
- Gestionar inventario operativo con valor calculado y alertas de nuevo pedido.
- Consultar el historial de transacciones.
- Registrar eventos de auditoria para operaciones principales.
- Evitar reembolsos cuando no existe saldo suficiente.

La informacion se persiste en PostgreSQL local cuando la aplicacion usa la configuracion por defecto.

## Modulos BRM implementados

| Modulo | Tabla principal | Descripcion |
| --- | --- | --- |
| Cuentas | `accounts_t` | Datos del cliente, saldo, estado, DOM, ciclo y `bill_no`. |
| Billinfo | `billinfo_t` | Configuracion de facturacion de la cuenta, similar a `/billinfo`. |
| Productos | `products_t` | Catalogo comercial con cargos ONE_TIME o RECURRING. |
| Servicios | `services_t` | Servicios contratados por cuenta, similar a `/service`. |
| Productos comprados | `service_products_t` | Asociacion activa o cancelada entre servicio y producto. |
| Billing runs | `billing_runs_t` | Ejecuciones de billing general o por cuenta. |
| Billing charges | `billing_charges_t` | Cargos calculados por billing. |
| Bills | `bills_t` | Documento de facturacion creado por cuenta y corrida. |
| Items | `items_t` | Lineas del bill creadas desde cargos de billing, similar a `/item`. |
| Inventario | `inventory_t` | Items operativos con existencias, valor calculado, nuevo pedido y disponibilidad. |
| Pagos | `payments_t` | Pagos recibidos con monto asignado y no asignado. |
| Eventos | `system_events_t` | Auditoria funcional del sistema. |

## Eventos nuevos de billing y pagos

Los eventos quedan registrados en `system_events_t` con `created_t` y `pin_virtual_time_t`:

- `BILLINFO_CREATED`: se crea la configuracion de facturacion de la cuenta.
- `BILL_CREATED`: billing crea un bill para agrupar cargos de una cuenta.
- `ITEM_CREATED`: billing crea un item asociado a un bill y a un cargo.
- `UNALLOCATED_PAYMENT_CREATED`: un pago queda recibido pero pendiente de asignacion.
- `INVENTORY_ITEM_CREATED`: se crea un item de inventario.
- `INVENTORY_ITEM_UPDATED`: se actualiza un item de inventario.
- `INVENTORY_AVAILABILITY_CHANGED`: cambia la disponibilidad de un item de inventario.

## Unallocated Payments

Cada pago nuevo se registra en `payments_t` con:

- `allocated_amount = 0`
- `unallocated_amount = amount`
- `allocation_status = UNALLOCATED`

Esto significa que el pago aumenta el saldo de la cuenta, pero todavia no se aplica directamente a un bill o item especifico. El flujo de asignacion de pagos contra bills queda preparado para una siguiente etapa.

Consultas disponibles:

```text
GET /api/accounts/{accountId}/payments/unallocated
GET /api/payments/unallocated
```

## Billing, Bills e Items

Cuando se ejecuta billing y existen productos facturables:

1. Se crea o usa el `billinfo_t` de la cuenta.
2. Se calcula el periodo del ciclo usando el DOM y el ciclo de la cuenta.
3. Se crea un registro en `bills_t` para la cuenta y el billing run.
4. Cada cargo calculado se guarda en `billing_charges_t`.
5. Cada cargo genera un item en `items_t`.
6. Se actualiza el total del bill.
7. Se registra la siguiente fecha de billing desde el fin del ciclo facturado.
8. Se registran eventos `BILL_CREATED`, `BILLING_CHARGE_CREATED` e `ITEM_CREATED`.

El periodo facturado se muestra en API y UI con:

- `billingDom`: dia del mes usado como DOM.
- `billingCycle`: ciclo configurado, por ejemplo `MONTHLY`.
- `billingPeriodLabel`: mes o rango de meses facturado, por ejemplo `2026-01 a 2026-02`.
- `billPeriodStart`: inicio exacto del ciclo.
- `billPeriodEnd`: fin exacto del ciclo.

Ejemplo: si una cuenta tiene DOM `10`, ciclo `MONTHLY` y el pin virtual time esta en `2026-01-15`, el bill cubre `2026-01-10 00:00:00` a `2026-02-09 23:59:59`. Si se ejecuta billing otra vez sin avanzar el pin virtual time, no se duplican cargos recurrentes del mismo ciclo.

Consultas REST principales:

```text
GET /api/accounts/{accountId}/billinfo
GET /api/accounts/{accountId}/bills
GET /api/accounts/{accountId}/items
GET /api/bills/{billId}
GET /api/bills/{billId}/items
```

## Consulta de services creados

El modulo `Services` permite consultar todos los servicios creados y filtrarlos por cuenta, tipo o estado. Cada service muestra su cuenta propietaria, titular, codigo, tipo, estado y productos asociados.

Pagina web:

```text
GET /services
```

API REST:

```text
GET /api/services
GET /api/services?accountId={accountId}
GET /api/services?serviceType=MOBILE
GET /api/services?status=ACTIVE
GET /api/services?accountId={accountId}&serviceType=MOBILE&status=ACTIVE
GET /api/accounts/{accountId}/services
GET /api/services/{serviceId}
```

Ejemplo PowerShell:

```powershell
Invoke-RestMethod -Method GET `
  -Uri "http://localhost:8080/api/services?serviceType=MOBILE&status=ACTIVE"
```

## Inventario

El modulo de inventario permite crear y consultar items operativos. El campo `inventoryValue` no se captura manualmente: el sistema lo calcula como `unitPrice * stockQuantity`.

Campos principales:

- `id`: identificador funcional con prefijo `INV`.
- `name`: nombre del item.
- `description`: descripcion operativa.
- `unitPrice`: precio por unidad.
- `stockQuantity`: cantidad en existencias.
- `inventoryValue`: valor total calculado.
- `reorderLevel`: nivel del nuevo pedido.
- `reorderTimeDays`: tiempo esperado del nuevo pedido en dias.
- `reorderQuantity`: cantidad sugerida del nuevo pedido.
- `available`: indica si aun se encuentra disponible.
- `needsReorder`: indicador calculado cuando `stockQuantity <= reorderLevel`.

Pagina web:

```text
GET /inventory
```

API REST:

```text
GET /api/inventory
GET /api/inventory?available=true
GET /api/inventory/{inventoryId}
POST /api/inventory
PUT /api/inventory/{inventoryId}
POST /api/inventory/{inventoryId}/available
POST /api/inventory/{inventoryId}/unavailable
```

Ejemplo para crear inventario:

```powershell
Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/inventory" `
  -ContentType "application/json" `
  -Body '{
    "name": "Router BRMC",
    "description": "Equipo para servicio de internet",
    "unitPrice": 150000.00,
    "stockQuantity": 5,
    "reorderLevel": 5,
    "reorderTimeDays": 7,
    "reorderQuantity": 20,
    "available": true
  }'
```

Respuesta esperada:

```json
{
  "id": "INV-2026051022000000012",
  "name": "Router BRMC",
  "unitPrice": 150000.00,
  "stockQuantity": 5,
  "inventoryValue": 750000.00,
  "reorderLevel": 5,
  "reorderTimeDays": 7,
  "reorderQuantity": 20,
  "available": true,
  "needsReorder": true
}
```
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)

## Requisitos para ejecutar

Antes de iniciar el proyecto se requiere:

- Java JDK 17 o superior.
- Apache Maven 3.6.3 o superior.

Validar instalaciones:

```powershell
java -version
mvn -version
```

## Ejecutar la aplicacion

Abrir una terminal PowerShell y entrar a la carpeta del proyecto:

```powershell
cd "C:\Users\David G\Documents\BRMC\brmc-account-service"
```

Iniciar Spring Boot:

```powershell
mvn spring-boot:run
```

Cuando la aplicacion este iniciada, la API queda disponible en:

```text
http://localhost:8080
```

## Funcionalidades y ejemplos

### 1. Crear cuenta

Permite crear una cuenta nueva indicando el nombre del titular y el saldo inicial.

Endpoint:

```text
POST /api/accounts
```

Ejemplo:

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

El campo `id` se debe usar para consultar la cuenta, registrar pagos, registrar reembolsos y listar transacciones.

### 2. Consultar cuenta

Permite consultar el estado actual de una cuenta.

Endpoint:

```text
GET /api/accounts/{accountId}
```

Ejemplo:

```powershell
curl "http://localhost:8080/api/accounts/{accountId}"
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

### 3. Recibir pago

Permite registrar un pago recibido. El monto del pago aumenta el saldo de la cuenta.

Endpoint:

```text
POST /api/accounts/{accountId}/payments
```

Ejemplo:

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/payments" `
  -ContentType "application/json" `
  -Body '{ "amount": 50.00, "description": "Pago recibido por servicio" }'
```

Respuesta esperada:

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "type": "PAYMENT",
  "amount": 50.00,
  "description": "Pago recibido por servicio",
  "createdAt": "2026-05-07T16:05:00Z"
}
```

Si la cuenta tenia saldo `100.00`, despues del pago queda con saldo `150.00`.

### 4. Enviar reembolso

Permite registrar un reembolso. El monto del reembolso disminuye el saldo de la cuenta.

Endpoint:

```text
POST /api/accounts/{accountId}/refunds
```

Ejemplo:

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/refunds" `
  -ContentType "application/json" `
  -Body '{ "amount": 20.00, "description": "Reembolso parcial al cliente" }'
```

Respuesta esperada:

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "type": "REFUND",
  "amount": 20.00,
  "description": "Reembolso parcial al cliente",
  "createdAt": "2026-05-07T16:10:00Z"
}
```

Si la cuenta tenia saldo `150.00`, despues del reembolso queda con saldo `130.00`.

### 5. Listar transacciones

Permite consultar todos los pagos y reembolsos registrados para una cuenta.

Endpoint:

```text
GET /api/accounts/{accountId}/transactions
```

Ejemplo:

```powershell
curl "http://localhost:8080/api/accounts/{accountId}/transactions"
```

Respuesta esperada:

```json
[
  {
    "id": "22222222-2222-2222-2222-222222222222",
    "type": "PAYMENT",
    "amount": 50.00,
    "description": "Pago recibido por servicio",
    "createdAt": "2026-05-07T16:05:00Z"
  },
  {
    "id": "33333333-3333-3333-3333-333333333333",
    "type": "REFUND",
    "amount": 20.00,
    "description": "Reembolso parcial al cliente",
    "createdAt": "2026-05-07T16:10:00Z"
  }
]
```

## Validaciones

### Saldo inicial

El saldo inicial no puede ser negativo.

Ejemplo invalido:

```powershell
curl -Method POST "http://localhost:8080/api/accounts" `
  -ContentType "application/json" `
  -Body '{ "ownerName": "Cliente BRMC", "initialBalance": -10.00 }'
```

Respuesta esperada:

```text
400 Bad Request
```

### Monto de pago o reembolso

El monto debe ser mayor a cero.

Ejemplo invalido:

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/payments" `
  -ContentType "application/json" `
  -Body '{ "amount": 0.00, "description": "Pago invalido" }'
```

Respuesta esperada:

```text
400 Bad Request
```

### Reembolso con saldo insuficiente

No se permite enviar un reembolso mayor al saldo disponible.

Ejemplo:

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/refunds" `
  -ContentType "application/json" `
  -Body '{ "amount": 9999.00, "description": "Reembolso superior al saldo" }'
```

Respuesta esperada:

```text
409 Conflict
```

## Flujo completo de prueba

Este flujo permite probar la aplicacion de inicio a fin.

### Paso 1: iniciar la aplicacion

```powershell
cd "C:\Users\David G\Documents\BRMC\brmc-account-service"
mvn spring-boot:run
```

### Paso 2: crear cuenta

```powershell
curl -Method POST "http://localhost:8080/api/accounts" `
  -ContentType "application/json" `
  -Body '{ "ownerName": "Cliente BRMC", "initialBalance": 100.00 }'
```

Copiar el valor de `id` de la respuesta.

### Paso 3: registrar pago

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/payments" `
  -ContentType "application/json" `
  -Body '{ "amount": 50.00, "description": "Pago recibido" }'
```

### Paso 4: registrar reembolso

```powershell
curl -Method POST "http://localhost:8080/api/accounts/{accountId}/refunds" `
  -ContentType "application/json" `
  -Body '{ "amount": 20.00, "description": "Reembolso enviado" }'
```

### Paso 5: consultar saldo final

```powershell
curl "http://localhost:8080/api/accounts/{accountId}"
```

Saldo esperado:

```text
130.00
```

### Paso 6: consultar historial

```powershell
curl "http://localhost:8080/api/accounts/{accountId}/transactions"
```

## Ejecutar pruebas automatizadas

El proyecto incluye pruebas de integracion para validar el flujo principal.

```powershell
cd "C:\Users\David G\Documents\BRMC\brmc-account-service"
mvn test
```

Las pruebas verifican:

- Creacion de cuenta.
- Registro de pago.
- Registro de reembolso.
- Consulta de saldo final.
- Listado de transacciones.
- Error cuando el reembolso supera el saldo disponible.

## Observaciones tecnicas

- La aplicacion usa una API REST.
<<<<<<< HEAD
- El almacenamiento actual es en memoria.
- Los identificadores de cuenta y transaccion son UUID.
=======
- El almacenamiento se realiza en PostgreSQL local por defecto; las pruebas usan H2 en memoria.
- Los identificadores principales se generan con formatos legibles basados en fecha y prefijos funcionales cuando aplica.
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)
- Los montos se manejan con `BigDecimal`.
- Los errores se devuelven usando respuestas tipo `ProblemDetail`.
- La concurrencia basica esta protegida sincronizando operaciones sobre cada cuenta.

## Siguientes mejoras recomendadas

<<<<<<< HEAD
- Agregar base de datos H2 o PostgreSQL.
- Agregar Swagger/OpenAPI para documentacion interactiva.
- Agregar autenticacion.
=======
- Agregar Swagger/OpenAPI para documentacion interactiva.
>>>>>>> 9a008f4 (Subir proyecto BRMC _v2)
- Agregar idempotencia para evitar pagos o reembolsos duplicados.
- Agregar estados de transaccion.
- Agregar pruebas adicionales de validacion.
