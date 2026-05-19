package com.brmc.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para generar y administrar invoices.
 *
 * <p>Construye facturas desde cargos de billing, conserva lineas snapshot, controla estados
 * funcionales y registra eventos {@code INVOICE_*}. La integracion con pagos usa el flujo actual
 * de {@link AccountService}: el pago incrementa el saldo de la cuenta y luego se aplica al saldo
 * pendiente de la factura.</p>
 */
@Service
@Transactional
class InvoiceService {

    private final AccountRepository accountRepository;
    private final BillingRunRepository billingRunRepository;
    private final BillingChargeRepository billingChargeRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final AccountService accountService;
    private final VirtualTimeService virtualTimeService;
    private final IdSequenceService idSequenceService;
    private final SystemEventRepository eventRepository;
    private final UserContextService userContextService;

    /**
     * Crea el servicio de invoices.
     *
     * @param accountRepository repositorio de cuentas.
     * @param billingRunRepository repositorio de corridas de billing.
     * @param billingChargeRepository repositorio de cargos de billing.
     * @param invoiceRepository repositorio de facturas.
     * @param invoiceLineRepository repositorio de lineas.
     * @param paymentRecordRepository repositorio de pagos.
     * @param accountService servicio financiero de cuentas.
     * @param virtualTimeService proveedor de fecha virtual.
     * @param eventRepository repositorio de auditoria.
     */
    InvoiceService(
            AccountRepository accountRepository,
            BillingRunRepository billingRunRepository,
            BillingChargeRepository billingChargeRepository,
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            PaymentRecordRepository paymentRecordRepository,
            AccountService accountService,
            VirtualTimeService virtualTimeService,
            IdSequenceService idSequenceService,
            SystemEventRepository eventRepository,
            UserContextService userContextService
    ) {
        this.accountRepository = accountRepository;
        this.billingRunRepository = billingRunRepository;
        this.billingChargeRepository = billingChargeRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.accountService = accountService;
        this.virtualTimeService = virtualTimeService;
        this.idSequenceService = idSequenceService;
        this.eventRepository = eventRepository;
        this.userContextService = userContextService;
    }

    /**
     * Genera invoices para todas las cuentas con cargos en una corrida.
     *
     * @param billingRunId corrida origen.
     * @return facturas nuevas o existentes para la corrida.
     * @throws BillingRunNotFoundException si la corrida no existe.
     */
    List<Invoice> generateInvoicesForBillingRun(String billingRunId) {
        var run = getBillingRun(billingRunId);
        var charges = billingChargeRepository.findByBillingRunIdOrderByCreatedAtAsc(run.id());
        if (charges.isEmpty()) {
            return List.of();
        }

        return charges.stream()
                .collect(Collectors.groupingBy(
                        charge -> charge.account().id(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(accountCharges -> generateInvoiceForAccountAndBillingRun(
                        accountCharges.get(0).account().id(),
                        run.id()
                ))
                .toList();
    }

    /**
     * Genera una factura para una cuenta dentro de una corrida.
     *
     * @param accountId cuenta facturada.
     * @param billingRunId corrida origen.
     * @return factura generada o existente.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws BillingRunNotFoundException si la corrida no existe.
     * @throws BusinessRuleException si no hay cargos para facturar.
     */
    Invoice generateInvoiceForAccountAndBillingRun(String accountId, String billingRunId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        var run = getBillingRun(billingRunId);
        var existing = invoiceRepository.findByBillingRunIdAndAccountId(run.id(), account.id());
        if (existing.isPresent()) {
            return existing.get();
        }

        var charges = billingChargeRepository.findByBillingRunIdOrderByCreatedAtAsc(run.id()).stream()
                .filter(charge -> charge.account().id().equals(account.id()))
                .toList();
        if (charges.isEmpty()) {
            throw new BusinessRuleException("No hay cargos de billing para generar invoice.");
        }
        if (account.status() == AccountStatus.CLOSED) {
            throw new AccountClosedException("La cuenta esta cerrada y no puede generar nuevas invoices de billing.");
        }

        var periodStart = charges.stream()
                .map(charge -> charge.bill() == null ? null : charge.bill().periodStart())
                .filter(value -> value != null)
                .min(Comparator.naturalOrder())
                .orElse(run.virtualTime().minusMonths(1));
        var periodEnd = charges.stream()
                .map(charge -> charge.bill() == null ? null : charge.bill().periodEnd())
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(run.virtualTime());

        var invoice = invoiceRepository.save(new Invoice(
                account,
                run,
                run.virtualTime(),
                periodStart,
                periodEnd,
                idSequenceService.nextId("INVOICE"),
                "billing"
        ));
        var lines = charges.stream()
                .map(charge -> invoiceLineRepository.save(new InvoiceLine(invoice, charge)))
                .toList();
        var subtotal = lines.stream()
                .map(InvoiceLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var taxAmount = lines.stream()
                .map(InvoiceLine::taxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.updateTotals(subtotal, taxAmount);
        var saved = invoiceRepository.save(invoice);

        logEvent(EventType.INVOICE_GENERATED, "INVOICE", saved.id(), account.id(),
                "Invoice " + saved.invoiceNumber() + " generada por " + saved.totalAmount() + " COP.");
        lines.forEach(line -> logEvent(EventType.INVOICE_LINE_CREATED, "INVOICE_LINE", line.id(), account.id(),
                "Linea creada para invoice " + saved.invoiceNumber() + ": " + line.description() + "."));
        return saved;
    }

    /**
     * Lista invoices con filtros opcionales.
     *
     * @param accountId cuenta opcional.
     * @param status estado opcional.
     * @param from fecha inicial opcional.
     * @param to fecha final opcional.
     * @return facturas coincidentes.
     */
    @Transactional(readOnly = true)
    List<Invoice> getInvoices(String accountId, InvoiceStatus status, String from, String to) {
        var fromDate = parseDateTime(from);
        var toDate = parseDateTime(to);
        return invoiceRepository.findAll().stream()
                .filter(invoice -> userContextService.canAccess(invoice.account()))
                .filter(invoice -> accountId == null || accountId.isBlank() || invoice.account().id().equals(accountId))
                .filter(invoice -> status == null || invoice.status() == status)
                .filter(invoice -> fromDate.isEmpty() || !invoice.issueDate().isBefore(fromDate.get()))
                .filter(invoice -> toDate.isEmpty() || !invoice.issueDate().isAfter(toDate.get()))
                .sorted(Comparator.comparing(Invoice::issueDate).reversed())
                .toList();
    }

    /**
     * Consulta una factura por id.
     *
     * @param invoiceId identificador interno.
     * @return factura encontrada.
     * @throws InvoiceNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    Invoice getInvoice(String invoiceId) {
        var invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("No existe una invoice con id " + invoiceId + "."));
        ensureAccountVisible(invoice.account());
        return invoice;
    }

    /**
     * Consulta una factura por numero funcional.
     *
     * @param invoiceNumber numero de factura.
     * @return factura encontrada.
     * @throws InvoiceNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    Invoice getInvoiceByNumber(String invoiceNumber) {
        var invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException("No existe una invoice con numero " + invoiceNumber + "."));
        ensureAccountVisible(invoice.account());
        return invoice;
    }

    /**
     * Lista facturas de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return facturas de la cuenta.
     */
    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        return invoiceRepository.findByAccountIdOrderByIssueDateDesc(accountId);
    }

    /**
     * Lista lineas de una factura.
     *
     * @param invoiceId factura consultada.
     * @return lineas ordenadas.
     */
    @Transactional(readOnly = true)
    List<InvoiceLine> getInvoiceLines(String invoiceId) {
        getInvoice(invoiceId);
        return invoiceLineRepository.findByInvoiceIdOrderByCreatedAtAsc(invoiceId);
    }

    /**
     * Marca una factura como enviada.
     *
     * @param invoiceId factura actualizada.
     * @return factura persistida.
     */
    Invoice markAsSent(String invoiceId) {
        var invoice = getInvoice(invoiceId);
        invoice.markAsSent(virtualTimeService.getCurrentVirtualTime());
        var saved = invoiceRepository.save(invoice);
        logEvent(EventType.INVOICE_SENT, "INVOICE", saved.id(), saved.account().id(),
                "Invoice " + saved.invoiceNumber() + " marcada como enviada.");
        return saved;
    }

    /**
     * Cancela una factura no pagada.
     *
     * @param invoiceId factura a cancelar.
     * @param reason motivo de cancelacion.
     * @return factura actualizada.
     */
    Invoice cancelInvoice(String invoiceId, String reason) {
        var invoice = getInvoice(invoiceId);
        invoice.cancel(virtualTimeService.getCurrentVirtualTime(), reason);
        var saved = invoiceRepository.save(invoice);
        logEvent(EventType.INVOICE_CANCELLED, "INVOICE", saved.id(), saved.account().id(),
                "Invoice " + saved.invoiceNumber() + " cancelada.");
        return saved;
    }

    /**
     * Aplica un pago manual a una factura.
     *
     * @param invoiceId factura pagada.
     * @param amount monto COP aplicado.
     * @return factura actualizada.
     */
    Invoice applyPaymentToInvoice(String invoiceId, BigDecimal amount) {
        var invoice = getInvoice(invoiceId);
        var transaction = accountService.receivePayment(
                invoice.account().id(),
                amount,
                Currency.COP,
                PaymentMethod.CASH,
                "Invoice payment - " + invoice.invoiceNumber()
        );
        paymentRecordRepository.findById(transaction.id())
                .ifPresent(payment -> {
                    payment.applyAllocation(invoice, amount);
                    paymentRecordRepository.save(payment);
                });
        invoice.applyPayment(amount, virtualTimeService.getCurrentVirtualTime());
        var saved = invoiceRepository.save(invoice);
        logEvent(
                saved.status() == InvoiceStatus.PAID ? EventType.INVOICE_PAID : EventType.INVOICE_PARTIALLY_PAID,
                "INVOICE",
                saved.id(),
                saved.account().id(),
                "Pago aplicado a invoice " + saved.invoiceNumber() + " por " + amount + " COP."
        );
        return saved;
    }

    /**
     * Renderiza una factura como HTML autonomo.
     *
     * @param invoiceId factura consultada.
     * @return documento HTML.
     */
    @Transactional(readOnly = true)
    String renderInvoiceHtml(String invoiceId) {
        var invoice = getInvoice(invoiceId);
        var lines = getInvoiceLines(invoiceId);
        var rows = lines.stream()
                .map(line -> """
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                        </tr>
                        """.formatted(
                        safe(line.chargeDate()),
                        safe(line.service() == null ? null : line.service().serviceCode()),
                        safe(line.product() == null ? null : line.product().code()),
                        safe(line.description()),
                        line.lineType(),
                        line.quantity(),
                        money(line.totalAmount(), line.currency())
                ))
                .collect(Collectors.joining());
        return """
                <!doctype html>
                <html lang="es">
                <head>
                    <meta charset="utf-8">
                    <title>%s</title>
                    <link rel="stylesheet" href="/css/brmc-billing-care.css">
                </head>
                <body class="bc-print-body">
                    <section class="bc-invoice-document">
                        <header class="bc-invoice-header">
                            <div>
                                <h1>BRMC Billing Care</h1>
                                <p>Factura de servicios</p>
                            </div>
                            <div>
                                <strong>%s</strong><br>
                                <span class="status %s">%s</span>
                            </div>
                        </header>
                        <section class="bc-invoice-summary">
                            <div><span>Cuenta</span><strong>%s</strong></div>
                            <div><span>Cliente</span><strong>%s</strong></div>
                            <div><span>Emision</span><strong>%s</strong></div>
                            <div><span>Vencimiento</span><strong>%s</strong></div>
                            <div><span>Total</span><strong>%s</strong></div>
                            <div><span>Notas de credito</span><strong>%s</strong></div>
                            <div><span>Saldo pendiente</span><strong>%s</strong></div>
                        </section>
                        <table>
                            <thead><tr><th>Fecha cargo</th><th>Servicio</th><th>Producto</th><th>Descripcion</th><th>Tipo</th><th>Cantidad</th><th>Total</th></tr></thead>
                            <tbody>%s</tbody>
                        </table>
                    </section>
                </body>
                </html>
                """.formatted(
                invoice.invoiceNumber(),
                invoice.invoiceNumber(),
                invoice.status().name().toLowerCase(),
                invoice.status(),
                invoice.account().id(),
                invoice.account().ownerName(),
                invoice.issueDate(),
                invoice.dueDate(),
                money(invoice.totalAmount(), invoice.currency()),
                money(invoice.creditAmount(), invoice.currency()),
                money(invoice.amountDue(), invoice.currency()),
                rows
        );
    }

    /**
     * Exporta una factura en CSV.
     *
     * @param invoiceId factura exportada.
     * @return contenido CSV.
     */
    @Transactional(readOnly = true)
    String exportInvoiceCsv(String invoiceId) {
        var invoice = getInvoice(invoiceId);
        var lines = getInvoiceLines(invoiceId);
        var csv = new StringBuilder();
        csv.append("invoiceNumber,status,accountId,accountOwner,issueDate,dueDate,totalAmount,amountPaid,creditAmount,amountDue\n");
        csv.append(csv(invoice.invoiceNumber())).append(',')
                .append(invoice.status()).append(',')
                .append(csv(invoice.account().id())).append(',')
                .append(csv(invoice.account().ownerName())).append(',')
                .append(invoice.issueDate()).append(',')
                .append(invoice.dueDate()).append(',')
                .append(invoice.totalAmount()).append(',')
                .append(invoice.amountPaid()).append(',')
                .append(invoice.creditAmount()).append(',')
                .append(invoice.amountDue()).append("\n\n");
        csv.append("lineId,billingChargeId,serviceCode,productCode,lineType,description,quantity,unitAmount,subtotal,taxAmount,totalAmount,currency,chargeDate\n");
        lines.forEach(line -> csv.append(csv(line.id())).append(',')
                .append(csv(line.billingCharge() == null ? null : line.billingCharge().id())).append(',')
                .append(csv(line.service() == null ? null : line.service().serviceCode())).append(',')
                .append(csv(line.product() == null ? null : line.product().code())).append(',')
                .append(line.lineType()).append(',')
                .append(csv(line.description())).append(',')
                .append(line.quantity()).append(',')
                .append(line.unitAmount()).append(',')
                .append(line.subtotal()).append(',')
                .append(line.taxAmount()).append(',')
                .append(line.totalAmount()).append(',')
                .append(line.currency()).append(',')
                .append(line.chargeDate()).append('\n'));
        return csv.toString();
    }

    private BillingRun getBillingRun(String billingRunId) {
        return billingRunRepository.findById(billingRunId)
                .orElseThrow(() -> new BillingRunNotFoundException("No existe un billing run con id " + billingRunId + "."));
    }

    private void ensureAccountVisible(Account account) {
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + account.id() + ".");
        }
    }

    private Optional<LocalDateTime> parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(LocalDateTime.parse(value));
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

    private String money(BigDecimal amount, Currency currency) {
        return amount + " " + currency;
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private String csv(Object value) {
        var text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
