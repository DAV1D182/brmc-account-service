package com.brmc.account;

/**
 * Tipo comercial de producto facturable.
 *
 * <p>{@code ONE_TIME} genera un cargo unico por asignacion. {@code RECURRING} genera cargos
 * periodicos segun su frecuencia de billing.</p>
 */
enum ProductType {
    /**
     * Producto que genera un unico cargo por asignacion.
     */
    ONE_TIME,
    /**
     * Producto que genera cargos recurrentes.
     */
    RECURRING
}
