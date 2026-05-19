package com.brmc.account;

import java.time.Instant;

/**
 * DTO de respuesta para usuarios de aplicacion.
 */
record AppUserResponse(
        String username,
        String fullName,
        String email,
        AppRole role,
        AppUserStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    static AppUserResponse from(AppUser user) {
        return new AppUserResponse(
                user.username(),
                user.fullName(),
                user.email(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
