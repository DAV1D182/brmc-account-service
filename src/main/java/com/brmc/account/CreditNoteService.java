package com.brmc.account;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para crear y consultar notas de credito de invoices.
 *
 * <p>Una nota de credito reduce el saldo pendiente de la factura sin eliminar cargos ni lineas
 * originales. Cada creacion genera una linea documental, actualiza la invoice y registra eventos
 * de auditoria.</p>
 */
@Service
@Transactional
class CreditNoteService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteLineRepository creditNoteLineRepository;
    private final VirtualTimeService virtualTimeService;
    private final IdSequenceService idSequenceService;
    private final SystemEventRepository eventRepository;
    private final UserContextService userContextService;

    CreditNoteService(
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            CreditNoteRepository creditNoteRepository,
            CreditNoteLineRepository creditNoteLineRepository,
            VirtualTimeService virtualTimeService,
            IdSequenceService idSequenceService,
            SystemEventRepository eventRepository,
            UserContextService userContextService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.creditNoteLineRepository = creditNoteLineRepository;
        this.virtualTimeService = virtualTimeService;
        this.idSequenceService = idSequenceService;
        this.eventRepository = eventRepository;
        this.userContextService = userContextService;
    }

    /**
     * Crea y aplica una nota de credito a una invoice.
     *
     * @param invoiceId factura objetivo.
     * @param amount monto positivo a acreditar.
     * @param reason motivo de negocio.
     * @param description descripcion de la linea.
     * @param invoiceLineId linea original opcional.
     * @return nota creada y aplicada.
     */
    CreditNote createCreditNote(
            String invoiceId,
            BigDecimal amount,
            String reason,
            String description,
            String invoiceLineId
    ) {
        var invoice = getInvoice(invoiceId);
        validateAmount(invoice, amount);
        var invoiceLine = resolveInvoiceLine(invoice, invoiceLineId);
        var now = virtualTimeService.getCurrentVirtualTime();
        invoice.applyCredit(amount, now);
        invoiceRepository.save(invoice);

        var creditNote = creditNoteRepository.save(new CreditNote(
                invoice,
                idSequenceService.nextId("CREDIT_NOTE"),
                amount,
                normalize(reason, "Nota de credito"),
                normalize(description, "Credito aplicado a invoice " + invoice.invoiceNumber()),
                now
        ));
        var line = creditNoteLineRepository.save(new CreditNoteLine(
                creditNote,
                invoiceLine,
                normalize(description, "Credito aplicado a invoice " + invoice.invoiceNumber()),
                amount,
                now
        ));

        logEvent(EventType.CREDIT_NOTE_CREATED, "CREDIT_NOTE", creditNote.id(), invoice.account().id(),
                "Nota de credito " + creditNote.creditNoteNumber() + " creada por " + amount + " COP.");
        logEvent(EventType.CREDIT_NOTE_LINE_CREATED, "CREDIT_NOTE_LINE", line.id(), invoice.account().id(),
                "Linea creada para nota de credito " + creditNote.creditNoteNumber() + ".");
        logEvent(EventType.CREDIT_NOTE_APPLIED, "INVOICE", invoice.id(), invoice.account().id(),
                "Nota de credito " + creditNote.creditNoteNumber() + " aplicada a invoice " + invoice.invoiceNumber() + ".");
        return creditNote;
    }

    /**
     * Cancela una nota de credito aplicada y reversa su impacto en la invoice.
     *
     * @param creditNoteId nota a cancelar.
     * @param reason motivo de cancelacion.
     * @return nota cancelada.
     */
    CreditNote cancelCreditNote(String creditNoteId, String reason) {
        var creditNote = getCreditNote(creditNoteId);
        var invoice = creditNote.invoice();
        if (invoice.status() == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede reversar una nota de credito de una invoice cancelada.");
        }
        invoice.reverseCredit(creditNote.totalAmount());
        invoiceRepository.save(invoice);
        creditNote.cancel(virtualTimeService.getCurrentVirtualTime(), normalize(reason, "Cancelacion de nota de credito"));
        var saved = creditNoteRepository.save(creditNote);
        logEvent(EventType.CREDIT_NOTE_CANCELLED, "CREDIT_NOTE", saved.id(), saved.account().id(),
                "Nota de credito " + saved.creditNoteNumber() + " cancelada.");
        return saved;
    }

    /**
     * Consulta una nota por id.
     *
     * @param creditNoteId identificador de nota.
     * @return nota encontrada.
     */
    @Transactional(readOnly = true)
    CreditNote getCreditNote(String creditNoteId) {
        var creditNote = creditNoteRepository.findById(creditNoteId)
                .orElseThrow(() -> new CreditNoteNotFoundException("No existe una nota de credito con id " + creditNoteId + "."));
        ensureAccountVisible(creditNote.account());
        return creditNote;
    }

    /**
     * Consulta una nota por numero visible.
     *
     * @param creditNoteNumber numero funcional.
     * @return nota encontrada.
     */
    @Transactional(readOnly = true)
    CreditNote getCreditNoteByNumber(String creditNoteNumber) {
        var creditNote = creditNoteRepository.findByCreditNoteNumber(creditNoteNumber)
                .orElseThrow(() -> new CreditNoteNotFoundException("No existe una nota de credito con numero " + creditNoteNumber + "."));
        ensureAccountVisible(creditNote.account());
        return creditNote;
    }

    /**
     * Lista notas de una invoice.
     *
     * @param invoiceId factura consultada.
     * @return notas ordenadas por emision descendente.
     */
    @Transactional(readOnly = true)
    List<CreditNote> getCreditNotesByInvoice(String invoiceId) {
        getInvoice(invoiceId);
        return creditNoteRepository.findByInvoiceIdOrderByIssueDateDesc(invoiceId);
    }

    /**
     * Lista notas de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return notas ordenadas por emision descendente.
     */
    @Transactional(readOnly = true)
    List<CreditNote> getCreditNotesByAccount(String accountId) {
        var invoiceAccount = invoiceRepository.findByAccountIdOrderByIssueDateDesc(accountId).stream()
                .findFirst()
                .map(Invoice::account);
        if (invoiceAccount.isPresent()) {
            ensureAccountVisible(invoiceAccount.get());
        }
        return creditNoteRepository.findByAccountIdOrderByIssueDateDesc(accountId).stream()
                .filter(note -> userContextService.canAccess(note.account()))
                .toList();
    }

    /**
     * Lista lineas de una nota de credito.
     *
     * @param creditNoteId nota consultada.
     * @return lineas ordenadas.
     */
    @Transactional(readOnly = true)
    List<CreditNoteLine> getCreditNoteLines(String creditNoteId) {
        getCreditNote(creditNoteId);
        return creditNoteLineRepository.findByCreditNoteIdOrderByCreatedAtAsc(creditNoteId);
    }

    /**
     * Lista todas las notas.
     *
     * @return notas ordenadas por fecha descendente.
     */
    @Transactional(readOnly = true)
    List<CreditNote> getCreditNotes() {
        return creditNoteRepository.findAll().stream()
                .filter(note -> userContextService.canAccess(note.account()))
                .sorted(Comparator.comparing(CreditNote::issueDate).reversed())
                .toList();
    }

    private Invoice getInvoice(String invoiceId) {
        var invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("No existe una invoice con id " + invoiceId + "."));
        ensureAccountVisible(invoice.account());
        return invoice;
    }

    private void ensureAccountVisible(Account account) {
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + account.id() + ".");
        }
    }

    private void validateAmount(Invoice invoice, BigDecimal amount) {
        if (invoice.status() == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede crear nota de credito para una invoice cancelada.");
        }
        if (invoice.amountDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La invoice no tiene saldo pendiente para acreditar.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto de la nota de credito debe ser mayor a cero.");
        }
        if (amount.compareTo(invoice.amountDue()) > 0) {
            throw new BusinessRuleException("La nota de credito no puede superar el saldo pendiente de la invoice.");
        }
    }

    private InvoiceLine resolveInvoiceLine(Invoice invoice, String invoiceLineId) {
        if (invoiceLineId == null || invoiceLineId.isBlank()) {
            return null;
        }
        return invoiceLineRepository.findById(invoiceLineId)
                .filter(line -> line.invoice().id().equals(invoice.id()))
                .orElseThrow(() -> new BusinessRuleException("La linea de invoice indicada no pertenece a la invoice."));
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
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
