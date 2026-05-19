package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de lineas de factura persistidas en {@code invoice_lines_t}.
 */
interface InvoiceLineRepository extends JpaRepository<InvoiceLine, String> {

    /**
     * Lista lineas de una factura.
     *
     * @param invoiceId factura consultada.
     * @return lineas ordenadas por fecha de creacion.
     */
    List<InvoiceLine> findByInvoiceIdOrderByCreatedAtAsc(String invoiceId);
}
