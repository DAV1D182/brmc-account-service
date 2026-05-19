package com.brmc.account;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para administrar inventario.
 *
 * <p>Centraliza creacion, consulta, actualizacion y disponibilidad de items de inventario. Cada
 * operacion registra eventos de auditoria y usa pin virtual time para conservar la fecha logica
 * bajo la cual se hizo el cambio.</p>
 */
@Service
@Transactional
class InventoryService {

    private final AccountRepository accountRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SystemEventRepository eventRepository;
    private final VirtualTimeService virtualTimeService;
    private final UserContextService userContextService;

    /**
     * Crea el servicio de inventario.
     *
     * @param inventoryItemRepository repositorio de inventario.
     * @param eventRepository repositorio de eventos.
     * @param virtualTimeService proveedor de fecha virtual.
     */
    InventoryService(
            AccountRepository accountRepository,
            InventoryItemRepository inventoryItemRepository,
            SystemEventRepository eventRepository,
            VirtualTimeService virtualTimeService,
            UserContextService userContextService
    ) {
        this.accountRepository = accountRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.eventRepository = eventRepository;
        this.virtualTimeService = virtualTimeService;
        this.userContextService = userContextService;
    }

    /**
     * Crea un item de inventario.
     *
     * @param name nombre del item.
     * @param description descripcion opcional.
     * @param unitPrice precio por unidad.
     * @param stockQuantity existencias actuales.
     * @param reorderLevel nivel minimo de reorden.
     * @param reorderTimeDays dias esperados para reorden.
     * @param reorderQuantity cantidad sugerida de reorden.
     * @param available disponibilidad inicial.
     * @return item persistido.
     * @throws BusinessRuleException si faltan datos obligatorios o hay valores negativos.
     */
    InventoryItem createInventoryItem(
            String name,
            String description,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity,
            Boolean available
    ) {
        return createInventoryItem(name, description, unitPrice, stockQuantity, reorderLevel, reorderTimeDays, reorderQuantity, available, null);
    }

    InventoryItem createInventoryItem(
            String name,
            String description,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity,
            Boolean available,
            String accountId
    ) {
        validateName(name);
        var account = resolveAccount(accountId);
        var item = inventoryItemRepository.save(new InventoryItem(
                name,
                description,
                unitPrice,
                stockQuantity,
                reorderLevel,
                reorderTimeDays,
                reorderQuantity,
                available,
                account,
                virtualTimeService.getCurrentVirtualTime()
        ));
        logEvent(EventType.INVENTORY_ITEM_CREATED, "INVENTORY", item.id(), accountId(item),
                "Item de inventario creado: " + item.name() + ".");
        return item;
    }

    /**
     * Actualiza un item de inventario.
     *
     * @param inventoryId identificador del item.
     * @param name nuevo nombre.
     * @param description nueva descripcion.
     * @param unitPrice precio unitario.
     * @param stockQuantity existencias.
     * @param reorderLevel nivel de reorden.
     * @param reorderTimeDays dias de reorden.
     * @param reorderQuantity cantidad de reorden.
     * @param available disponibilidad.
     * @return item actualizado.
     * @throws BusinessRuleException si el id no existe o los datos son invalidos.
     */
    InventoryItem updateInventoryItem(
            String inventoryId,
            String name,
            String description,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity,
            Boolean available
    ) {
        return updateInventoryItem(inventoryId, name, description, unitPrice, stockQuantity, reorderLevel, reorderTimeDays, reorderQuantity, available, null);
    }

    InventoryItem updateInventoryItem(
            String inventoryId,
            String name,
            String description,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity,
            Boolean available,
            String accountId
    ) {
        validateName(name);
        var item = getInventoryItem(inventoryId);
        item.assignAccount(resolveAccountForUpdate(accountId, item));
        item.update(
                name,
                description,
                unitPrice,
                stockQuantity,
                reorderLevel,
                reorderTimeDays,
                reorderQuantity,
                available,
                virtualTimeService.getCurrentVirtualTime()
        );
        var saved = inventoryItemRepository.save(item);
        logEvent(EventType.INVENTORY_ITEM_UPDATED, "INVENTORY", saved.id(), accountId(saved),
                "Item de inventario actualizado: " + saved.name() + ".");
        return saved;
    }

    /**
     * Cambia la disponibilidad de un item.
     *
     * @param inventoryId identificador del item.
     * @param available nuevo estado.
     * @return item actualizado.
     * @throws BusinessRuleException si el id no existe.
     */
    InventoryItem changeAvailability(String inventoryId, Boolean available) {
        var item = getInventoryItem(inventoryId);
        item.changeAvailability(available, virtualTimeService.getCurrentVirtualTime());
        var saved = inventoryItemRepository.save(item);
        logEvent(EventType.INVENTORY_AVAILABILITY_CHANGED, "INVENTORY", saved.id(), accountId(saved),
                "Disponibilidad de inventario cambiada a " + saved.available() + ".");
        return saved;
    }

    /**
     * Consulta un item por id.
     *
     * @param inventoryId identificador del item.
     * @return item encontrado.
     * @throws BusinessRuleException si no existe.
     */
    @Transactional(readOnly = true)
    InventoryItem getInventoryItem(String inventoryId) {
        var item = inventoryItemRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessRuleException("No existe un item de inventario con id " + inventoryId + "."));
        if (!canAccess(item)) {
            throw new BusinessRuleException("No existe un item de inventario con id " + inventoryId + ".");
        }
        return item;
    }

    /**
     * Lista items de inventario ordenados por nombre.
     *
     * @param available filtro opcional de disponibilidad.
     * @return items encontrados.
     */
    @Transactional(readOnly = true)
    List<InventoryItem> getInventoryItems(Boolean available) {
        return inventoryItemRepository.findAll().stream()
                .filter(this::canAccess)
                .filter(item -> available == null || item.available().equals(available))
                .sorted(Comparator.comparing(InventoryItem::name))
                .toList();
    }

    @Transactional(readOnly = true)
    List<InventoryItem> getInventoryItems(String accountId, Boolean available) {
        var account = resolveRequiredAccessibleAccount(accountId);
        return inventoryItemRepository.findAll().stream()
                .filter(this::canAccess)
                .filter(item -> item.account() == null || item.account().id().equals(account.id()))
                .filter(item -> available == null || item.available().equals(available))
                .sorted(Comparator.comparing(InventoryItem::name))
                .toList();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del inventario es obligatorio.");
        }
    }

    private Account resolveAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return null;
        }
        return resolveRequiredAccessibleAccount(accountId);
    }

    private Account resolveAccountForUpdate(String accountId, InventoryItem item) {
        if (accountId != null) {
            return resolveAccount(accountId);
        }
        return item.account();
    }

    private Account resolveRequiredAccessibleAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + accountId + ".");
        }
        return account;
    }

    private boolean canAccess(InventoryItem item) {
        return item.account() == null || userContextService.canAccess(item.account());
    }

    private String accountId(InventoryItem item) {
        return item.account() == null ? null : item.account().id();
    }

    private void logEvent(EventType type, String entityType, String entityId, String accountId, String description) {
        eventRepository.save(new SystemEvent(
                type,
                entityType,
                entityId,
                accountId,
                description,
                virtualTimeService.getCurrentVirtualTime()
        ));
    }
}
