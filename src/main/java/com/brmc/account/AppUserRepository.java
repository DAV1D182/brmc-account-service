package com.brmc.account;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de usuarios autenticables.
 */
interface AppUserRepository extends JpaRepository<AppUser, String> {
}
