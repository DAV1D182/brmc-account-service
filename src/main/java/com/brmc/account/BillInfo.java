package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la configuracion de facturacion de una cuenta.
 *
 * <p>Modela de forma simplificada el objeto {@code /billinfo} de BRM. Guarda el DOM, ciclo,
 * moneda y fechas de billing usadas para crear bills e items. Cada cuenta mantiene una unica
 * configuracion activa en esta version educativa.</p>
 */
@Entity
@Table(name = "billinfo_t")
class BillInfo {

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false, unique = true, length = 60)
    private String billInfoNo;

    @Column(nullable = false)
    private Integer billingDom;

    @Column(nullable = false, length = 20)
    private String billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillInfoStatus status;

    private LocalDateTime lastBillAt;

    private LocalDateTime nextBillAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA.
     */
    protected BillInfo() {
    }

    /**
     * Crea la configuracion de billing por defecto para una cuenta.
     *
     * @param account cuenta propietaria.
     * @param pinVirtualTimeT fecha virtual vigente al crear la configuracion.
     */
    BillInfo(Account account, LocalDateTime pinVirtualTimeT) {
        var now = Instant.now();
        this.id = "BI-" + account.id();
        this.account = account;
        this.billInfoNo = "BILLINFO-" + account.id();
        this.billingDom = account.billingDom();
        this.billingCycle = account.billingCycle();
        this.currency = account.currency();
        this.status = BillInfoStatus.ACTIVE;
        this.lastBillAt = null;
        this.nextBillAt = pinVirtualTimeT;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Obtiene el identificador de la configuracion.
     *
     * @return id persistido en {@code billinfo_t}.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la cuenta asociada.
     *
     * @return cuenta propietaria.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el numero funcional de billinfo.
     *
     * @return numero legible de configuracion.
     */
    String billInfoNo() {
        return billInfoNo;
    }

    /**
     * Obtiene el dia del mes usado como DOM de facturacion.
     *
     * @return dia configurado.
     */
    Integer billingDom() {
        return billingDom;
    }

    /**
     * Obtiene el ciclo de facturacion.
     *
     * @return ciclo configurado.
     */
    String billingCycle() {
        return billingCycle;
    }

    /**
     * Obtiene la duracion del ciclo de facturacion en meses.
     *
     * @return cantidad de meses del ciclo configurado.
     */
    int cycleMonths() {
        return switch (billingCycle()) {
            case "BIMONTHLY" -> 2;
            case "QUARTERLY" -> 3;
            case "ANNUAL" -> 12;
            default -> 1;
        };
    }

    /**
     * Obtiene la moneda base de facturacion.
     *
     * @return moneda configurada.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene el estado operativo.
     *
     * @return ACTIVE o CLOSED.
     */
    BillInfoStatus status() {
        return status;
    }

    /**
     * Obtiene la ultima fecha virtual facturada.
     *
     * @return fecha del ultimo bill o {@code null}.
     */
    LocalDateTime lastBillAt() {
        return lastBillAt;
    }

    /**
     * Obtiene la proxima fecha esperada de billing.
     *
     * @return siguiente fecha virtual o {@code null}.
     */
    LocalDateTime nextBillAt() {
        return nextBillAt;
    }

    /**
     * Obtiene la fecha real de creacion.
     *
     * @return instante real de persistencia.
     */
    Instant createdAt() {
        return createdAt;
    }

    /**
     * Obtiene el reloj real tecnico de creacion.
     *
     * @return instante real tecnico.
     */
    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    /**
     * Obtiene el pin virtual time usado al crear la configuracion.
     *
     * @return fecha virtual de negocio.
     */
    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    /**
     * Actualiza la informacion de ciclo luego de generar un bill.
     *
     * @param bill bill generado para el ciclo facturado.
     */
    void markBilled(Bill bill) {
        this.lastBillAt = bill.billDate();
        this.nextBillAt = bill.periodEnd().plusSeconds(1);
    }
}
