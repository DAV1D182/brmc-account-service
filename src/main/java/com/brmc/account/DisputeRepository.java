package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de disputas registradas sobre cuentas.
 */
interface DisputeRepository extends JpaRepository<Dispute, String> {

    /**
     * Lista disputas asociadas a una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return disputas ordenadas por fecha de creacion ascendente.
     */
    List<Dispute> findByAccountIdOrderByCreatedAtAsc(String accountId);

    /**
     * Lista disputas por estado.
     *
     * @param status estado de disputa.
     * @return disputas coincidentes ordenadas por fecha de creacion ascendente.
     */
    List<Dispute> findByStatusOrderByCreatedAtAsc(DisputeStatus status);

    /**
     * Lista disputas de una cuenta filtradas por estado.
     *
     * @param accountId identificador de cuenta.
     * @param status estado de disputa.
     * @return disputas coincidentes ordenadas por fecha de creacion ascendente.
     */
    List<Dispute> findByAccountIdAndStatusOrderByCreatedAtAsc(String accountId, DisputeStatus status);
}
