package com.brmc.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de bills generados por billing en {@code bills_t}.
 */
interface BillRepository extends JpaRepository<Bill, String> {

    /**
     * Lista bills de una cuenta.
     *
     * @param accountId cuenta facturada.
     * @return bills ordenados por fecha de emision descendente.
     */
    List<Bill> findByAccountIdOrderByBillDateDesc(String accountId);

    /**
     * Lista bills generados por una corrida.
     *
     * @param billingRunId corrida consultada.
     * @return bills ordenados por fecha de emision ascendente.
     */
    List<Bill> findByBillingRunIdOrderByBillDateAsc(String billingRunId);

    /**
     * Busca un bill por su numero funcional.
     *
     * @param billNo numero del bill.
     * @return bill encontrado o vacio.
     */
    Optional<Bill> findByBillNo(String billNo);
}
