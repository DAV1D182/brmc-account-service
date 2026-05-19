package com.brmc.account;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para consultar y modificar la fecha virtual.
 *
 * <p>Centraliza el acceso al reloj logico del sistema. Cuando no existe configuracion persistida,
 * devuelve la fecha real actual; cuando se actualiza o reinicia, guarda el registro unico y crea el
 * evento de auditoria correspondiente.</p>
 */
@Service
@Transactional
class VirtualTimeService {

    private static final String VIRTUAL_TIME_ID = "BRMC_TIME";

    private final VirtualTimeRepository virtualTimeRepository;
    private final SystemEventRepository eventRepository;

    /**
     * Crea el servicio de fecha virtual.
     *
     * @param virtualTimeRepository repositorio de la configuracion unica.
     * @param eventRepository repositorio de auditoria.
     */
    VirtualTimeService(VirtualTimeRepository virtualTimeRepository, SystemEventRepository eventRepository) {
        this.virtualTimeRepository = virtualTimeRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Obtiene la fecha virtual que deben usar procesos dependientes del tiempo.
     *
     * @return fecha configurada o {@link LocalDateTime#now()} si aun no existe registro.
     */
    @Transactional(readOnly = true)
    LocalDateTime getCurrentVirtualTime() {
        return virtualTimeRepository.findById(VIRTUAL_TIME_ID)
                .map(VirtualTime::currentVirtualTime)
                .orElseGet(LocalDateTime::now);
    }

    /**
     * Consulta el estado de la fecha virtual.
     *
     * @return respuesta con configuracion persistida o fallback al reloj real.
     */
    @Transactional(readOnly = true)
    VirtualTimeResponse getVirtualTime() {
        return virtualTimeRepository.findById(VIRTUAL_TIME_ID)
                .map(VirtualTimeResponse::configured)
                .orElseGet(() -> VirtualTimeResponse.realTime(LocalDateTime.now()));
    }

    /**
     * Actualiza la fecha virtual del sistema.
     *
     * @param currentVirtualTime nueva fecha/hora logica.
     * @param updatedBy usuario informado; admin si esta vacio.
     * @return configuracion persistida.
     */
    VirtualTimeResponse update(LocalDateTime currentVirtualTime, String updatedBy) {
        var virtualTime = virtualTimeRepository.findById(VIRTUAL_TIME_ID)
                .orElseGet(() -> new VirtualTime(currentVirtualTime, updatedBy));
        virtualTime.update(currentVirtualTime, updatedBy == null || updatedBy.isBlank() ? "admin" : updatedBy);
        var saved = virtualTimeRepository.save(virtualTime);
        logEvent(
                EventType.VIRTUAL_TIME_UPDATED,
                "VIRTUAL_TIME",
                saved.id(),
                null,
                "Fecha virtual actualizada a " + saved.currentVirtualTime() + ".",
                saved.currentVirtualTime()
        );
        return VirtualTimeResponse.configured(saved);
    }

    /**
     * Reinicia la fecha virtual al reloj real actual.
     *
     * @param updatedBy usuario informado; admin si esta vacio.
     * @return configuracion persistida con la fecha real actual.
     */
    VirtualTimeResponse reset(String updatedBy) {
        var now = LocalDateTime.now();
        var virtualTime = virtualTimeRepository.findById(VIRTUAL_TIME_ID)
                .orElseGet(() -> new VirtualTime(now, updatedBy));
        virtualTime.update(now, updatedBy == null || updatedBy.isBlank() ? "admin" : updatedBy);
        var saved = virtualTimeRepository.save(virtualTime);
        logEvent(
                EventType.VIRTUAL_TIME_RESET,
                "VIRTUAL_TIME",
                saved.id(),
                null,
                "Fecha virtual reiniciada a la fecha real.",
                saved.currentVirtualTime()
        );
        return VirtualTimeResponse.configured(saved);
    }

    private void logEvent(
            EventType type,
            String entityType,
            String entityId,
            String accountId,
            String description,
            LocalDateTime pinVirtualTimeT
    ) {
        eventRepository.save(new SystemEvent(type, entityType, entityId, accountId, description, pinVirtualTimeT));
    }
}
