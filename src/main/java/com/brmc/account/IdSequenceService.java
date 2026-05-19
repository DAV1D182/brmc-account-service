package com.brmc.account;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio central para generar ids visibles cortos y secuenciales.
 *
 * <p>El metodo {@link #nextId(String)} incrementa una fila de {@code id_sequences_t} dentro de la
 * transaccion activa y devuelve el prefijo mas el nuevo valor. Si una secuencia conocida no existe,
 * la crea con valor base 100 para que la primera llamada devuelva 101.</p>
 */
@Service
class IdSequenceService {

    private static final long DEFAULT_START_VALUE = 100L;
    private static final Map<String, String> DEFAULT_PREFIXES = Map.ofEntries(
            Map.entry("ACCOUNT", "A1-"),
            Map.entry("PRODUCT", "PRD-"),
            Map.entry("SERVICE", "SVC-"),
            Map.entry("BILLING_RUN", "BR-"),
            Map.entry("BILL", "B1-"),
            Map.entry("INVOICE", "INV-"),
            Map.entry("PAYMENT", "PAY-"),
            Map.entry("REFUND", "REF-"),
            Map.entry("WRITEOFF", "WO-"),
            Map.entry("DISPUTE", "DSP-"),
            Map.entry("CREDIT_NOTE", "CN-")
    );

    private final IdSequenceRepository idSequenceRepository;

    /**
     * Crea el servicio de secuencias.
     *
     * @param idSequenceRepository repositorio de secuencias.
     */
    IdSequenceService(IdSequenceRepository idSequenceRepository) {
        this.idSequenceRepository = idSequenceRepository;
    }

    /**
     * Genera el siguiente id visible para una secuencia.
     *
     * @param sequenceName nombre funcional, por ejemplo BILL o PRODUCT.
     * @return id visible como B1-101 o PRD-101.
     * @throws BusinessRuleException si la secuencia no tiene prefijo conocido.
     */
    @Transactional
    String nextId(String sequenceName) {
        var normalized = normalize(sequenceName);
        var sequence = idSequenceRepository.findBySequenceName(normalized)
                .orElseGet(() -> idSequenceRepository.save(new IdSequence(
                        normalized,
                        defaultPrefix(normalized),
                        DEFAULT_START_VALUE
                )));
        var next = sequence.nextId();
        idSequenceRepository.save(sequence);
        return next;
    }

    private String normalize(String sequenceName) {
        if (sequenceName == null || sequenceName.isBlank()) {
            throw new BusinessRuleException("El nombre de secuencia es obligatorio.");
        }
        return sequenceName.trim().toUpperCase();
    }

    private String defaultPrefix(String sequenceName) {
        var prefix = DEFAULT_PREFIXES.get(sequenceName);
        if (prefix == null) {
            throw new BusinessRuleException("No existe configuracion de prefijo para la secuencia " + sequenceName + ".");
        }
        return prefix;
    }
}
