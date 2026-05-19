package com.brmc.account;

/**
 * Estado de una definicion general de servicio dentro del catalogo.
 */
enum ServiceCatalogStatus {
    /**
     * Definicion disponible para activar servicios en cuentas.
     */
    ACTIVE,
    /**
     * Definicion no disponible para nuevas activaciones.
     */
    INACTIVE
}
