package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de lineas de notas de credito.
 */
interface CreditNoteLineRepository extends JpaRepository<CreditNoteLine, String> {

    List<CreditNoteLine> findByCreditNoteIdOrderByCreatedAtAsc(String creditNoteId);
}
