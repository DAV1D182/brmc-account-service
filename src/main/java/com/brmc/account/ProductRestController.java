package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
 * Controlador REST del catalogo de productos.
 *
 * <p>Expone operaciones CRUD basicas y cambios de estado. Las reglas de duplicidad, precio y
 * frecuencia se resuelven en {@link ProductService} y {@link Product}.</p>
 */
@RestController
@RequestMapping("/api/products")
class ProductRestController {

    private final ProductService productService;

    /**
     * Crea el controlador de productos.
     *
     * @param productService servicio del catalogo.
     */
    ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lista productos del catalogo.
     *
     * @return productos registrados.
     */
    @GetMapping
    List<ProductResponse> getProducts(@RequestParam(required = false) String accountId) {
        var products = accountId == null || accountId.isBlank()
                ? productService.getProducts()
                : productService.getProducts(accountId);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * Consulta un producto.
     *
     * @param productId identificador del producto.
     * @return producto encontrado.
     */
    @GetMapping("/{productId}")
    ProductResponse getProduct(@PathVariable String productId) {
        return ProductResponse.from(productService.getProduct(productId));
    }

    /**
     * Crea un producto.
     *
     * @param request datos validados del producto.
     * @return producto creado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.createProduct(
                request.code(),
                request.name(),
                request.description(),
                request.productType(),
                request.price(),
                request.currency(),
                request.billingFrequency(),
                request.status(),
                request.accountId()
        ));
    }

    /**
     * Actualiza un producto existente.
     *
     * @param productId producto a modificar.
     * @param request nuevos datos del producto.
     * @return producto actualizado.
     */
    @PutMapping("/{productId}")
    ProductResponse updateProduct(@PathVariable String productId, @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.updateProduct(
                productId,
                request.code(),
                request.name(),
                request.description(),
                request.productType(),
                request.price(),
                request.currency(),
                request.billingFrequency(),
                request.status(),
                request.accountId()
        ));
    }

    /**
     * Activa un producto.
     *
     * @param productId producto a activar.
     * @return producto actualizado.
     */
    @PostMapping("/{productId}/activate")
    ProductResponse activateProduct(@PathVariable String productId) {
        return ProductResponse.from(productService.activateProduct(productId));
    }

    /**
     * Inactiva un producto.
     *
     * @param productId producto a inactivar.
     * @return producto actualizado.
     */
    @PostMapping("/{productId}/deactivate")
    ProductResponse deactivateProduct(@PathVariable String productId) {
        return ProductResponse.from(productService.deactivateProduct(productId));
    }

    /**
     * Solicitud de creacion o actualizacion de producto.
     *
     * @param code codigo obligatorio y unico.
     * @param name nombre obligatorio.
     * @param description descripcion opcional.
     * @param productType tipo obligatorio.
     * @param price precio obligatorio no negativo.
     * @param currency moneda opcional.
     * @param billingFrequency frecuencia opcional.
     * @param status estado opcional.
     * @param accountId cuenta propietaria opcional.
     */
    record ProductRequest(
            @NotBlank(message = "code es obligatorio")
            @Size(max = 80, message = "code no puede superar 80 caracteres")
            String code,

            @NotBlank(message = "name es obligatorio")
            @Size(max = 160, message = "name no puede superar 160 caracteres")
            String name,

            @Size(max = 500, message = "description no puede superar 500 caracteres")
            String description,

            @NotNull(message = "productType es obligatorio")
            ProductType productType,

            @NotNull(message = "price es obligatorio")
            @DecimalMin(value = "0.00", message = "price no puede ser negativo")
            BigDecimal price,

            Currency currency,

            BillingFrequency billingFrequency,

            ProductStatus status,

            String accountId
    ) {
    }
}
