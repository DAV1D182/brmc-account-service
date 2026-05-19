package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de cuentas.
 *
 * <p>Expone operaciones HTTP para crear, consultar y cerrar cuentas, ademas de registrar pagos,
 * reembolsos y write-offs. La validacion estructural se aplica con Jakarta Validation y las reglas
 * de negocio se delegan a {@link AccountService}.</p>
 */
@RestController
@RequestMapping("/api/accounts")
class AccountController {

    private final AccountService accountService;

    /**
     * Crea el controlador de cuentas.
     *
     * @param accountService servicio de aplicacion de cuentas.
     */
    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Lista las cuentas registradas.
     *
     * @return cuentas en formato de respuesta API.
     */
    @GetMapping
    List<AccountResponse> getAccounts() {
        return accountService.getAccounts().stream()
                .map(AccountResponse::from)
                .toList();
    }

    /**
     * Crea una cuenta con estado ACTIVE.
     *
     * @param request datos validados de creacion.
     * @return cuenta creada.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(accountService.createAccount(
                request.ownerName(),
                request.phoneNumber(),
                request.email(),
                request.initialBalance(),
                request.billingDom(),
                request.billingCycle()
        ));
    }

    /**
     * Consulta una cuenta por id.
     *
     * @param accountId identificador de cuenta.
     * @return cuenta encontrada.
     * @throws AccountNotFoundException si no existe.
     */
    @GetMapping("/{accountId}")
    AccountResponse getAccount(@PathVariable String accountId) {
        return AccountResponse.from(accountService.getAccount(accountId));
    }

    /**
     * Cierra logicamente una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return cuenta con estado CLOSED.
     * @throws AccountNotFoundException si no existe.
     */
    @PostMapping("/{accountId}/close")
    AccountResponse closeAccount(@PathVariable String accountId) {
        return AccountResponse.from(accountService.closeAccount(accountId));
    }

    /**
     * Registra un pago recibido para una cuenta.
     *
     * @param accountId cuenta que recibe el pago.
     * @param request monto, moneda, metodo y descripcion.
     * @return transaccion PAYMENT creada.
     */
    @PostMapping("/{accountId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse receivePayment(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyMovementRequest request
    ) {
        return TransactionResponse.from(accountService.receivePayment(
                accountId,
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.description()
        ));
    }

    /**
     * Registra el reembolso total de un pago.
     *
     * @param accountId cuenta que emite el reembolso.
     * @param request pago origen y descripcion.
     * @return transaccion REFUND creada.
     */
    @PostMapping("/{accountId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse sendRefund(
            @PathVariable String accountId,
            @Valid @RequestBody RefundRequest request
    ) {
        return TransactionResponse.from(accountService.sendRefund(
                accountId,
                request.paymentId(),
                request.description()
        ));
    }

    /**
     * Aplica un write-off a una cuenta.
     *
     * @param accountId cuenta ajustada.
     * @param request monto COP y descripcion.
     * @return transaccion WRITE_OFF creada.
     */
    @PostMapping("/{accountId}/write-offs")
    @ResponseStatus(HttpStatus.CREATED)
    TransactionResponse writeOff(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyMovementRequest request
    ) {
        return TransactionResponse.from(accountService.writeOff(accountId, request.amount(), request.description()));
    }

    /**
     * Consulta todo el historial financiero de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return transacciones ordenadas por fecha.
     */
    @GetMapping("/{accountId}/transactions")
    List<TransactionResponse> getTransactions(@PathVariable String accountId) {
        return accountService.getTransactions(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * Consulta pagos de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return pagos registrados.
     */
    @GetMapping("/{accountId}/payments")
    List<TransactionResponse> getPayments(@PathVariable String accountId) {
        return accountService.getPayments(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * Consulta pagos recibidos que aun no han sido asignados a bills o items.
     *
     * @param accountId cuenta consultada.
     * @return pagos UNALLOCATED de la cuenta.
     */
    @GetMapping("/{accountId}/payments/unallocated")
    List<PaymentRecordResponse> getUnallocatedPayments(@PathVariable String accountId) {
        return accountService.getUnallocatedPayments(accountId).stream()
                .map(PaymentRecordResponse::from)
                .toList();
    }

    /**
     * Consulta reembolsos de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return reembolsos registrados.
     */
    @GetMapping("/{accountId}/refunds")
    List<TransactionResponse> getRefunds(@PathVariable String accountId) {
        return accountService.getRefunds(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * Consulta write-offs de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return write-offs registrados.
     */
    @GetMapping("/{accountId}/write-offs")
    List<TransactionResponse> getWriteOffs(@PathVariable String accountId) {
        return accountService.getWriteOffs(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * Solicitud para crear una cuenta.
     *
     * @param ownerName titular obligatorio.
     * @param phoneNumber numero obligatorio de contacto.
     * @param email correo obligatorio con formato valido.
     * @param initialBalance saldo inicial no negativo.
     * @param billingDom dia del mes para facturacion.
     * @param billingCycle ciclo inicial de facturacion.
     */
    record CreateAccountRequest(
            @NotBlank(message = "ownerName es obligatorio")
            @Size(max = 120, message = "ownerName no puede superar 120 caracteres")
            String ownerName,

            @NotBlank(message = "phoneNumber es obligatorio")
            @Size(max = 40, message = "phoneNumber no puede superar 40 caracteres")
            String phoneNumber,

            @NotBlank(message = "email es obligatorio")
            @Email(message = "email debe tener un formato valido")
            @Size(max = 160, message = "email no puede superar 160 caracteres")
            String email,

            @NotNull(message = "initialBalance es obligatorio")
            @DecimalMin(value = "0.00", message = "initialBalance no puede ser negativo")
            BigDecimal initialBalance,

            Integer billingDom,

            @Size(max = 20, message = "billingCycle no puede superar 20 caracteres")
            String billingCycle
    ) {
    }

    /**
     * Solicitud para movimientos con monto.
     *
     * @param amount monto positivo.
     * @param currency moneda opcional; COP por defecto.
     * @param paymentMethod metodo opcional; CASH por defecto en pagos.
     * @param paymentId campo legado no usado por write-off ni pagos.
     * @param description descripcion opcional.
     */
    record MoneyMovementRequest(
            @NotNull(message = "amount es obligatorio")
            @DecimalMin(value = "0.01", message = "amount debe ser mayor a cero")
            BigDecimal amount,

            Currency currency,

            PaymentMethod paymentMethod,

            String paymentId,

            @Size(max = 240, message = "description no puede superar 240 caracteres")
            String description
    ) {
    }

    /**
     * Solicitud para reembolsar un pago completo.
     *
     * @param paymentId identificador obligatorio del pago origen.
     * @param description descripcion opcional.
     */
    record RefundRequest(
            @NotBlank(message = "paymentId es obligatorio")
            String paymentId,

            @Size(max = 240, message = "description no puede superar 240 caracteres")
            String description
    ) {
    }
}
