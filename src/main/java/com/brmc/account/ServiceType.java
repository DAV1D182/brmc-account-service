package com.brmc.account;

/**
 * Clasificacion funcional de servicios simulados al estilo BRM.
 *
 * <p>Se usa para describir el servicio contratado por una cuenta y para generar codigos de
 * servicio cuando el usuario no especifica uno.</p>
 */
enum ServiceType {
    /**
     * Servicio de telefonia movil.
     */
    MOBILE,
    /**
     * Servicio de internet.
     */
    INTERNET,
    /**
     * Servicio de television.
     */
    TV,
    /**
     * Servicio generico cuando no se especifica una categoria.
     */
    GENERIC
}
