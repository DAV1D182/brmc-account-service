package com.brmc.account;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/**
 * Repositorio de secuencias visibles en {@code id_sequences_t}.
 */
interface IdSequenceRepository extends JpaRepository<IdSequence, String> {

    /**
     * Busca una secuencia bloqueando la fila para incremento transaccional.
     *
     * @param sequenceName nombre de secuencia.
     * @return secuencia encontrada.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IdSequence> findBySequenceName(String sequenceName);
}
