package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Usuario autenticable de BRMC Billing Care.
 *
 * <p>Reemplaza el usuario en memoria y permite que cada operador tenga credenciales, rol y estado
 * propios. El identificador funcional es {@code username}; las contrasenas se almacenan ya
 * codificadas por Spring Security.</p>
 */
@Entity
@Table(name = "app_users_t")
class AppUser {

    @Id
    @Column(length = 60, nullable = false)
    private String username;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppUserStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Constructor requerido por JPA.
     */
    protected AppUser() {
    }

    /**
     * Crea un usuario nuevo.
     *
     * @param username nombre de acceso normalizado.
     * @param passwordHash contrasena codificada.
     * @param fullName nombre visible.
     * @param email correo opcional.
     * @param role rol de seguridad.
     * @param status estado inicial.
     */
    AppUser(
            String username,
            String passwordHash,
            String fullName,
            String email,
            AppRole role,
            AppUserStatus status
    ) {
        var now = Instant.now();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role == null ? AppRole.USER : role;
        this.status = status == null ? AppUserStatus.ACTIVE : status;
        this.createdAt = now;
        this.updatedAt = now;
    }

    String username() {
        return username;
    }

    String passwordHash() {
        return passwordHash;
    }

    String fullName() {
        return fullName;
    }

    String email() {
        return email;
    }

    AppRole role() {
        return role;
    }

    AppUserStatus status() {
        return status;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant updatedAt() {
        return updatedAt;
    }

    void update(String fullName, String email, AppRole role, AppUserStatus status) {
        this.fullName = fullName;
        this.email = email;
        this.role = role == null ? AppRole.USER : role;
        this.status = status == null ? AppUserStatus.ACTIVE : status;
        this.updatedAt = Instant.now();
    }

    void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    void activate() {
        this.status = AppUserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    void deactivate() {
        this.status = AppUserStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
}
