package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de inventario.
 *
 * <p>Expone operaciones para crear, consultar, actualizar y cambiar disponibilidad de items de
 * inventario. El valor de inventario no se recibe desde el cliente: se calcula en el dominio.</p>
 */
@RestController
@RequestMapping("/api/inventory")
class InventoryRestController {

    private final InventoryService inventoryService;

    /**
     * Crea el controlador de inventario.
     *
     * @param inventoryService servicio de inventario.
     */
    InventoryRestController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Lista items de inventario.
     *
     * @param available filtro opcional de disponibilidad.
     * @return items registrados.
     */
    @GetMapping
    List<InventoryItemResponse> getInventory(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) Boolean available
    ) {
        var items = accountId == null || accountId.isBlank()
                ? inventoryService.getInventoryItems(available)
                : inventoryService.getInventoryItems(accountId, available);
        return items.stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    /**
     * Consulta un item de inventario por id.
     *
     * @param inventoryId identificador del item.
     * @return item encontrado.
     */
    @GetMapping("/{inventoryId}")
    InventoryItemResponse getInventoryItem(@PathVariable String inventoryId) {
        return InventoryItemResponse.from(inventoryService.getInventoryItem(inventoryId));
    }

    /**
     * Crea un item de inventario.
     *
     * @param request datos validados.
     * @return item creado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    InventoryItemResponse createInventoryItem(@Valid @RequestBody InventoryRequest request) {
        return InventoryItemResponse.from(inventoryService.createInventoryItem(
                request.name(),
                request.description(),
                request.unitPrice(),
                request.stockQuantity(),
                request.reorderLevel(),
                request.reorderTimeDays(),
                request.reorderQuantity(),
                request.available(),
                request.accountId()
        ));
    }

    /**
     * Actualiza un item de inventario.
     *
     * @param inventoryId item a modificar.
     * @param request datos validados.
     * @return item actualizado.
     */
    @PutMapping("/{inventoryId}")
    InventoryItemResponse updateInventoryItem(
            @PathVariable String inventoryId,
            @Valid @RequestBody InventoryRequest request
    ) {
        return InventoryItemResponse.from(inventoryService.updateInventoryItem(
                inventoryId,
                request.name(),
                request.description(),
                request.unitPrice(),
                request.stockQuantity(),
                request.reorderLevel(),
                request.reorderTimeDays(),
                request.reorderQuantity(),
                request.available(),
                request.accountId()
        ));
    }

    /**
     * Marca un item como disponible.
     *
     * @param inventoryId item afectado.
     * @return item actualizado.
     */
    @PostMapping("/{inventoryId}/available")
    InventoryItemResponse markAvailable(@PathVariable String inventoryId) {
        return InventoryItemResponse.from(inventoryService.changeAvailability(inventoryId, true));
    }

    /**
     * Marca un item como no disponible.
     *
     * @param inventoryId item afectado.
     * @return item actualizado.
     */
    @PostMapping("/{inventoryId}/unavailable")
    InventoryItemResponse markUnavailable(@PathVariable String inventoryId) {
        return InventoryItemResponse.from(inventoryService.changeAvailability(inventoryId, false));
    }

    /**
     * Solicitud de creacion o actualizacion de inventario.
     *
     * @param name nombre obligatorio.
     * @param description descripcion opcional.
     * @param unitPrice precio unitario no negativo.
     * @param stockQuantity cantidad en existencias.
     * @param reorderLevel nivel del nuevo pedido.
     * @param reorderTimeDays tiempo del nuevo pedido en dias.
     * @param reorderQuantity cantidad del nuevo pedido.
     * @param available disponibilidad.
     * @param accountId cuenta propietaria opcional.
     */
    record InventoryRequest(
            @NotBlank(message = "name es obligatorio")
            @Size(max = 160, message = "name no puede superar 160 caracteres")
            String name,

            @Size(max = 500, message = "description no puede superar 500 caracteres")
            String description,

            @NotNull(message = "unitPrice es obligatorio")
            @DecimalMin(value = "0.00", message = "unitPrice no puede ser negativo")
            BigDecimal unitPrice,

            @NotNull(message = "stockQuantity es obligatorio")
            @Min(value = 0, message = "stockQuantity no puede ser negativo")
            Integer stockQuantity,

            @NotNull(message = "reorderLevel es obligatorio")
            @Min(value = 0, message = "reorderLevel no puede ser negativo")
            Integer reorderLevel,

            @NotNull(message = "reorderTimeDays es obligatorio")
            @Min(value = 0, message = "reorderTimeDays no puede ser negativo")
            Integer reorderTimeDays,

            @NotNull(message = "reorderQuantity es obligatorio")
            @Min(value = 0, message = "reorderQuantity no puede ser negativo")
            Integer reorderQuantity,

            Boolean available,

            String accountId
    ) {
    }
}
