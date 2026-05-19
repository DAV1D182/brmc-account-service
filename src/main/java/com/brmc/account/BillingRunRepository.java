package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para corridas de billing persistidas en {@code billing_runs_t}.
 */
interface BillingRunRepository extends JpaRepository<BillingRun, String> {

    /**
     * Recupera las corridas de billing mas recientes primero.
     *
     * @return lista ordenada descendentemente por fecha de inicio.
     */
    List<BillingRun> findAllByOrderByStartedAtDesc();
}
