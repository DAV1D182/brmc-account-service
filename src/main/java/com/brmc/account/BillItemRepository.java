package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de items de bill persistidos en {@code items_t}.
 */
interface BillItemRepository extends JpaRepository<BillItem, String> {

    /**
     * Lista items de un bill.
     *
     * @param billId bill contenedor.
     * @return items ordenados por fecha ascendente.
     */
    List<BillItem> findByBillIdOrderByItemDateAsc(String billId);

    /**
     * Lista items de una cuenta.
     *
     * @param accountId cuenta facturada.
     * @return items ordenados por fecha ascendente.
     */
    List<BillItem> findByAccountIdOrderByItemDateAsc(String accountId);

    /**
     * Lista items asociados a una corrida de billing.
     *
     * @param billingRunId corrida consultada.
     * @return items ordenados por fecha ascendente.
     */
    List<BillItem> findByBillBillingRunIdOrderByItemDateAsc(String billingRunId);
}
