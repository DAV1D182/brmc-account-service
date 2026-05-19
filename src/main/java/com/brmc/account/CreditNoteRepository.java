package com.brmc.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de notas de credito.
 */
interface CreditNoteRepository extends JpaRepository<CreditNote, String> {

    Optional<CreditNote> findByCreditNoteNumber(String creditNoteNumber);

    List<CreditNote> findByInvoiceIdOrderByIssueDateDesc(String invoiceId);

    List<CreditNote> findByAccountIdOrderByIssueDateDesc(String accountId);
}
