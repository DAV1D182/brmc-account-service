package com.brmc.account;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para la fecha virtual del sistema.
 *
 * @param currentVirtualTime fecha/hora logica vigente.
 * @param updatedAt fecha/hora real de actualizacion o consulta.
 * @param updatedBy usuario o actor que actualizo la configuracion.
 * @param configured indica si existe registro persistido en {@code virtual_time_t}.
 */
record VirtualTimeResponse(
        LocalDateTime currentVirtualTime,
        LocalDateTime updatedAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        String updatedBy,
        boolean configured
) {

    /**
     * Construye una respuesta desde una configuracion persistida.
     *
     * @param virtualTime entidad de configuracion unica.
     * @return respuesta marcada como configurada.
     */
    static VirtualTimeResponse configured(VirtualTime virtualTime) {
        return new VirtualTimeResponse(
                virtualTime.currentVirtualTime(),
                virtualTime.updatedAt(),
                virtualTime.createdT(),
                virtualTime.pinVirtualTimeT(),
                virtualTime.updatedBy(),
                true
        );
    }

    /**
     * Construye una respuesta cuando no existe configuracion persistida.
     *
     * @param now fecha/hora real usada como fallback.
     * @return respuesta marcada como no configurada.
     */
    static VirtualTimeResponse realTime(LocalDateTime now) {
        return new VirtualTimeResponse(now, now, Instant.now(), now, "SYSTEM", false);
    }
}
