package com.brmc.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio del catalogo comercial de productos.
 */
interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Verifica unicidad del codigo de producto.
     *
     * @param code codigo normalizado del producto.
     * @return {@code true} si ya existe.
     */
    boolean existsByCode(String code);

    /**
     * Busca un producto por codigo comercial.
     *
     * @param code codigo normalizado del producto.
     * @return producto encontrado, si existe.
     */
    Optional<Product> findByCode(String code);

    /**
     * Busca un producto global por codigo comercial.
     *
     * @param code codigo normalizado del producto.
     * @return producto global encontrado, si existe.
     */
    Optional<Product> findByCodeAndAccountIsNull(String code);

    /**
     * Busca un producto por codigo dentro de una cuenta.
     *
     * @param code codigo normalizado del producto.
     * @param accountId identificador de cuenta.
     * @return producto encontrado, si existe.
     */
    Optional<Product> findByCodeAndAccount_Id(String code, String accountId);

    /**
     * Lista productos por estado.
     *
     * @param status estado comercial.
     * @return productos coincidentes ordenados por fecha de creacion ascendente.
     */
    List<Product> findByStatusOrderByCreatedAtAsc(ProductStatus status);
}
