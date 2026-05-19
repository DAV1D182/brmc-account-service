package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Secuencia funcional para generar identificadores visibles cortos.
 *
 * <p>La tabla {@code id_sequences_t} no reemplaza claves primarias tecnicas. Su objetivo es
 * producir numeros legibles para pantallas y documentos, por ejemplo {@code B1-101} para bills o
 * {@code PRD-101} para productos.</p>
 */
@Entity
@Table(name = "id_sequences_t")
class IdSequence {

    @Id
    @Column(name = "sequence_name", length = 60, nullable = false)
    private String sequenceName;

    @Column(nullable = false, length = 20)
    private String prefix;

    @Column(nullable = false)
    private long currentValue;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Constructor requerido por JPA.
     */
    protected IdSequence() {
    }

    /**
     * Crea una secuencia con valor inicial.
     *
     * @param sequenceName nombre funcional.
     * @param prefix prefijo visible.
     * @param currentValue ultimo valor usado.
     */
    IdSequence(String sequenceName, String prefix, long currentValue) {
        var now = Instant.now();
        this.sequenceName = sequenceName;
        this.prefix = prefix;
        this.currentValue = currentValue;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Obtiene el nombre de la secuencia.
     *
     * @return nombre funcional.
     */
    String sequenceName() {
        return sequenceName;
    }

    /**
     * Obtiene el prefijo visible.
     *
     * @return prefijo usado al generar el id.
     */
    String prefix() {
        return prefix;
    }

    /**
     * Obtiene el ultimo valor usado.
     *
     * @return valor actual persistido.
     */
    long currentValue() {
        return currentValue;
    }

    /**
     * Incrementa la secuencia y retorna el id visible.
     *
     * @return identificador visible generado.
     */
    String nextId() {
        currentValue++;
        updatedAt = Instant.now();
        return prefix + currentValue;
    }
}
