package com.brmc.account;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adaptador entre usuarios persistidos y Spring Security.
 */
@Service
class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    DatabaseUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var normalized = UserAdminService.normalizeUsername(username);
        var user = appUserRepository.findById(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
        return User.withUsername(user.username())
                .password(user.passwordHash())
                .disabled(user.status() != AppUserStatus.ACTIVE)
                .roles(user.role().name())
                .build();
    }
}
