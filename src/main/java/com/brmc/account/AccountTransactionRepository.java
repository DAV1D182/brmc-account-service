package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de movimientos financieros de cuenta.
 *
 * <p>Provee consultas ordenadas para historial completo y para subconjuntos por tipo de
 * movimiento.</p>
 */
interface AccountTransactionRepository extends JpaRepository<AccountTransaction, String> {

    /**
     * Consulta el historial financiero completo de una cuenta.
     *
     * @param accountId identificador de la cuenta.
     * @return movimientos ordenados por fecha de creacion ascendente.
     */
    List<AccountTransaction> findByAccountIdOrderByCreatedAtAsc(String accountId);

    /**
     * Consulta movimientos de una cuenta filtrados por tipo.
     *
     * @param accountId identificador de la cuenta.
     * @param type tipo de movimiento solicitado.
     * @return movimientos coincidentes ordenados por fecha de creacion ascendente.
     */
    List<AccountTransaction> findByAccountIdAndTypeOrderByCreatedAtAsc(String accountId, TransactionType type);
}
