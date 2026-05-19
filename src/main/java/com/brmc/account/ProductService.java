package com.brmc.account;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para administrar el catalogo de productos.
 *
 * <p>Aplica normalizacion de codigos, previene duplicados y delega en {@link Product} las reglas
 * de precio y frecuencia. Cada cambio relevante registra un evento de auditoria.</p>
 */
@Service
@Transactional
class ProductService {

    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final SystemEventRepository eventRepository;
    private final VirtualTimeService virtualTimeService;
    private final IdSequenceService idSequenceService;
    private final UserContextService userContextService;

    /**
     * Crea el servicio de productos.
     *
     * @param productRepository repositorio del catalogo.
     * @param eventRepository repositorio de auditoria.
     */
    ProductService(
            AccountRepository accountRepository,
            ProductRepository productRepository,
            SystemEventRepository eventRepository,
            VirtualTimeService virtualTimeService,
            IdSequenceService idSequenceService,
            UserContextService userContextService
    ) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.eventRepository = eventRepository;
        this.virtualTimeService = virtualTimeService;
        this.idSequenceService = idSequenceService;
        this.userContextService = userContextService;
    }

    /**
     * Crea un producto nuevo en el catalogo.
     *
     * @param code codigo funcional unico.
     * @param name nombre comercial.
     * @param description descripcion opcional.
     * @param productType tipo de producto.
     * @param price precio no negativo.
     * @param currency moneda; COP si es nula.
     * @param billingFrequency frecuencia de billing.
     * @param status estado inicial.
     * @return producto persistido.
     * @throws BusinessRuleException si el codigo falta, ya existe o los datos violan reglas del producto.
     */
    Product createProduct(
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status
    ) {
        return createProduct(code, name, description, productType, price, currency, billingFrequency, status, null);
    }

    Product createProduct(
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status,
            String accountId
    ) {
        var normalizedCode = normalizeCode(code);
        var account = resolveAccount(accountId);
        validateUniqueCode(normalizedCode, account, null);

        var product = productRepository.save(new Product(
                normalizedCode,
                name,
                description,
                productType,
                price,
                currency,
                billingFrequency,
                status,
                idSequenceService.nextId("PRODUCT"),
                account,
                virtualTimeService.getCurrentVirtualTime()
        ));
        logEvent(EventType.PRODUCT_CREATED, "PRODUCT", product.id(), accountId(product),
                "Producto creado: " + product.code() + ".");
        return product;
    }

    /**
     * Actualiza un producto existente.
     *
     * @param productId producto a modificar.
     * @param code nuevo codigo funcional.
     * @param name nuevo nombre.
     * @param description nueva descripcion.
     * @param productType tipo de producto.
     * @param price precio no negativo.
     * @param currency moneda.
     * @param billingFrequency frecuencia de billing.
     * @param status estado comercial.
     * @return producto actualizado.
     * @throws ProductNotFoundException si el producto no existe.
     * @throws BusinessRuleException si el codigo esta duplicado o los datos son invalidos.
     */
    Product updateProduct(
            String productId,
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status
    ) {
        return updateProduct(productId, code, name, description, productType, price, currency, billingFrequency, status, null);
    }

    Product updateProduct(
            String productId,
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status,
            String accountId
    ) {
        var product = getProduct(productId);
        var account = resolveAccountForUpdate(accountId, product);
        var normalizedCode = normalizeCode(code);
        validateUniqueCode(normalizedCode, account, productId);
        product.assignAccount(account);
        product.update(
                normalizedCode,
                name,
                description,
                productType,
                price,
                currency,
                billingFrequency,
                status,
                virtualTimeService.getCurrentVirtualTime()
        );
        var saved = productRepository.save(product);
        logEvent(EventType.PRODUCT_UPDATED, "PRODUCT", saved.id(), accountId(saved),
                "Producto actualizado: " + saved.code() + ".");
        return saved;
    }

    /**
     * Activa un producto para permitir nuevas asignaciones.
     *
     * @param productId producto a activar.
     * @return producto actualizado.
     * @throws ProductNotFoundException si el producto no existe.
     */
    Product activateProduct(String productId) {
        var product = getProduct(productId);
        product.activate(virtualTimeService.getCurrentVirtualTime());
        var saved = productRepository.save(product);
        logEvent(EventType.PRODUCT_ACTIVATED, "PRODUCT", saved.id(), accountId(saved),
                "Producto activado: " + saved.code() + ".");
        return saved;
    }

    /**
     * Inactiva un producto para bloquear nuevas asignaciones.
     *
     * @param productId producto a inactivar.
     * @return producto actualizado.
     * @throws ProductNotFoundException si el producto no existe.
     */
    Product deactivateProduct(String productId) {
        var product = getProduct(productId);
        product.deactivate(virtualTimeService.getCurrentVirtualTime());
        var saved = productRepository.save(product);
        logEvent(EventType.PRODUCT_DEACTIVATED, "PRODUCT", saved.id(), accountId(saved),
                "Producto desactivado: " + saved.code() + ".");
        return saved;
    }

    /**
     * Consulta un producto por id.
     *
     * @param productId identificador del producto.
     * @return producto encontrado.
     * @throws ProductNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    Product getProduct(String productId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("No existe un producto con id " + productId + "."));
        if (!canAccess(product)) {
            throw new ProductNotFoundException("No existe un producto con id " + productId + ".");
        }
        return product;
    }

    /**
     * Lista todos los productos ordenados por fecha de creacion.
     *
     * @return productos del catalogo.
     */
    @Transactional(readOnly = true)
    List<Product> getProducts() {
        return productRepository.findAll().stream()
                .filter(this::canAccess)
                .sorted(Comparator.comparing(Product::createdAt))
                .toList();
    }

    @Transactional(readOnly = true)
    List<Product> getProducts(String accountId) {
        var account = resolveRequiredAccessibleAccount(accountId);
        return productRepository.findAll().stream()
                .filter(this::canAccess)
                .filter(product -> product.account() == null || product.account().id().equals(account.id()))
                .sorted(Comparator.comparing(Product::createdAt))
                .toList();
    }

    /**
     * Lista productos activos disponibles para asignacion.
     *
     * @return productos con estado ACTIVE.
     */
    @Transactional(readOnly = true)
    List<Product> getActiveProducts() {
        return productRepository.findByStatusOrderByCreatedAtAsc(ProductStatus.ACTIVE).stream()
                .filter(this::canAccess)
                .toList();
    }

    private Account resolveAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return null;
        }
        return resolveRequiredAccessibleAccount(accountId);
    }

    private Account resolveAccountForUpdate(String accountId, Product product) {
        if (accountId != null) {
            return resolveAccount(accountId);
        }
        return product.account();
    }

    private Account resolveRequiredAccessibleAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + accountId + ".");
        }
        return account;
    }

    private void validateUniqueCode(String code, Account account, String currentProductId) {
        var existing = account == null
                ? productRepository.findByCodeAndAccountIsNull(code)
                : productRepository.findByCodeAndAccount_Id(code, account.id());
        existing.filter(product -> currentProductId == null || !product.id().equals(currentProductId))
                .ifPresent(product -> {
                    throw new BusinessRuleException("Ya existe un producto con codigo " + code + " para esa cuenta.");
                });
    }

    private boolean canAccess(Product product) {
        return product.account() == null || userContextService.canAccess(product.account());
    }

    private String accountId(Product product) {
        return product.account() == null ? null : product.account().id();
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("El codigo del producto es obligatorio.");
        }
        return code.trim().toUpperCase();
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
