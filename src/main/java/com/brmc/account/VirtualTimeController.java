package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para administrar la fecha virtual del sistema.
 *
 * <p>Expone la simulacion de {@code pin_virtual_time}. Los procesos de negocio consultan
 * {@link VirtualTimeService}, no este controlador, para obtener la fecha logica vigente.</p>
 */
@RestController
@RequestMapping("/api/virtual-time")
class VirtualTimeController {

    private final VirtualTimeService virtualTimeService;

    /**
     * Crea el controlador de fecha virtual.
     *
     * @param virtualTimeService servicio de fecha virtual.
     */
    VirtualTimeController(VirtualTimeService virtualTimeService) {
        this.virtualTimeService = virtualTimeService;
    }

    /**
     * Consulta la fecha virtual vigente.
     *
     * @return configuracion persistida o reloj real como fallback.
     */
    @GetMapping
    VirtualTimeResponse getVirtualTime() {
        return virtualTimeService.getVirtualTime();
    }

    /**
     * Actualiza la fecha virtual.
     *
     * @param request fecha y usuario responsable.
     * @return configuracion actualizada.
     */
    @PostMapping
    VirtualTimeResponse update(@Valid @RequestBody UpdateVirtualTimeRequest request) {
        return virtualTimeService.update(request.currentVirtualTime(), request.updatedBy());
    }

    /**
     * Reinicia la fecha virtual a la fecha real actual.
     *
     * @param request usuario responsable opcional.
     * @return configuracion actualizada.
     */
    @PostMapping("/reset")
    VirtualTimeResponse reset(@RequestBody(required = false) ResetVirtualTimeRequest request) {
        return virtualTimeService.reset(request == null ? "admin" : request.updatedBy());
    }

    /**
     * Solicitud de actualizacion de fecha virtual.
     *
     * @param currentVirtualTime fecha/hora logica obligatoria.
     * @param updatedBy usuario opcional.
     */
    record UpdateVirtualTimeRequest(
            @NotNull(message = "currentVirtualTime es obligatorio")
            LocalDateTime currentVirtualTime,
            String updatedBy
    ) {
    }

    /**
     * Solicitud de reinicio de fecha virtual.
     *
     * @param updatedBy usuario opcional.
     */
    record ResetVirtualTimeRequest(String updatedBy) {
    }
}
