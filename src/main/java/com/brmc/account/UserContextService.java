package com.brmc.account;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Servicio de contexto para consultar el usuario autenticado y sus permisos.
 */
@Service
class UserContextService {

    /**
     * Obtiene el username autenticado en el hilo actual.
     *
     * @return username normalizado o {@code system} cuando no hay autenticacion interactiva.
     */
    String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "system";
        }
        return authentication.getName().trim().toLowerCase();
    }

    /**
     * Indica si el usuario actual tiene rol administrador.
     *
     * @return {@code true} para ROLE_ADMIN.
     */
    boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    /**
     * Valida si el usuario actual puede ver u operar una cuenta.
     *
     * @param account cuenta evaluada.
     * @return {@code true} si es ADMIN o propietario de la cuenta.
     */
    boolean canAccess(Account account) {
        return isAdmin() || currentUsername().equalsIgnoreCase(account.ownerUsername());
    }
}
