package com.brmc.account;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio administrativo para usuarios de acceso a BRMC.
 */
@Service
@Transactional
class UserAdminService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultAdminUsername;
    private final String defaultAdminPassword;

    UserAdminService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${brmc.security.username:admin}") String defaultAdminUsername,
            @Value("${brmc.security.password:admin123}") String defaultAdminPassword
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultAdminUsername = defaultAdminUsername;
        this.defaultAdminPassword = defaultAdminPassword;
    }

    /**
     * Garantiza un usuario administrador inicial para no perder acceso al sistema.
     */
    @PostConstruct
    void ensureDefaultAdmin() {
        var username = normalizeUsername(defaultAdminUsername);
        if (!appUserRepository.existsById(username)) {
            appUserRepository.save(new AppUser(
                    username,
                    passwordEncoder.encode(defaultAdminPassword),
                    "Administrador BRMC",
                    null,
                    AppRole.ADMIN,
                    AppUserStatus.ACTIVE
            ));
        }
    }

    List<AppUser> getUsers() {
        return appUserRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::username))
                .toList();
    }

    AppUser createUser(
            String username,
            String password,
            String fullName,
            String email,
            AppRole role,
            AppUserStatus status
    ) {
        var normalized = normalizeUsername(username);
        if (appUserRepository.existsById(normalized)) {
            throw new BusinessRuleException("Ya existe un usuario con username " + normalized + ".");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessRuleException("La contrasena es obligatoria al crear usuarios.");
        }
        return appUserRepository.save(new AppUser(
                normalized,
                passwordEncoder.encode(password),
                normalizeFullName(fullName),
                normalizeNullable(email),
                role,
                status
        ));
    }

    AppUser updateUser(
            String username,
            String password,
            String fullName,
            String email,
            AppRole role,
            AppUserStatus status
    ) {
        var user = getUser(username);
        user.update(normalizeFullName(fullName), normalizeNullable(email), role, status);
        if (password != null && !password.isBlank()) {
            user.changePassword(passwordEncoder.encode(password));
        }
        return appUserRepository.save(user);
    }

    AppUser activate(String username) {
        var user = getUser(username);
        user.activate();
        return appUserRepository.save(user);
    }

    AppUser deactivate(String username) {
        var user = getUser(username);
        if (user.role() == AppRole.ADMIN && activeAdminCount() <= 1) {
            throw new BusinessRuleException("Debe existir al menos un usuario ADMIN activo.");
        }
        user.deactivate();
        return appUserRepository.save(user);
    }

    AppUser getUser(String username) {
        return appUserRepository.findById(normalizeUsername(username))
                .orElseThrow(() -> new BusinessRuleException("No existe un usuario con username " + username + "."));
    }

    static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessRuleException("El username es obligatorio.");
        }
        return username.trim().toLowerCase();
    }

    private long activeAdminCount() {
        return appUserRepository.findAll().stream()
                .filter(user -> user.role() == AppRole.ADMIN)
                .filter(user -> user.status() == AppUserStatus.ACTIVE)
                .count();
    }

    private String normalizeFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessRuleException("El nombre del usuario es obligatorio.");
        }
        return fullName.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
