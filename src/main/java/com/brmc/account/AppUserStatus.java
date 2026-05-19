package com.brmc.account;

/**
 * Estado de acceso de un usuario de la aplicacion.
 */
enum AppUserStatus {
    /**
     * Usuario habilitado para iniciar sesion.
     */
    ACTIVE,
    /**
     * Usuario bloqueado para nuevos inicios de sesion.
     */
    INACTIVE
}
