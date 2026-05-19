package com.brmc.account;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private RefundRecordRepository refundRecordRepository;

    @Autowired
    private BillInfoRepository billInfoRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceLineRepository invoiceLineRepository;

    @Autowired
    private CreditNoteRepository creditNoteRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    @WithAnonymousUser
    void showsLoginPageAndProtectsHome() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresa tus credenciales")))
                .andExpect(content().string(containsString("Crear usuario")));

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(formLogin("/login").user("admin").password("admin123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithAnonymousUser
    void registersUserFromLoginPage() throws Exception {
        var username = "cliente.login";

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", "cliente123")
                        .param("fullName", "Cliente Login")
                        .param("email", "cliente.login@brmc.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/login?registered")));

        var user = appUserRepository.findById(username).orElseThrow();
        assertThat(user.role()).isEqualTo(AppRole.USER);
        assertThat(user.status()).isEqualTo(AppUserStatus.ACTIVE);

        mockMvc.perform(formLogin("/login").user(username).password("cliente123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void showsHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Crear cuenta")))
                .andExpect(content().string(containsString("Ver cuentas")))
                .andExpect(content().string(containsString("Crear pago")))
                .andExpect(content().string(containsString("Crear reembolso")))
                .andExpect(content().string(containsString("Historial completo")))
                .andExpect(content().string(containsString("Disputas")))
                .andExpect(content().string(containsString("Reportes")))
                .andExpect(content().string(containsString("Inventario")))
                .andExpect(content().string(containsString("Consultar services creados")))
                .andExpect(content().string(containsString("Eventos")));

        mockMvc.perform(get("/accounts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Numero")))
                .andExpect(content().string(containsString("Correo")))
                .andExpect(content().string(containsString("Saldo inicial COP")))
                .andExpect(content().string(containsString("Billinfo")))
                .andExpect(content().string(containsString("DOM")))
                .andExpect(content().string(containsString("Ciclo de facturacion")));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ver cuentas")))
                .andExpect(content().string(containsString("Buscar cuenta")))
                .andExpect(content().string(containsString("Filtrar cuentas")))
                .andExpect(content().string(containsString("Ciclo facturacion")))
                .andExpect(content().string(containsString("Ver detalle")))
                .andExpect(content().string(containsString("target=\"_blank\"")));
    }

    @Test
    void regularUserCannotOpenUserManager() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanOpenUserManager() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestor de usuarios")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Crear usuario")))
                .andExpect(content().string(containsString("Rol")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin")));
    }

    @Test
    void showsPaymentAndRefundPages() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Crear pago")))
                .andExpect(content().string(containsString("Volver a la principal")));

        mockMvc.perform(get("/refunds"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Crear reembolso")))
                .andExpect(content().string(containsString("El reembolso se realiza por la totalidad del pago seleccionado")))
                .andExpect(content().string(containsString("Volver a la principal")));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Historial de transacciones")))
                .andExpect(content().string(containsString("Exportar CSV")));

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reportes")))
                .andExpect(content().string(containsString("Transacciones")))
                .andExpect(content().string(containsString("Estado disputa")));

        mockMvc.perform(get("/disputes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Crear disputa")))
                .andExpect(content().string(containsString("Settlement")))
                .andExpect(content().string(containsString("Aprobar")))
                .andExpect(content().string(containsString("Rechazar")));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Eventos del sistema")))
                .andExpect(content().string(containsString("Tipo de evento")));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Consultar productos creados")))
                .andExpect(content().string(containsString("Codigo o nombre")))
                .andExpect(content().string(containsString("Crear producto")))
                .andExpect(content().string(containsString("Guardar producto")))
                .andExpect(content().string(containsString("Editar")))
                .andExpect(content().string(containsString("Product ID")))
                .andExpect(content().string(containsString("Product Catalog")));

        mockMvc.perform(get("/bills"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bills generados")))
                .andExpect(content().string(containsString("Bill Number")))
                .andExpect(content().string(containsString("Periodo")));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Consultar services creados")))
                .andExpect(content().string(containsString("Tipo de servicio")))
                .andExpect(content().string(containsString("Guardar servicio")))
                .andExpect(content().string(containsString("Producto activo")))
                .andExpect(content().string(containsString("Asociar producto")))
                .andExpect(content().string(containsString("PVT alta")))
                .andExpect(content().string(containsString("PVT compra")))
                .andExpect(content().string(containsString("Fecha real")))
                .andExpect(content().string(containsString("Services creados")));

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ID de inventario")))
                .andExpect(content().string(containsString("Precio por unidad")))
                .andExpect(content().string(containsString("Cantidad en existencias")))
                .andExpect(content().string(containsString("Valor de inventario")))
                .andExpect(content().string(containsString("Nivel del nuevo pedido")))
                .andExpect(content().string(containsString("Aun se encuentra disponible")));
    }

    @Test
    void createsInventoryItemAndTracksAvailability() throws Exception {
        var response = mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Router BRMC",
                                  "description": "Equipo para servicio de internet",
                                  "unitPrice": 150000.00,
                                  "stockQuantity": 5,
                                  "reorderLevel": 5,
                                  "reorderTimeDays": 7,
                                  "reorderQuantity": 20,
                                  "available": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(matchesPattern("INV-\\d{19}")))
                .andExpect(jsonPath("$.name").value("Router BRMC"))
                .andExpect(jsonPath("$.unitPrice").value(150000.00))
                .andExpect(jsonPath("$.stockQuantity").value(5))
                .andExpect(jsonPath("$.inventoryValue").value(750000.00))
                .andExpect(jsonPath("$.reorderLevel").value(5))
                .andExpect(jsonPath("$.reorderTimeDays").value(7))
                .andExpect(jsonPath("$.reorderQuantity").value(20))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.needsReorder").value(true))
                .andExpect(jsonPath("$.pinVirtualTimeT").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        var inventoryId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/inventory").param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + inventoryId + "')]", hasSize(1)));

        mockMvc.perform(post("/api/inventory/{inventoryId}/unavailable", inventoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(get("/api/inventory").param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + inventoryId + "')]", hasSize(1)));

        mockMvc.perform(get("/api/events").param("type", "INVENTORY_ITEM_CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entityId == '" + inventoryId + "')]", hasSize(1)));

        mockMvc.perform(get("/api/events").param("type", "INVENTORY_AVAILABILITY_CHANGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entityId == '" + inventoryId + "')]", hasSize(1)));

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Inventario invalido",
                                  "unitPrice": -1.00,
                                  "stockQuantity": 1,
                                  "reorderLevel": 1,
                                  "reorderTimeDays": 1,
                                  "reorderQuantity": 1,
                                  "available": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void keepsProductsAndInventoryScopedByAccount() throws Exception {
        var firstAccountId = createAccount("Cliente Scope Uno", "10000.00");
        var secondAccountId = createAccount("Cliente Scope Dos", "10000.00");

        var firstProductResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "code": "PLAN_SCOPE_TEST",
                                  "name": "Plan Scope Test Uno",
                                  "description": "Producto por cuenta",
                                  "productType": "RECURRING",
                                  "price": 10000.00,
                                  "currency": "COP",
                                  "billingFrequency": "MONTHLY",
                                  "status": "ACTIVE"
                                }
                                """.formatted(firstAccountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(firstAccountId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var firstProductId = objectMapper.readTree(firstProductResponse).get("id").asText();

        var secondProductResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "code": "PLAN_SCOPE_TEST",
                                  "name": "Plan Scope Test Dos",
                                  "description": "Producto por cuenta",
                                  "productType": "RECURRING",
                                  "price": 12000.00,
                                  "currency": "COP",
                                  "billingFrequency": "MONTHLY",
                                  "status": "ACTIVE"
                                }
                                """.formatted(secondAccountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(secondAccountId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var secondProductId = objectMapper.readTree(secondProductResponse).get("id").asText();

        mockMvc.perform(get("/api/products").param("accountId", firstAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + firstProductId + "')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.id == '" + secondProductId + "')]", hasSize(0)));

        var secondServiceId = createService(secondAccountId, "MOBILE", "MOVIL-SCOPE-2");
        mockMvc.perform(post("/api/services/{serviceId}/products/{productId}", secondServiceId, firstProductId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Regla de negocio"));

        var firstInventoryResponse = mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "name": "SIM Scope Uno",
                                  "description": "Inventario por cuenta",
                                  "unitPrice": 5000.00,
                                  "stockQuantity": 10,
                                  "reorderLevel": 2,
                                  "reorderTimeDays": 5,
                                  "reorderQuantity": 10,
                                  "available": true
                                }
                                """.formatted(firstAccountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(firstAccountId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var firstInventoryId = objectMapper.readTree(firstInventoryResponse).get("id").asText();

        var secondInventoryResponse = mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "name": "SIM Scope Dos",
                                  "description": "Inventario por cuenta",
                                  "unitPrice": 5000.00,
                                  "stockQuantity": 10,
                                  "reorderLevel": 2,
                                  "reorderTimeDays": 5,
                                  "reorderQuantity": 10,
                                  "available": true
                                }
                                """.formatted(secondAccountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(secondAccountId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var secondInventoryId = objectMapper.readTree(secondInventoryResponse).get("id").asText();

        mockMvc.perform(get("/api/inventory").param("accountId", firstAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + firstInventoryId + "')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.id == '" + secondInventoryId + "')]", hasSize(0)));
    }

    @Test
    void createsAccountReceivesPaymentAndSendsRefund() throws Exception {
        var accountId = createAccount("Cliente BRMC", "10.00");

        var paymentResponse = mockMvc.perform(post("/api/accounts/{accountId}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 25.50,
                                  "paymentMethod": "ELECTRONIC_TRANSFER",
                                  "description": "Pago recibido"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.paymentMethod").value("ELECTRONIC_TRANSFER"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var paymentId = objectMapper.readTree(paymentResponse).get("id").asText();

        mockMvc.perform(post("/api/accounts/{accountId}/refunds", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "%s",
                                  "description": "Reembolso enviado"
                                }
                                """.formatted(paymentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("REFUND"))
                .andExpect(jsonPath("$.amount").value(25.50));

        mockMvc.perform(post("/api/accounts/{accountId}/refunds", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "%s",
                                  "description": "Reembolso duplicado"
                                }
                                """.formatted(paymentId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Reembolso no permitido"));

        mockMvc.perform(post("/api/accounts/{accountId}/write-offs", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 2.00,
                                  "description": "Ajuste write-off"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WRITE_OFF"))
                .andExpect(jsonPath("$.amount").value(2.00));

        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(8.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Detalle de cuenta")))
                .andExpect(content().string(containsString("Pagar")))
                .andExpect(content().string(containsString("Reembolsar")))
                .andExpect(content().string(containsString("Cerrar cuenta")))
                .andExpect(content().string(containsString("Current cycle")))
                .andExpect(content().string(containsString("Ciclo de facturacion actual")))
                .andExpect(content().string(containsString("Proxima fecha de billing")))
                .andExpect(content().string(containsString("Tipo de servicio")))
                .andExpect(content().string(containsString("Producto activo")))
                .andExpect(content().string(containsString("Asociar producto")))
                .andExpect(content().string(containsString("PVT compra")))
                .andExpect(content().string(containsString("PVT apertura")))
                .andExpect(content().string(containsString("PVT negocio")))
                .andExpect(content().string(containsString("Fecha real")))
                .andExpect(content().string(containsString("Disputas")))
                .andExpect(content().string(containsString("Settlement")))
                .andExpect(content().string(containsString("Aprobar")))
                .andExpect(content().string(containsString("Rechazar")));

        mockMvc.perform(get("/api/accounts/{accountId}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/accounts/{accountId}/payments", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("PAYMENT"));

        mockMvc.perform(get("/api/accounts/{accountId}/payments/unallocated", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].allocationStatus").value("UNALLOCATED"))
                .andExpect(jsonPath("$[0].unallocatedAmount").value(25.50));

        mockMvc.perform(get("/api/payments/unallocated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + paymentId + "')]", hasSize(1)));

        mockMvc.perform(get("/api/accounts/{accountId}/refunds", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("REFUND"));

        mockMvc.perform(get("/api/accounts/{accountId}/write-offs", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("WRITE_OFF"));

        mockMvc.perform(get("/api/transactions")
                        .param("accountId", accountId)
                        .param("type", "PAYMENT")
                        .param("minAmount", "20.00")
                        .param("maxAmount", "30.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountId").value(accountId))
                .andExpect(jsonPath("$[0].type").value("PAYMENT"));

        mockMvc.perform(get("/api/transactions/export")
                        .param("accountId", accountId)
                        .param("type", "REFUND"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accountId,ownerName,transactionId,type,amount,currency,originalAmount,originalCurrency,exchangeRate,paymentMethod,description,createdAt")))
                .andExpect(content().string(containsString("REFUND")));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + accountId + "')]", hasSize(1)));

        assertThat(paymentRecordRepository.findByAccountIdOrderByCreatedAtAsc(accountId)).hasSize(1);
        assertThat(refundRecordRepository.findByAccountIdOrderByCreatedAtAsc(accountId)).hasSize(1);
        assertThat(billInfoRepository.findByAccountId(accountId)).isPresent();
    }

    @Test
    void rejectsFullRefundWhenBalanceIsInsufficient() throws Exception {
        var accountId = createAccount("Cliente sin saldo", "0.00");
        var paymentId = createPayment(accountId, "10.00");

        mockMvc.perform(post("/api/accounts/{accountId}/write-offs", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 9.00,
                                  "description": "Ajuste antes del reembolso"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/accounts/{accountId}/refunds", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "%s",
                                  "description": "Reembolso total"
                                }
                                """.formatted(paymentId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Saldo insuficiente"));
    }

    @Test
    void closesAccountAndRejectsNewMovements() throws Exception {
        var accountId = createAccount("Cliente para cierre", "20.00");
        var paymentId = createPayment(accountId, "5.00");

        mockMvc.perform(post("/api/accounts/{accountId}/close", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(post("/api/accounts/{accountId}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00,
                                  "description": "Pago rechazado"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Cuenta cerrada"));

        mockMvc.perform(post("/api/accounts/{accountId}/refunds", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "%s",
                                  "description": "Reembolso rechazado"
                                }
                                """.formatted(paymentId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Cuenta cerrada"));
    }

    @Test
    void receivesUsdPaymentUsingTrmAndKeepsAccountInCop() throws Exception {
        var accountId = createAccount("Cliente USD", "0.00");

        mockMvc.perform(get("/api/exchange-rates/usd-cop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("COP"))
                .andExpect(jsonPath("$.rate").value(4000.00));

        mockMvc.perform(post("/api/accounts/{accountId}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 2.00,
                                  "currency": "USD",
                                  "paymentMethod": "CREDIT_CARD",
                                  "description": "Pago en dolares"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.amount").value(8000.00))
                .andExpect(jsonPath("$.currency").value("COP"))
                .andExpect(jsonPath("$.originalAmount").value(2.00))
                .andExpect(jsonPath("$.originalCurrency").value("USD"))
                .andExpect(jsonPath("$.exchangeRate").value(4000.00))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));

        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(8000.00))
                .andExpect(jsonPath("$.currency").value("COP"));

        var payments = paymentRecordRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).amount()).isEqualByComparingTo("8000.00");
        assertThat(payments.get(0).originalCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void simulatesBrmProductsServicesVirtualTimeAndBilling() throws Exception {
        var accountId = createAccount("Cliente BRM", "100000.00");

        mockMvc.perform(post("/api/virtual-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentVirtualTime": "2026-01-15T10:00:00",
                                  "updatedBy": "test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVirtualTime").value("2026-01-15T10:00:00"));

        var recurringProductId = createProduct(
                "PLAN_MOVIL_10GB",
                "Plan Movil 10GB",
                "RECURRING",
                "50000.00",
                "MONTHLY",
                "ACTIVE"
        );
        var oneTimeProductId = createProduct(
                "ACTIVACION_SIM",
                "Activacion SIM",
                "ONE_TIME",
                "10000.00",
                "NONE",
                "ACTIVE"
        );
        var inactiveProductId = createProduct(
                "PLAN_INACTIVO",
                "Plan Inactivo",
                "RECURRING",
                "1000.00",
                "MONTHLY",
                "INACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "PRECIO_NEGATIVO",
                                  "name": "Precio negativo",
                                  "productType": "ONE_TIME",
                                  "price": -1.00,
                                  "currency": "COP",
                                  "billingFrequency": "NONE",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isBadRequest());

        var serviceId = createService(accountId, "MOBILE", "MOVIL-TEST-1");

        mockMvc.perform(get("/api/services")
                        .param("accountId", accountId)
                        .param("serviceType", "MOBILE")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(serviceId))
                .andExpect(jsonPath("$[0].serviceCode").value("MOVIL-TEST-1"))
                .andExpect(jsonPath("$[0].serviceType").value("MOBILE"));

        mockMvc.perform(post("/api/services/{serviceId}/products/{productId}", serviceId, recurringProductId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("PLAN_MOVIL_10GB"))
                .andExpect(jsonPath("$.nextBillAt").value("2026-01-15T10:00:00"));

        mockMvc.perform(post("/api/services/{serviceId}/products/{productId}", serviceId, recurringProductId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Regla de negocio"));

        mockMvc.perform(post("/api/services/{serviceId}/products/{productId}", serviceId, inactiveProductId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Regla de negocio"));

        mockMvc.perform(post("/api/services/{serviceId}/products/{productId}", serviceId, oneTimeProductId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("ACTIVACION_SIM"));

        mockMvc.perform(post("/api/billing/accounts/{accountId}/run", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.chargesCreated").value(2))
                .andExpect(jsonPath("$.totalAmount").value(60000.00))
                .andExpect(jsonPath("$.charges", hasSize(2)))
                .andExpect(jsonPath("$.charges[0].billNo").exists())
                .andExpect(jsonPath("$.charges[0].billNo").value(matchesPattern("B1-\\d+")))
                .andExpect(jsonPath("$.charges[0].billingDom").value(10))
                .andExpect(jsonPath("$.charges[0].billingCycle").value("MONTHLY"))
                .andExpect(jsonPath("$.charges[0].billingPeriodLabel").value("2026-01 a 2026-02"))
                .andExpect(jsonPath("$.charges[0].billPeriodStart").exists())
                .andExpect(jsonPath("$.charges[0].billPeriodEnd").exists());

        var invoicesResponse = mockMvc.perform(get("/api/accounts/{accountId}/invoices", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ISSUED"))
                .andExpect(jsonPath("$[0].subtotal").value(60000.00))
                .andExpect(jsonPath("$[0].taxAmount").value(0))
                .andExpect(jsonPath("$[0].totalAmount").value(60000.00))
                .andExpect(jsonPath("$[0].amountDue").value(60000.00))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var invoiceId = objectMapper.readTree(invoicesResponse).get(0).get("id").asText();
        var invoiceNumber = objectMapper.readTree(invoicesResponse).get(0).get("invoiceNumber").asText();
        assertThat(invoiceNumber).matches("INV-\\d+");

        mockMvc.perform(get("/api/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value(invoiceNumber))
                .andExpect(jsonPath("$.lines", hasSize(2)));

        mockMvc.perform(get("/api/invoices/number/{invoiceNumber}", invoiceNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId));

        mockMvc.perform(get("/api/invoices/{invoiceId}/lines", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].taxAmount").value(0));

        mockMvc.perform(get("/api/accounts/{accountId}/billinfo", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        var billsResponse = mockMvc.perform(get("/api/accounts/{accountId}/bills", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].totalAmount").value(60000.00))
                .andExpect(jsonPath("$[0].billNo").value(matchesPattern("B1-\\d+")))
                .andExpect(jsonPath("$[0].billingDom").value(10))
                .andExpect(jsonPath("$[0].billingCycle").value("MONTHLY"))
                .andExpect(jsonPath("$[0].billingPeriodLabel").value("2026-01 a 2026-02"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var billId = objectMapper.readTree(billsResponse).get(0).get("id").asText();

        mockMvc.perform(get("/api/bills/{billId}/items", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        mockMvc.perform(get("/api/bills/{billId}", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billNo").value(matchesPattern("B1-\\d+")))
                .andExpect(jsonPath("$.periodStart").exists())
                .andExpect(jsonPath("$.periodEnd").exists())
                .andExpect(jsonPath("$.items", hasSize(2)));

        mockMvc.perform(get("/bills/{billId}", billId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bill Number")))
                .andExpect(content().string(containsString("Services & Products")))
                .andExpect(content().string(containsString("Write-Offs")));

        mockMvc.perform(get("/api/bills/{billId}/csv", billId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bill_number,account_number,period_start,period_end,item_type")));

        mockMvc.perform(post("/api/billing/accounts/{accountId}/run", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.chargesCreated").value(0));

        mockMvc.perform(get("/api/accounts/{accountId}/invoices", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/accounts/{accountId}/billing-charges", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/accounts/{accountId}/items", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/api/services/{serviceId}/suspend", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        mockMvc.perform(post("/api/virtual-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentVirtualTime": "2026-02-15T10:00:00",
                                  "updatedBy": "test"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/billing/accounts/{accountId}/run", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chargesCreated").value(0));

        mockMvc.perform(post("/api/services/{serviceId}/reactivate", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/billing/accounts/{accountId}/run", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chargesCreated").value(1))
                .andExpect(jsonPath("$.charges[0].chargeType").value("RECURRING"))
                .andExpect(jsonPath("$.charges[0].billingCycle").value("MONTHLY"))
                .andExpect(jsonPath("$.charges[0].billingPeriodLabel").value("2026-02 a 2026-03"));

        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-10000.00));

        mockMvc.perform(post("/api/invoices/{invoiceId}/sent", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        mockMvc.perform(post("/api/invoices/{invoiceId}/payment", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 30000.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.amountPaid").value(30000.00))
                .andExpect(jsonPath("$.amountDue").value(30000.00));

        mockMvc.perform(post("/api/invoices/{invoiceId}/payment", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 30000.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.amountDue").value(0));

        mockMvc.perform(post("/api/invoices/{invoiceId}/cancel", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Intento de cancelar pagada"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Regla de negocio"));

        mockMvc.perform(get("/api/invoices/{invoiceId}/csv", invoiceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("invoiceNumber,status,accountId")))
                .andExpect(content().string(containsString(invoiceNumber)));

        mockMvc.perform(get("/api/invoices/{invoiceId}/html", invoiceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(invoiceNumber)))
                .andExpect(content().string(containsString("BRMC Billing Care")));

        var closedAccountId = createAccount("Cuenta cerrada BRM", "0.00");
        mockMvc.perform(post("/api/accounts/{accountId}/close", closedAccountId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/accounts/{accountId}/services", closedAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceType": "MOBILE",
                                  "serviceCode": "CLOSED-SERVICE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Cuenta cerrada"));

        mockMvc.perform(post("/api/billing/accounts/{accountId}/run", closedAccountId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Cuenta cerrada"));

        mockMvc.perform(get("/api/events").param("accountId", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type == 'SERVICE_CREATED')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'SERVICE_PRODUCT_ASSIGNED')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.type == 'BILLING_CHARGE_CREATED')]", hasSize(3)))
                .andExpect(jsonPath("$[?(@.type == 'BILL_CREATED')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.type == 'ITEM_CREATED')]", hasSize(3)))
                .andExpect(jsonPath("$[?(@.type == 'BILL_ITEM_CREATED')]", hasSize(3)))
                .andExpect(jsonPath("$[?(@.type == 'INVOICE_GENERATED')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.type == 'INVOICE_LINE_CREATED')]", hasSize(3)))
                .andExpect(jsonPath("$[?(@.type == 'INVOICE_SENT')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'INVOICE_PAID')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'INVOICE_PARTIALLY_PAID')]", hasSize(1)));

        assertThat(billRepository.findByAccountIdOrderByBillDateDesc(accountId)).hasSize(2);
        assertThat(billItemRepository.findByAccountIdOrderByItemDateAsc(accountId)).hasSize(3);
        assertThat(invoiceRepository.findByAccountIdOrderByIssueDateDesc(accountId)).hasSize(2);
        assertThat(invoiceLineRepository.findByInvoiceIdOrderByCreatedAtAsc(invoiceId)).hasSize(2);
    }

    @Test
    void updatesProductAndServiceFromApi() throws Exception {
        var accountId = createAccount("Cliente Edicion", "0.00");
        var productId = createProduct(
                "PLAN_EDITABLE",
                "Plan Editable",
                "RECURRING",
                "30000.00",
                "MONTHLY",
                "ACTIVE"
        );
        var serviceId = createService(accountId, "MOBILE", "SERVICIO-EDITABLE");

        mockMvc.perform(put("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "PLAN_EDITADO",
                                  "name": "Plan Editado",
                                  "description": "Producto editado",
                                  "productType": "ONE_TIME",
                                  "price": 25000.00,
                                  "currency": "COP",
                                  "billingFrequency": "NONE",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PLAN_EDITADO"))
                .andExpect(jsonPath("$.name").value("Plan Editado"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.updatedPinVirtualTimeT").exists());

        mockMvc.perform(put("/api/services/{serviceId}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceType": "INTERNET",
                                  "serviceCode": "SERVICIO-EDITADO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceCode").value("SERVICIO-EDITADO"))
                .andExpect(jsonPath("$.serviceType").value("INTERNET"))
                .andExpect(jsonPath("$.updatedAt").exists());

        mockMvc.perform(get("/api/events").param("type", "PRODUCT_UPDATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entityId == '" + productId + "')]", hasSize(1)));

        mockMvc.perform(get("/api/events").param("type", "SERVICE_UPDATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entityId == '" + serviceId + "')]", hasSize(1)));
    }

    @Test
    void createsAndCancelsCreditNoteForInvoice() throws Exception {
        var accountId = createAccount("Cliente Nota Credito", "0.00");
        var productId = createProduct(
                "AJUSTE_CN_TEST",
                "Producto Nota Credito",
                "ONE_TIME",
                "50000.00",
                "NONE",
                "ACTIVE"
        );
        var serviceId = createService(accountId, "MOBILE", "SERVICIO-CN-TEST");

        mockMvc.perform(post("/api/services/{serviceId}/products/{productId}", serviceId, productId))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/billing/accounts/{accountId}/run", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chargesCreated").value(1));

        var invoicesResponse = mockMvc.perform(get("/api/accounts/{accountId}/invoices", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var invoiceId = objectMapper.readTree(invoicesResponse).get(0).get("id").asText();

        var creditNoteResponse = mockMvc.perform(post("/api/invoices/{invoiceId}/credit-notes", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 15000.00,
                                  "reason": "Ajuste por cobro comercial",
                                  "description": "Credito parcial"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creditNoteNumber").value(matchesPattern("CN-\\d+")))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.totalAmount").value(15000.00))
                .andExpect(jsonPath("$.lines", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var creditNoteId = objectMapper.readTree(creditNoteResponse).get("id").asText();

        mockMvc.perform(get("/api/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_CREDITED"))
                .andExpect(jsonPath("$.creditAmount").value(15000.00))
                .andExpect(jsonPath("$.amountDue").value(35000.00));

        mockMvc.perform(get("/api/invoices/{invoiceId}/credit-notes", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(creditNoteId));

        mockMvc.perform(post("/api/invoices/{invoiceId}/credit-notes", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 90000.00,
                                  "reason": "Exceso"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Regla de negocio"));

        mockMvc.perform(post("/api/credit-notes/{creditNoteId}/cancel", creditNoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Reversa de prueba"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/invoices/{invoiceId}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.creditAmount").value(0))
                .andExpect(jsonPath("$.amountDue").value(50000.00));

        mockMvc.perform(get("/api/events").param("type", "CREDIT_NOTE_APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entityId == '" + invoiceId + "')]", hasSize(1)));

        assertThat(creditNoteRepository.findByInvoiceIdOrderByIssueDateDesc(invoiceId)).hasSize(1);
    }

    @Test
    void createsApprovesRejectsDisputesAndRegistersEvents() throws Exception {
        var accountId = createAccount("Cliente con disputa", "80.00");

        var settledDisputeId = createDispute(accountId, "15.00", "Cobro no reconocido");

        mockMvc.perform(post("/api/disputes/{disputeId}/settlements", settledDisputeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 12.50,
                                  "note": "Acuerdo parcial con el cliente"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.disputeId").value(settledDisputeId))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(12.50))
                .andExpect(jsonPath("$.currency").value("COP"))
                .andExpect(jsonPath("$.note").value("Acuerdo parcial con el cliente"));

        mockMvc.perform(get("/api/disputes/{disputeId}/settlements", settledDisputeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].disputeId").value(settledDisputeId));

        mockMvc.perform(get("/api/disputes").param("accountId", accountId).param("status", "SETTLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(settledDisputeId))
                .andExpect(jsonPath("$[0].status").value("SETTLED"))
                .andExpect(jsonPath("$[0].resolutionNote").value("Acuerdo parcial con el cliente"));

        mockMvc.perform(post("/api/disputes/{disputeId}/approve", settledDisputeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resolutionNote": "Se aprueba por evidencia del cliente"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Disputa ya resuelta"));

        var approvedDisputeId = createDispute(accountId, "10.00", "Evidencia aceptada");

        mockMvc.perform(post("/api/disputes/{disputeId}/approve", approvedDisputeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resolutionNote": "Se aprueba por evidencia del cliente"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Se aprueba por evidencia del cliente"));

        mockMvc.perform(post("/api/disputes/{disputeId}/reject", approvedDisputeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resolutionNote": "Intento duplicado"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Disputa ya resuelta"));

        var rejectedDisputeId = createDispute(accountId, "8.00", "Solicitud duplicada");

        mockMvc.perform(post("/api/disputes/{disputeId}/reject", rejectedDisputeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resolutionNote": "No aplica"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(get("/api/disputes")
                        .param("accountId", accountId)
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(approvedDisputeId));

        mockMvc.perform(get("/api/events").param("accountId", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type == 'ACCOUNT_CREATED')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'DISPUTE_CREATED')]", hasSize(3)))
                .andExpect(jsonPath("$[?(@.type == 'DISPUTE_SETTLEMENT_CREATED')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'DISPUTE_SETTLED')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'DISPUTE_APPROVED')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.type == 'DISPUTE_REJECTED')]", hasSize(1)));
    }

    private String createAccount(String ownerName, String initialBalance) throws Exception {
        var response = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerName": "%s",
                                  "phoneNumber": "3001234567",
                                  "email": "cliente@brmc.com",
                                  "initialBalance": %s,
                                  "billingDom": 10,
                                  "billingCycle": "MONTHLY"
                                }
                                """.formatted(ownerName, initialBalance)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        mockMvc.perform(get("/api/accounts/{accountId}", json.get("id").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchesPattern("\\d{14}")))
                .andExpect(jsonPath("$.phoneNumber").value("3001234567"))
                .andExpect(jsonPath("$.email").value("cliente@brmc.com"))
                .andExpect(jsonPath("$.billingDom").value(10))
                .andExpect(jsonPath("$.billingCycle").value("MONTHLY"));
        return json.get("id").asText();
    }

    private String createDispute(String accountId, String amount, String reason) throws Exception {
        var response = mockMvc.perform(post("/api/disputes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "amount": %s,
                                  "reason": "%s"
                                }
                                """.formatted(accountId, amount, reason)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asText();
    }

    private String createPayment(String accountId, String amount) throws Exception {
        var response = mockMvc.perform(post("/api/accounts/{accountId}/payments", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": %s,
                                  "paymentMethod": "CASH",
                                  "description": "Pago base"
                                }
                                """.formatted(amount)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createProduct(
            String code,
            String name,
            String productType,
            String price,
            String billingFrequency,
            String productStatus
    ) throws Exception {
        var response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "%s",
                                  "name": "%s",
                                  "description": "Producto de prueba",
                                  "productType": "%s",
                                  "price": %s,
                                  "currency": "COP",
                                  "billingFrequency": "%s",
                                  "status": "%s"
                                }
                """.formatted(code, name, productType, price, billingFrequency, productStatus)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.displayId").value(matchesPattern("PRD-\\d+")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createService(String accountId, String serviceType, String serviceCode) throws Exception {
        var response = mockMvc.perform(post("/api/accounts/{accountId}/services", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceType": "%s",
                                  "serviceCode": "%s"
                                }
                                """.formatted(serviceType, serviceCode)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.serviceType").value(serviceType))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}
