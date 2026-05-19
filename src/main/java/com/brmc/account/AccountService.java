package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para cuentas y movimientos financieros.
 *
 * <p>Orquesta repositorios, reglas de dominio, conversion de moneda y auditoria. Sus metodos se
 * ejecutan dentro de una transaccion de Spring, por lo que cambios sobre cuenta, registros
 * especializados y eventos de sistema se confirman de forma atomica cuando la operacion termina
 * correctamente.</p>
 */
@Service
@Transactional
class AccountService {

    private static final DateTimeFormatter ACCOUNT_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RefundRecordRepository refundRecordRepository;
    private final WriteOffRecordRepository writeOffRecordRepository;
    private final DisputeRepository disputeRepository;
    private final DisputeSettlementRepository disputeSettlementRepository;
    private final BillInfoRepository billInfoRepository;
    private final SystemEventRepository eventRepository;
    private final ExchangeRateService exchangeRateService;
    private final VirtualTimeService virtualTimeService;
    private final UserContextService userContextService;

    /**
     * Crea el servicio con sus dependencias de persistencia, auditoria y TRM.
     *
     * @param accountRepository repositorio de cuentas.
     * @param transactionRepository repositorio de historial financiero.
     * @param paymentRecordRepository repositorio especializado de pagos.
     * @param refundRecordRepository repositorio especializado de reembolsos.
     * @param writeOffRecordRepository repositorio especializado de write-offs.
     * @param disputeRepository repositorio de disputas.
     * @param disputeSettlementRepository repositorio de settlements.
     * @param billInfoRepository repositorio de configuracion de facturacion.
     * @param eventRepository repositorio de eventos de sistema.
     * @param exchangeRateService servicio de conversion COP/USD.
     */
    AccountService(
            AccountRepository accountRepository,
            AccountTransactionRepository transactionRepository,
            PaymentRecordRepository paymentRecordRepository,
            RefundRecordRepository refundRecordRepository,
            WriteOffRecordRepository writeOffRecordRepository,
            DisputeRepository disputeRepository,
            DisputeSettlementRepository disputeSettlementRepository,
            BillInfoRepository billInfoRepository,
            SystemEventRepository eventRepository,
            ExchangeRateService exchangeRateService,
            VirtualTimeService virtualTimeService,
            UserContextService userContextService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.refundRecordRepository = refundRecordRepository;
        this.writeOffRecordRepository = writeOffRecordRepository;
        this.disputeRepository = disputeRepository;
        this.disputeSettlementRepository = disputeSettlementRepository;
        this.billInfoRepository = billInfoRepository;
        this.eventRepository = eventRepository;
        this.exchangeRateService = exchangeRateService;
        this.virtualTimeService = virtualTimeService;
        this.userContextService = userContextService;
    }

    /**
     * Crea una cuenta activa con identificador basado en fecha/hora.
     *
     * <p>Intenta generar un id unico durante una ventana de sesenta segundos y registra el evento
     * {@link EventType#ACCOUNT_CREATED} cuando la cuenta se guarda.</p>
     *
     * @param ownerName titular de la cuenta.
     * @param phoneNumber numero de contacto.
     * @param email correo de contacto.
     * @param initialBalance saldo inicial en COP.
     * @param billingDom dia del mes usado como DOM de facturacion.
     * @param billingCycle ciclo de facturacion inicial.
     * @return cuenta persistida.
     * @throws IllegalStateException si no puede generar un id unico.
     */
    Account createAccount(
            String ownerName,
            String phoneNumber,
            String email,
            BigDecimal initialBalance,
            Integer billingDom,
            String billingCycle
    ) {
        var pinVirtualTimeT = virtualTimeService.getCurrentVirtualTime();
        var normalizedBillingDom = normalizeBillingDom(billingDom, pinVirtualTimeT);
        var normalizedBillingCycle = normalizeBillingCycle(billingCycle);
        for (var secondsToAdd = 0; secondsToAdd < 60; secondsToAdd++) {
            var accountId = LocalDateTime.now().plusSeconds(secondsToAdd).format(ACCOUNT_ID_FORMAT);
            var account = new Account(
                    accountId,
                    ownerName,
                    phoneNumber,
                    email,
                    initialBalance,
                    normalizedBillingDom,
                    normalizedBillingCycle,
                    pinVirtualTimeT
            );
            account.assignOwnerUsername(userContextService.currentUsername());
            if (!accountRepository.existsById(account.id())) {
                var savedAccount = accountRepository.save(account);
                var billInfo = billInfoRepository.save(new BillInfo(savedAccount, pinVirtualTimeT));
                logEvent(
                        EventType.ACCOUNT_CREATED,
                        "ACCOUNT",
                        savedAccount.id(),
                        savedAccount.id(),
                        "Cuenta creada para " + savedAccount.ownerName() + "."
                );
                logEvent(
                        EventType.BILLINFO_CREATED,
                        "BILLINFO",
                        billInfo.id(),
                        savedAccount.id(),
                        "Billinfo creado con DOM " + billInfo.billingDom() + " y ciclo " + billInfo.billingCycle() + "."
                );
                return savedAccount;
            }
        }

        throw new IllegalStateException("No fue posible generar un id unico para la cuenta.");
    }

    /**
     * Consulta una cuenta por id.
     *
     * @param accountId identificador de cuenta.
     * @return cuenta encontrada.
     * @throws AccountNotFoundException si no existe la cuenta.
     */
    Account getAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + accountId + ".");
        }
        return account;
    }

    /**
     * Lista todas las cuentas ordenadas por fecha de creacion.
     *
     * @return cuentas existentes.
     */
    @Transactional(readOnly = true)
    List<Account> getAccounts() {
        var accounts = userContextService.isAdmin()
                ? accountRepository.findAll()
                : accountRepository.findByOwnerUsernameOrderByCreatedAtAsc(userContextService.currentUsername());
        return accounts.stream()
                .sorted(Comparator.comparing(Account::createdAt))
                .toList();
    }

    /**
     * Registra un pago sobre una cuenta activa.
     *
     * <p>Cuando la moneda es USD, convierte el monto a COP usando {@link ExchangeRateService}. La
     * operacion incrementa el saldo, crea el registro en {@code payments_t} y registra
     * {@link EventType#PAYMENT_RECEIVED}.</p>
     *
     * @param accountId cuenta que recibe el pago.
     * @param amount monto informado por el usuario.
     * @param currency moneda del monto; COP si es nula.
     * @param paymentMethod metodo de pago; CASH si es nulo.
     * @param description descripcion opcional.
     * @return transaccion PAYMENT creada.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     */
    AccountTransaction receivePayment(
            String accountId,
            BigDecimal amount,
            Currency currency,
            PaymentMethod paymentMethod,
            String description
    ) {
        var account = getAccount(accountId);
        var paymentCurrency = currency == null ? Currency.COP : currency;
        var method = paymentMethod == null ? PaymentMethod.CASH : paymentMethod;
        var exchangeRate = exchangeRateService.rateFor(paymentCurrency);
        var amountInCop = exchangeRateService.convertToCop(amount, paymentCurrency);
        var transaction = account.receivePayment(
                amountInCop,
                amount,
                paymentCurrency,
                exchangeRate,
                description,
                method,
                virtualTimeService.getCurrentVirtualTime()
        );
        accountRepository.save(account);
        var paymentRecord = paymentRecordRepository.save(new PaymentRecord(account, transaction));
        logEvent(
                EventType.PAYMENT_RECEIVED,
                "TRANSACTION",
                transaction.id(),
                account.id(),
                "Pago registrado por " + transaction.amount() + " COP."
        );
        logEvent(
                EventType.UNALLOCATED_PAYMENT_CREATED,
                "PAYMENT",
                paymentRecord.id(),
                account.id(),
                "Pago no asignado disponible por " + paymentRecord.unallocatedAmount() + " COP."
        );
        return transaction;
    }

    /**
     * Emite el reembolso total de un pago existente.
     *
     * <p>El monto no se recibe por parametro: se toma del pago origen. La operacion valida cuenta
     * activa, pago perteneciente a la cuenta y que el pago no haya sido reembolsado previamente.
     * Reduce el saldo, crea registro en {@code refunds_t} y emite {@link EventType#REFUND_SENT}.</p>
     *
     * @param accountId cuenta que emite el reembolso.
     * @param paymentId pago origen.
     * @param description descripcion opcional.
     * @return transaccion REFUND creada.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws PaymentNotFoundException si no se informa o no existe el pago para la cuenta.
     * @throws RefundAmountExceededException si el pago ya fue reembolsado.
     * @throws InsufficientBalanceException si el saldo no cubre el reembolso.
     */
    AccountTransaction sendRefund(String accountId, String paymentId, String description) {
        var account = getAccount(accountId);
        if (account.status() == AccountStatus.CLOSED) {
            throw new AccountClosedException("La cuenta esta cerrada y no permite pagos ni reembolsos.");
        }

        if (paymentId == null || paymentId.isBlank()) {
            throw new PaymentNotFoundException("Debe seleccionar el pago origen del reembolso.");
        }

        var payment = paymentRecordRepository.findById(paymentId)
                .filter(record -> record.account().id().equals(accountId))
                .orElseThrow(() -> new PaymentNotFoundException("No existe un pago con id " + paymentId + " para esta cuenta."));
        var alreadyRefunded = refundRecordRepository.findByPaymentIdOrderByCreatedAtAsc(payment.id()).stream()
                .map(RefundRecord::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (alreadyRefunded.compareTo(BigDecimal.ZERO) > 0) {
            throw new RefundAmountExceededException("El pago seleccionado ya fue reembolsado.");
        }

        var amount = payment.amount();
        var transaction = account.sendRefund(amount, description, virtualTimeService.getCurrentVirtualTime());
        accountRepository.save(account);
        refundRecordRepository.save(new RefundRecord(account, payment, transaction));
        logEvent(
                EventType.REFUND_SENT,
                "TRANSACTION",
                transaction.id(),
                account.id(),
                "Reembolso registrado por " + transaction.amount() + " COP."
        );
        return transaction;
    }

    /**
     * Aplica un write-off independiente de pagos.
     *
     * @param accountId cuenta ajustada.
     * @param amount monto COP a descontar.
     * @param description descripcion del ajuste.
     * @return transaccion WRITE_OFF creada.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws InsufficientBalanceException si el saldo no cubre el ajuste.
     */
    AccountTransaction writeOff(String accountId, BigDecimal amount, String description) {
        var account = getAccount(accountId);
        var transaction = account.writeOff(amount, description, virtualTimeService.getCurrentVirtualTime());
        accountRepository.save(account);
        writeOffRecordRepository.save(new WriteOffRecord(account, transaction));
        logEvent(
                EventType.WRITE_OFF_APPLIED,
                "TRANSACTION",
                transaction.id(),
                account.id(),
                "Write-off aplicado por " + transaction.amount() + " COP."
        );
        return transaction;
    }

    /**
     * Consulta todo el historial financiero de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return transacciones ordenadas por fecha ascendente.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<AccountTransaction> getTransactions(String accountId) {
        getAccount(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
    }

    /**
     * Consulta pagos de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return transacciones PAYMENT ordenadas por fecha.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<AccountTransaction> getPayments(String accountId) {
        getAccount(accountId);
        return transactionRepository.findByAccountIdAndTypeOrderByCreatedAtAsc(accountId, TransactionType.PAYMENT);
    }

    /**
     * Consulta pagos de una cuenta que siguen pendientes de asignacion.
     *
     * @param accountId cuenta consultada.
     * @return pagos con estado UNALLOCATED.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<PaymentRecord> getUnallocatedPayments(String accountId) {
        getAccount(accountId);
        return paymentRecordRepository.findByAccountIdAndAllocationStatusOrderByCreatedAtAsc(
                accountId,
                PaymentAllocationStatus.UNALLOCATED
        );
    }

    /**
     * Consulta todos los pagos no asignados del sistema.
     *
     * @return pagos UNALLOCATED ordenados por fecha.
     */
    @Transactional(readOnly = true)
    List<PaymentRecord> getUnallocatedPayments() {
        var payments = paymentRecordRepository.findByAllocationStatusOrderByCreatedAtAsc(PaymentAllocationStatus.UNALLOCATED);
        if (userContextService.isAdmin()) {
            return payments;
        }
        var accessibleAccountIds = getAccounts().stream().map(Account::id).toList();
        return payments.stream()
                .filter(payment -> accessibleAccountIds.contains(payment.account().id()))
                .toList();
    }

    /**
     * Consulta reembolsos de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return transacciones REFUND ordenadas por fecha.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<AccountTransaction> getRefunds(String accountId) {
        getAccount(accountId);
        return transactionRepository.findByAccountIdAndTypeOrderByCreatedAtAsc(accountId, TransactionType.REFUND);
    }

    /**
     * Consulta write-offs de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return transacciones WRITE_OFF ordenadas por fecha.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<AccountTransaction> getWriteOffs(String accountId) {
        getAccount(accountId);
        return transactionRepository.findByAccountIdAndTypeOrderByCreatedAtAsc(accountId, TransactionType.WRITE_OFF);
    }

    /**
     * Cierra logicamente una cuenta.
     *
     * @param accountId cuenta a cerrar.
     * @return cuenta actualizada.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    Account closeAccount(String accountId) {
        var account = getAccount(accountId);
        account.close();
        var savedAccount = accountRepository.save(account);
        logEvent(
                EventType.ACCOUNT_CLOSED,
                "ACCOUNT",
                savedAccount.id(),
                savedAccount.id(),
                "Cuenta cerrada."
        );
        return savedAccount;
    }

    /**
     * Crea una disputa pendiente para una cuenta.
     *
     * @param accountId cuenta asociada.
     * @param amount monto disputado en COP.
     * @param reason motivo de la disputa.
     * @return disputa persistida.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    Dispute createDispute(String accountId, BigDecimal amount, String reason) {
        var account = getAccount(accountId);
        var dispute = disputeRepository.save(new Dispute(account, amount, reason, virtualTimeService.getCurrentVirtualTime()));
        logEvent(
                EventType.DISPUTE_CREATED,
                "DISPUTE",
                dispute.id(),
                account.id(),
                "Disputa creada por " + dispute.amount() + " COP."
        );
        return dispute;
    }

    /**
     * Crea un settlement y cierra la disputa en estado SETTLED.
     *
     * @param disputeId disputa pendiente.
     * @param amount monto COP del acuerdo.
     * @param note nota del settlement.
     * @return settlement persistido.
     * @throws DisputeNotFoundException si la disputa no existe.
     * @throws DisputeAlreadyResolvedException si la disputa ya fue resuelta.
     */
    DisputeSettlement createDisputeSettlement(String disputeId, BigDecimal amount, String note) {
        var dispute = getDispute(disputeId);
        var pinVirtualTimeT = virtualTimeService.getCurrentVirtualTime();
        dispute.settle(note, pinVirtualTimeT);
        var savedDispute = disputeRepository.save(dispute);
        var settlement = disputeSettlementRepository.save(new DisputeSettlement(dispute, amount, note, pinVirtualTimeT));
        logEvent(
                EventType.DISPUTE_SETTLEMENT_CREATED,
                "DISPUTE_SETTLEMENT",
                settlement.id(),
                settlement.account().id(),
                "Settlement creado para disputa " + dispute.id() + " por " + settlement.amount() + " COP."
        );
        logEvent(
                EventType.DISPUTE_SETTLED,
                "DISPUTE",
                savedDispute.id(),
                savedDispute.account().id(),
                "Disputa cerrada por settlement."
        );
        return settlement;
    }

    /**
     * Consulta settlements de una disputa.
     *
     * @param disputeId disputa consultada.
     * @return settlements ordenados por fecha.
     * @throws DisputeNotFoundException si la disputa no existe.
     */
    @Transactional(readOnly = true)
    List<DisputeSettlement> getDisputeSettlements(String disputeId) {
        getDispute(disputeId);
        return disputeSettlementRepository.findByDisputeIdOrderByCreatedAtAsc(disputeId);
    }

    /**
     * Consulta disputas aplicando filtros opcionales.
     *
     * @param accountId cuenta opcional para restringir resultados.
     * @param status estado opcional.
     * @return disputas ordenadas por fecha de creacion.
     * @throws AccountNotFoundException si se informa una cuenta inexistente.
     */
    @Transactional(readOnly = true)
    List<Dispute> getDisputes(String accountId, DisputeStatus status) {
        if (accountId != null && !accountId.isBlank() && status != null) {
            getAccount(accountId);
            return disputeRepository.findByAccountIdAndStatusOrderByCreatedAtAsc(accountId, status);
        }

        if (accountId != null && !accountId.isBlank()) {
            getAccount(accountId);
            return disputeRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
        }

        if (status != null && userContextService.isAdmin()) {
            return disputeRepository.findByStatusOrderByCreatedAtAsc(status);
        }

        var accessibleAccountIds = getAccounts().stream().map(Account::id).toList();
        return disputeRepository.findAll().stream()
                .filter(dispute -> accessibleAccountIds.contains(dispute.account().id()))
                .filter(dispute -> status == null || dispute.status() == status)
                .sorted(Comparator.comparing(Dispute::createdAt))
                .toList();
    }

    /**
     * Aprueba una disputa pendiente.
     *
     * @param disputeId disputa a aprobar.
     * @param resolutionNote nota de resolucion.
     * @return disputa actualizada.
     * @throws DisputeNotFoundException si la disputa no existe.
     * @throws DisputeAlreadyResolvedException si la disputa ya fue resuelta.
     */
    Dispute approveDispute(String disputeId, String resolutionNote) {
        var dispute = getDispute(disputeId);
        dispute.approve(resolutionNote, virtualTimeService.getCurrentVirtualTime());
        var savedDispute = disputeRepository.save(dispute);
        logEvent(
                EventType.DISPUTE_APPROVED,
                "DISPUTE",
                savedDispute.id(),
                savedDispute.account().id(),
                "Disputa aprobada."
        );
        return savedDispute;
    }

    /**
     * Rechaza una disputa pendiente.
     *
     * @param disputeId disputa a rechazar.
     * @param resolutionNote nota de resolucion.
     * @return disputa actualizada.
     * @throws DisputeNotFoundException si la disputa no existe.
     * @throws DisputeAlreadyResolvedException si la disputa ya fue resuelta.
     */
    Dispute rejectDispute(String disputeId, String resolutionNote) {
        var dispute = getDispute(disputeId);
        dispute.reject(resolutionNote, virtualTimeService.getCurrentVirtualTime());
        var savedDispute = disputeRepository.save(dispute);
        logEvent(
                EventType.DISPUTE_REJECTED,
                "DISPUTE",
                savedDispute.id(),
                savedDispute.account().id(),
                "Disputa rechazada."
        );
        return savedDispute;
    }

    /**
     * Consulta eventos de auditoria con filtros opcionales.
     *
     * @param accountId cuenta opcional.
     * @param type tipo de evento opcional.
     * @return eventos ordenados por fecha.
     * @throws AccountNotFoundException si se informa una cuenta inexistente.
     */
    @Transactional(readOnly = true)
    List<SystemEvent> getEvents(String accountId, EventType type) {
        if (accountId != null && !accountId.isBlank() && type != null) {
            getAccount(accountId);
            return eventRepository.findByAccountIdAndTypeOrderByCreatedAtAsc(accountId, type);
        }

        if (accountId != null && !accountId.isBlank()) {
            getAccount(accountId);
            return eventRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
        }

        if (type != null && userContextService.isAdmin()) {
            return eventRepository.findByTypeOrderByCreatedAtAsc(type);
        }

        var accessibleAccountIds = getAccounts().stream().map(Account::id).toList();
        return eventRepository.findAll().stream()
                .filter(event -> event.accountId() == null || accessibleAccountIds.contains(event.accountId()))
                .filter(event -> type == null || event.type() == type)
                .sorted(Comparator.comparing(SystemEvent::createdAt))
                .toList();
    }

    /**
     * Busca transacciones para reportes y exportacion CSV.
     *
     * @param accountId cuenta opcional.
     * @param type tipo de transaccion opcional.
     * @param dateFrom fecha/hora inicial en formato compatible con {@link LocalDateTime#parse(CharSequence)}.
     * @param dateTo fecha/hora final en formato compatible con {@link LocalDateTime#parse(CharSequence)}.
     * @param minAmount monto minimo opcional.
     * @param maxAmount monto maximo opcional.
     * @return transacciones que cumplen los filtros.
     */
    @Transactional(readOnly = true)
    List<TransactionSearchResponse> searchTransactions(
            String accountId,
            TransactionType type,
            String dateFrom,
            String dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        var from = parseDateTime(dateFrom);
        var to = parseDateTime(dateTo);

        return getAccounts().stream()
                .filter(account -> accountId == null || accountId.isBlank() || account.id().equals(accountId))
                .flatMap(account -> getTransactions(account.id()).stream()
                        .map(transaction -> TransactionSearchResponse.from(account, transaction)))
                .filter(transaction -> type == null || transaction.type() == type)
                .filter(transaction -> from.isEmpty() || !transaction.createdAt().isBefore(from.get()))
                .filter(transaction -> to.isEmpty() || !transaction.createdAt().isAfter(to.get()))
                .filter(transaction -> minAmount == null || transaction.amount().compareTo(minAmount) >= 0)
                .filter(transaction -> maxAmount == null || transaction.amount().compareTo(maxAmount) <= 0)
                .sorted(Comparator.comparing(TransactionSearchResponse::createdAt))
                .toList();
    }

    private Optional<Instant> parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant());
    }

    private Integer normalizeBillingDom(Integer billingDom, LocalDateTime pinVirtualTimeT) {
        var value = billingDom == null
                ? (pinVirtualTimeT == null ? LocalDateTime.now().getDayOfMonth() : pinVirtualTimeT.getDayOfMonth())
                : billingDom;
        if (value < 1 || value > 31) {
            throw new BusinessRuleException("El DOM de facturacion debe estar entre 1 y 31.");
        }
        return value;
    }

    private String normalizeBillingCycle(String billingCycle) {
        if (billingCycle == null || billingCycle.isBlank()) {
            return "MONTHLY";
        }
        var normalized = billingCycle.trim().toUpperCase();
        if (!List.of("MONTHLY", "BIMONTHLY", "QUARTERLY", "ANNUAL").contains(normalized)) {
            throw new BusinessRuleException("El ciclo de facturacion no es valido.");
        }
        return normalized;
    }

    private Dispute getDispute(String disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("No existe una disputa con id " + disputeId + "."));
    }

    private void logEvent(EventType type, String entityType, String entityId, String accountId, String description) {
        eventRepository.save(new SystemEvent(
                type,
                entityType,
                entityId,
                accountId,
                description,
                virtualTimeService.getCurrentVirtualTime()
        ));
    }
}
