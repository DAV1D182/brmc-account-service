package com.brmc.account;

/**
 * Rol de seguridad de un usuario de la aplicacion.
 */
enum AppRole {
    /**
     * Usuario administrador con visibilidad completa y gestion de usuarios.
     */
    ADMIN,
    /**
     * Usuario operativo con visibilidad limitada a sus propios datos.
     */
    USER
}
