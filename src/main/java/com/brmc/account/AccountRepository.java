package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data para persistir cuentas en {@code accounts_t}.
 */
interface AccountRepository extends JpaRepository<Account, String> {

    /**
     * Lista cuentas propiedad de un usuario.
     *
     * @param ownerUsername usuario propietario.
     * @return cuentas del usuario ordenadas por fecha de creacion.
     */
    List<Account> findByOwnerUsernameOrderByCreatedAtAsc(String ownerUsername);
}
