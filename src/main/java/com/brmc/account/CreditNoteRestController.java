package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
 * Controlador REST de notas de credito.
 */
@RestController
@RequestMapping("/api")
class CreditNoteRestController {

    private final CreditNoteService creditNoteService;

    CreditNoteRestController(CreditNoteService creditNoteService) {
        this.creditNoteService = creditNoteService;
    }

    @GetMapping("/credit-notes")
    List<CreditNoteResponse> getCreditNotes() {
        return creditNoteService.getCreditNotes().stream()
                .map(CreditNoteResponse::from)
                .toList();
    }

    @GetMapping("/credit-notes/{creditNoteId}")
    CreditNoteResponse getCreditNote(@PathVariable String creditNoteId) {
        var creditNote = creditNoteService.getCreditNote(creditNoteId);
        return CreditNoteResponse.from(creditNote, creditNoteService.getCreditNoteLines(creditNote.id()));
    }

    @GetMapping("/credit-notes/number/{creditNoteNumber}")
    CreditNoteResponse getCreditNoteByNumber(@PathVariable String creditNoteNumber) {
        var creditNote = creditNoteService.getCreditNoteByNumber(creditNoteNumber);
        return CreditNoteResponse.from(creditNote, creditNoteService.getCreditNoteLines(creditNote.id()));
    }

    @GetMapping("/invoices/{invoiceId}/credit-notes")
    List<CreditNoteResponse> getCreditNotesByInvoice(@PathVariable String invoiceId) {
        return creditNoteService.getCreditNotesByInvoice(invoiceId).stream()
                .map(CreditNoteResponse::from)
                .toList();
    }

    @GetMapping("/accounts/{accountId}/credit-notes")
    List<CreditNoteResponse> getCreditNotesByAccount(@PathVariable String accountId) {
        return creditNoteService.getCreditNotesByAccount(accountId).stream()
                .map(CreditNoteResponse::from)
                .toList();
    }

    @PostMapping("/invoices/{invoiceId}/credit-notes")
    @ResponseStatus(HttpStatus.CREATED)
    CreditNoteResponse createCreditNote(
            @PathVariable String invoiceId,
            @Valid @RequestBody CreditNoteRequest request
    ) {
        var creditNote = creditNoteService.createCreditNote(
                invoiceId,
                request.amount(),
                request.reason(),
                request.description(),
                request.invoiceLineId()
        );
        return CreditNoteResponse.from(creditNote, creditNoteService.getCreditNoteLines(creditNote.id()));
    }

    @PostMapping("/credit-notes/{creditNoteId}/cancel")
    CreditNoteResponse cancelCreditNote(
            @PathVariable String creditNoteId,
            @RequestBody(required = false) CreditNoteCancelRequest request
    ) {
        var reason = request == null ? "Cancelacion manual" : request.reason();
        var creditNote = creditNoteService.cancelCreditNote(creditNoteId, reason);
        return CreditNoteResponse.from(creditNote, creditNoteService.getCreditNoteLines(creditNote.id()));
    }

    record CreditNoteRequest(
            @NotNull(message = "amount es obligatorio")
            @DecimalMin(value = "0.01", message = "amount debe ser mayor a cero")
            BigDecimal amount,
            String reason,
            String description,
            String invoiceLineId
    ) {
    }

    record CreditNoteCancelRequest(String reason) {
    }
}
