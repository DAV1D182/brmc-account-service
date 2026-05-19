package com.brmc.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de facturas persistidas en {@code invoices_t}.
 */
interface InvoiceRepository extends JpaRepository<Invoice, String> {

    /**
     * Busca una factura por numero funcional.
     *
     * @param invoiceNumber numero visible de factura.
     * @return factura encontrada o vacio.
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Busca la factura generada para una cuenta dentro de una corrida.
     *
     * @param billingRunId corrida de billing.
     * @param accountId cuenta facturada.
     * @return factura existente o vacio.
     */
    Optional<Invoice> findByBillingRunIdAndAccountId(String billingRunId, String accountId);

    /**
     * Lista facturas de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return facturas ordenadas por emision descendente.
     */
    List<Invoice> findByAccountIdOrderByIssueDateDesc(String accountId);

    /**
     * Lista facturas de una corrida.
     *
     * @param billingRunId corrida consultada.
     * @return facturas ordenadas por emision ascendente.
     */
    List<Invoice> findByBillingRunIdOrderByIssueDateAsc(String billingRunId);

    /**
     * Lista facturas por estado.
     *
     * @param status estado consultado.
     * @return facturas ordenadas por emision descendente.
     */
    List<Invoice> findByStatusOrderByIssueDateDesc(InvoiceStatus status);
}
