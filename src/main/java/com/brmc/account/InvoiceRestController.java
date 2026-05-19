package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de invoices.
 *
 * <p>Expone consultas, generacion desde billing, cambio de estado, pago aplicado y exportacion CSV
 * de facturas. La logica de negocio se delega a {@link InvoiceService}.</p>
 */
@RestController
@RequestMapping("/api")
class InvoiceRestController {

    private final InvoiceService invoiceService;

    /**
     * Crea el controlador REST.
     *
     * @param invoiceService servicio de invoices.
     */
    InvoiceRestController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
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
    @GetMapping("/invoices")
    List<InvoiceResponse> getInvoices(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return invoiceService.getInvoices(accountId, status, from, to).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    /**
     * Consulta una invoice por id.
     *
     * @param invoiceId identificador interno.
     * @return factura con lineas.
     */
    @GetMapping("/invoices/{invoiceId}")
    InvoiceResponse getInvoice(@PathVariable String invoiceId) {
        return InvoiceResponse.from(invoiceService.getInvoice(invoiceId), invoiceService.getInvoiceLines(invoiceId));
    }

    /**
     * Consulta una invoice por numero funcional.
     *
     * @param invoiceNumber numero visible.
     * @return factura con lineas.
     */
    @GetMapping("/invoices/number/{invoiceNumber}")
    InvoiceResponse getInvoiceByNumber(@PathVariable String invoiceNumber) {
        var invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return InvoiceResponse.from(invoice, invoiceService.getInvoiceLines(invoice.id()));
    }

    /**
     * Lista invoices de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return facturas de la cuenta.
     */
    @GetMapping("/accounts/{accountId}/invoices")
    List<InvoiceResponse> getInvoicesByAccount(@PathVariable String accountId) {
        return invoiceService.getInvoicesByAccount(accountId).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    /**
     * Genera invoices para un billing run existente.
     *
     * @param billingRunId corrida origen.
     * @return invoices generadas o ya existentes.
     */
    @PostMapping("/billing/runs/{billingRunId}/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    List<InvoiceResponse> generateInvoicesForBillingRun(@PathVariable String billingRunId) {
        return invoiceService.generateInvoicesForBillingRun(billingRunId).stream()
                .map(invoice -> InvoiceResponse.from(invoice, invoiceService.getInvoiceLines(invoice.id())))
                .toList();
    }

    /**
     * Marca una invoice como enviada.
     *
     * @param invoiceId factura actualizada.
     * @return factura actualizada.
     */
    @PostMapping("/invoices/{invoiceId}/sent")
    InvoiceResponse markAsSent(@PathVariable String invoiceId) {
        var invoice = invoiceService.markAsSent(invoiceId);
        return InvoiceResponse.from(invoice, invoiceService.getInvoiceLines(invoice.id()));
    }

    /**
     * Cancela una invoice.
     *
     * @param invoiceId factura cancelada.
     * @param request motivo opcional.
     * @return factura actualizada.
     */
    @PostMapping("/invoices/{invoiceId}/cancel")
    InvoiceResponse cancelInvoice(
            @PathVariable String invoiceId,
            @RequestBody(required = false) InvoiceCancelRequest request
    ) {
        var reason = request == null ? "Cancelacion manual" : request.reason();
        var invoice = invoiceService.cancelInvoice(invoiceId, reason);
        return InvoiceResponse.from(invoice, invoiceService.getInvoiceLines(invoice.id()));
    }

    /**
     * Aplica un pago a una invoice.
     *
     * @param invoiceId factura pagada.
     * @param request monto validado.
     * @return factura actualizada.
     */
    @PostMapping("/invoices/{invoiceId}/payment")
    InvoiceResponse applyPayment(
            @PathVariable String invoiceId,
            @Valid @RequestBody InvoicePaymentRequest request
    ) {
        var invoice = invoiceService.applyPaymentToInvoice(invoiceId, request.amount());
        return InvoiceResponse.from(invoice, invoiceService.getInvoiceLines(invoice.id()));
    }

    /**
     * Lista lineas de una invoice.
     *
     * @param invoiceId factura consultada.
     * @return lineas de la factura.
     */
    @GetMapping("/invoices/{invoiceId}/lines")
    List<InvoiceLineResponse> getInvoiceLines(@PathVariable String invoiceId) {
        return invoiceService.getInvoiceLines(invoiceId).stream()
                .map(InvoiceLineResponse::from)
                .toList();
    }

    /**
     * Exporta una invoice en CSV.
     *
     * @param invoiceId factura exportada.
     * @return archivo CSV.
     */
    @GetMapping("/invoices/{invoiceId}/csv")
    ResponseEntity<String> exportInvoiceCsv(@PathVariable String invoiceId) {
        var invoice = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(invoice.invoiceNumber() + ".csv")
                        .build()
                        .toString())
                .body(invoiceService.exportInvoiceCsv(invoiceId));
    }

    /**
     * Renderiza una invoice como HTML simple.
     *
     * @param invoiceId factura consultada.
     * @return documento HTML.
     */
    @GetMapping(value = "/invoices/{invoiceId}/html", produces = MediaType.TEXT_HTML_VALUE)
    String renderInvoiceHtml(@PathVariable String invoiceId) {
        return invoiceService.renderInvoiceHtml(invoiceId);
    }

    /**
     * Solicitud para cancelar una invoice.
     *
     * @param reason motivo visible en notas.
     */
    record InvoiceCancelRequest(String reason) {
    }

    /**
     * Solicitud para pago de invoice.
     *
     * @param amount monto COP positivo.
     */
    record InvoicePaymentRequest(
            @NotNull(message = "amount es obligatorio")
            @DecimalMin(value = "0.01", message = "amount debe ser mayor a cero")
            BigDecimal amount
    ) {
    }
}
