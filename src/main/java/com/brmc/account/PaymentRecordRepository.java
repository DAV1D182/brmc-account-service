package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de pagos materializados en {@code payments_t}.
 */
interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {

    /**
     * Lista pagos de una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return pagos ordenados por fecha de creacion ascendente.
     */
    List<PaymentRecord> findByAccountIdOrderByCreatedAtAsc(String accountId);

    /**
     * Lista pagos con remanente no asignado para una cuenta.
     *
     * @param accountId cuenta consultada.
     * @param allocationStatus estado de asignacion buscado.
     * @return pagos no asignados ordenados por fecha.
     */
    List<PaymentRecord> findByAccountIdAndAllocationStatusOrderByCreatedAtAsc(
            String accountId,
            PaymentAllocationStatus allocationStatus
    );

    /**
     * Lista pagos por estado de asignacion.
     *
     * @param allocationStatus estado buscado.
     * @return pagos ordenados por fecha.
     */
    List<PaymentRecord> findByAllocationStatusOrderByCreatedAtAsc(PaymentAllocationStatus allocationStatus);
}
