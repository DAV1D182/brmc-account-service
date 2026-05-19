package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para cargos generados por billing en {@code billing_charges_t}.
 */
interface BillingChargeRepository extends JpaRepository<BillingCharge, String> {

    /**
     * Verifica si una asignacion producto-servicio ya tiene un cargo de un tipo determinado.
     *
     * <p>Se usa para impedir duplicidad de cargos {@code ONE_TIME}.</p>
     *
     * @param serviceProductId identificador de la asignacion producto-servicio.
     * @param chargeType tipo de cargo evaluado.
     * @return {@code true} si ya existe un cargo para esa combinacion.
     */
    boolean existsByServiceProductIdAndChargeType(String serviceProductId, ChargeType chargeType);

    /**
     * Lista los cargos asociados a una corrida de billing.
     *
     * @param billingRunId identificador del billing run.
     * @return cargos ordenados por fecha de creacion ascendente.
     */
    List<BillingCharge> findByBillingRunIdOrderByCreatedAtAsc(String billingRunId);

    /**
     * Lista cargos de billing de una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return cargos ordenados por fecha de cargo ascendente.
     */
    List<BillingCharge> findByAccountIdOrderByChargeDateAsc(String accountId);
}
