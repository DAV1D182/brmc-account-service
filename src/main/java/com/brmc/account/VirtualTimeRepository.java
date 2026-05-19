package com.brmc.account;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de la configuracion unica de fecha virtual.
 */
interface VirtualTimeRepository extends JpaRepository<VirtualTime, String> {
}
