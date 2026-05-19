package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de write-offs materializados en {@code write_offs_t}.
 */
interface WriteOffRecordRepository extends JpaRepository<WriteOffRecord, String> {

    /**
     * Lista write-offs de una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return write-offs ordenados por fecha de creacion ascendente.
     */
    List<WriteOffRecord> findByAccountIdOrderByCreatedAtAsc(String accountId);
}
