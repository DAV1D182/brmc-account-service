package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de reembolsos materializados en {@code refunds_t}.
 */
interface RefundRecordRepository extends JpaRepository<RefundRecord, String> {

    /**
     * Lista reembolsos de una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return reembolsos ordenados por fecha de creacion ascendente.
     */
    List<RefundRecord> findByAccountIdOrderByCreatedAtAsc(String accountId);

    /**
     * Lista reembolsos asociados a un pago origen.
     *
     * <p>Permite impedir reembolsos duplicados sobre el mismo pago.</p>
     *
     * @param paymentId identificador del pago.
     * @return reembolsos ordenados por fecha de creacion ascendente.
     */
    List<RefundRecord> findByPaymentIdOrderByCreatedAtAsc(String paymentId);
}
