package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entidad JPA que resume una ejecucion de billing.
 *
 * <p>Cada registro de {@code billing_runs_t} guarda el tipo de corrida, la fecha virtual usada,
 * estado final y totales de procesamiento. El servicio de billing crea la corrida en STARTED y la
 * cierra como COMPLETED o FAILED segun el resultado del proceso.</p>
 */
@Entity
@Table(name = "billing_runs_t")
class BillingRun {

    private static final DateTimeFormatter BILLING_RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String runCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingRunType runType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingRunStatus status;

    @Column(nullable = false)
    private LocalDateTime virtualTime;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private int accountsProcessed;

    @Column(nullable = false)
    private int chargesCreated;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String message;

    /**
     * Constructor requerido por JPA para materializar corridas de billing.
     */
    protected BillingRun() {
    }

    /**
     * Crea una corrida de billing iniciada.
     *
     * @param runType alcance de la ejecucion.
     * @param virtualTime fecha virtual usada para evaluar vencimientos.
     */
    BillingRun(BillingRunType runType, LocalDateTime virtualTime) {
        this.id = newBillingRunId();
        this.runCode = "BILL-" + id;
        this.runType = runType;
        this.status = BillingRunStatus.STARTED;
        this.virtualTime = virtualTime;
        this.createdT = Instant.now();
        this.pinVirtualTimeT = virtualTime;
        this.startedAt = LocalDateTime.now();
        this.accountsProcessed = 0;
        this.chargesCreated = 0;
        this.totalAmount = BigDecimal.ZERO;
    }

    /**
     * Obtiene el identificador interno de la corrida.
     *
     * @return id temporal de billing.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el codigo funcional de la corrida.
     *
     * @return codigo con prefijo BILL.
     */
    String runCode() {
        return runCode;
    }

    /**
     * Obtiene el tipo de ejecucion.
     *
     * @return MANUAL o ACCOUNT.
     */
    BillingRunType runType() {
        return runType;
    }

    /**
     * Obtiene el estado de procesamiento.
     *
     * @return STARTED, COMPLETED o FAILED.
     */
    BillingRunStatus status() {
        return status;
    }

    /**
     * Obtiene la fecha virtual usada por la corrida.
     *
     * @return fecha/hora logica de billing.
     */
    LocalDateTime virtualTime() {
        return virtualTime;
    }

    Instant createdT() {
        return createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? virtualTime : pinVirtualTimeT;
    }

    /**
     * Obtiene la fecha real de inicio.
     *
     * @return momento de creacion de la corrida.
     */
    LocalDateTime startedAt() {
        return startedAt;
    }

    /**
     * Obtiene la fecha real de finalizacion.
     *
     * @return momento de cierre o {@code null} si sigue iniciada.
     */
    LocalDateTime finishedAt() {
        return finishedAt;
    }

    /**
     * Obtiene cuantas cuentas fueron consideradas en la corrida.
     *
     * @return numero de cuentas procesadas.
     */
    int accountsProcessed() {
        return accountsProcessed;
    }

    /**
     * Obtiene cuantos cargos se crearon.
     *
     * @return cantidad de cargos generados.
     */
    int chargesCreated() {
        return chargesCreated;
    }

    /**
     * Obtiene el total de cargos generados.
     *
     * @return suma de montos facturados en COP.
     */
    BigDecimal totalAmount() {
        return totalAmount;
    }

    /**
     * Obtiene el mensaje final de la corrida.
     *
     * @return mensaje de exito o detalle de falla.
     */
    String message() {
        return message;
    }

    /**
     * Marca la corrida como completada.
     *
     * @param accountsProcessed cuentas consideradas.
     * @param chargesCreated cargos creados.
     * @param totalAmount suma de cargos generados.
     */
    void complete(int accountsProcessed, int chargesCreated, BigDecimal totalAmount) {
        this.status = BillingRunStatus.COMPLETED;
        this.accountsProcessed = accountsProcessed;
        this.chargesCreated = chargesCreated;
        this.totalAmount = totalAmount;
        this.finishedAt = LocalDateTime.now();
        this.message = "Billing completado.";
    }

    /**
     * Marca la corrida como fallida.
     *
     * @param message detalle de la excepcion o regla que detuvo el proceso.
     */
    void fail(String message) {
        this.status = BillingRunStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
        this.message = message;
    }

    private static String newBillingRunId() {
        return LocalDateTime.now().format(BILLING_RUN_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
