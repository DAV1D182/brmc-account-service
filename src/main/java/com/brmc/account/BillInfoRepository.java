package com.brmc.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de configuraciones de facturacion persistidas en {@code billinfo_t}.
 */
interface BillInfoRepository extends JpaRepository<BillInfo, String> {

    /**
     * Busca la configuracion de facturacion de una cuenta.
     *
     * @param accountId cuenta propietaria.
     * @return billinfo encontrado o vacio.
     */
    Optional<BillInfo> findByAccountId(String accountId);
}
