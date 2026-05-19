package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entidad JPA que representa un bill generado por una corrida de billing.
 *
 * <p>Modela de forma simplificada el objeto {@code /bill} de BRM. Agrupa los items creados por
 * cargos de billing para una cuenta y conserva totales basicos de facturacion. El bill se crea
 * solo cuando una corrida genera al menos un cargo para la cuenta.</p>
 */
@Entity
@Table(name = "bills_t")
class Bill {

    private static final DateTimeFormatter BILL_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter BILL_PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String billNo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "billinfo_id", nullable = false)
    private BillInfo billInfo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "billing_run_id", nullable = false)
    private BillingRun billingRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(nullable = false)
    private LocalDateTime billDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dueAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA.
     */
    protected Bill() {
    }

    /**
     * Crea un bill abierto para una cuenta dentro de una corrida.
     *
     * @param account cuenta facturada.
     * @param billInfo configuracion usada para calcular el ciclo.
     * @param billingRun corrida que genera el bill.
     */
    Bill(Account account, BillInfo billInfo, BillingRun billingRun, String billNo) {
        var virtualTime = billingRun.virtualTime();
        var periodStartValue = calculatePeriodStart(billInfo, virtualTime);
        this.id = newBillId();
        this.billNo = billNo;
        this.account = account;
        this.billInfo = billInfo;
        this.billingRun = billingRun;
        this.status = BillStatus.OPEN;
        this.periodStart = periodStartValue;
        this.periodEnd = periodStartValue.plusMonths(billInfo.cycleMonths()).minusSeconds(1);
        this.billDate = virtualTime;
        this.dueDate = virtualTime.plusDays(15);
        this.currency = account.currency();
        this.totalAmount = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.dueAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.createdT = createdAt;
        this.pinVirtualTimeT = virtualTime;
    }

    /**
     * Obtiene el identificador interno del bill.
     *
     * @return id persistido.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el numero funcional del bill.
     *
     * @return bill no visible para consultas.
     */
    String billNo() {
        return billNo;
    }

    /**
     * Obtiene la cuenta facturada.
     *
     * @return cuenta propietaria.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el billinfo usado.
     *
     * @return configuracion de billing.
     */
    BillInfo billInfo() {
        return billInfo;
    }

    /**
     * Obtiene la corrida que genero el bill.
     *
     * @return billing run asociado.
     */
    BillingRun billingRun() {
        return billingRun;
    }

    /**
     * Obtiene el estado financiero del bill.
     *
     * @return estado actual.
     */
    BillStatus status() {
        return status;
    }

    /**
     * Obtiene el inicio del periodo facturado.
     *
     * @return fecha/hora inicial del ciclo.
     */
    LocalDateTime periodStart() {
        return periodStart;
    }

    /**
     * Obtiene el final del periodo facturado.
     *
     * @return fecha/hora final del ciclo.
     */
    LocalDateTime periodEnd() {
        return periodEnd;
    }

    /**
     * Obtiene una etiqueta legible del mes o rango de meses facturado.
     *
     * @return periodo en formato {@code yyyy-MM} o {@code yyyy-MM a yyyy-MM}.
     */
    String billingPeriodLabel() {
        var start = periodStart.format(BILL_PERIOD_FORMAT);
        var end = periodEnd.format(BILL_PERIOD_FORMAT);
        return start.equals(end) ? start : start + " a " + end;
    }

    /**
     * Obtiene la fecha virtual de emision.
     *
     * @return fecha del bill.
     */
    LocalDateTime billDate() {
        return billDate;
    }

    /**
     * Obtiene la fecha sugerida de vencimiento.
     *
     * @return fecha de vencimiento calculada.
     */
    LocalDateTime dueDate() {
        return dueDate;
    }

    /**
     * Obtiene la moneda del bill.
     *
     * @return moneda contable.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene el total facturado.
     *
     * @return suma de items del bill.
     */
    BigDecimal totalAmount() {
        return totalAmount;
    }

    /**
     * Obtiene el monto pagado aplicado al bill.
     *
     * @return monto aplicado; actualmente cero hasta implementar asignacion.
     */
    BigDecimal paidAmount() {
        return paidAmount;
    }

    /**
     * Obtiene el saldo pendiente del bill.
     *
     * @return total menos pagos aplicados.
     */
    BigDecimal dueAmount() {
        return dueAmount;
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
     * Obtiene el pin virtual time usado al crear el bill.
     *
     * @return fecha virtual de negocio.
     */
    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? billDate : pinVirtualTimeT;
    }

    /**
     * Suma un item al total del bill.
     *
     * @param amount monto del item creado.
     */
    void addItemAmount(BigDecimal amount) {
        totalAmount = totalAmount.add(amount);
        dueAmount = totalAmount.subtract(paidAmount);
    }

    private static String newBillId() {
        return LocalDateTime.now().format(BILL_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }

    private static LocalDateTime calculatePeriodStart(BillInfo billInfo, LocalDateTime virtualTime) {
        var cycleMonths = billInfo.cycleMonths();
        var dom = Math.max(1, Math.min(31, billInfo.billingDom()));
        var yearMonth = YearMonth.from(virtualTime);
        var day = Math.min(dom, yearMonth.lengthOfMonth());
        var start = yearMonth.atDay(day).atStartOfDay();
        if (start.isAfter(virtualTime)) {
            var previous = yearMonth.minusMonths(cycleMonths);
            start = previous.atDay(Math.min(dom, previous.lengthOfMonth())).atStartOfDay();
        }
        return start;
    }
}
