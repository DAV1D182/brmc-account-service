package com.brmc.account;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion que ejecuta el proceso basico de billing.
 *
 * <p>Usa la fecha virtual para decidir que productos deben facturarse. Solo procesa cuentas y
 * servicios activos, evita duplicar cargos ONE_TIME y mueve la proxima fecha de productos
 * recurrentes mensuales despues de cada cargo. Cada cargo impacta el saldo mediante una
 * transaccion financiera y queda auditado en {@code system_events_t}.</p>
 */
@Service
@Transactional
class BillingService {

    private final AccountRepository accountRepository;
    private final BrmServiceRepository serviceRepository;
    private final ServiceProductRepository serviceProductRepository;
    private final BillingRunRepository billingRunRepository;
    private final BillingChargeRepository billingChargeRepository;
    private final BillInfoRepository billInfoRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final InvoiceService invoiceService;
    private final IdSequenceService idSequenceService;
    private final VirtualTimeService virtualTimeService;
    private final SystemEventRepository eventRepository;
    private final UserContextService userContextService;

    /**
     * Crea el servicio de billing.
     *
     * @param accountRepository repositorio de cuentas.
     * @param serviceRepository repositorio de servicios.
     * @param serviceProductRepository repositorio de productos asignados.
     * @param billingRunRepository repositorio de corridas.
     * @param billingChargeRepository repositorio de cargos.
     * @param billInfoRepository repositorio de configuracion de facturacion.
     * @param billRepository repositorio de bills.
     * @param billItemRepository repositorio de items.
     * @param invoiceService servicio que genera invoices desde cargos.
     * @param virtualTimeService proveedor de fecha virtual.
     * @param eventRepository repositorio de auditoria.
     */
    BillingService(
            AccountRepository accountRepository,
            BrmServiceRepository serviceRepository,
            ServiceProductRepository serviceProductRepository,
            BillingRunRepository billingRunRepository,
            BillingChargeRepository billingChargeRepository,
            BillInfoRepository billInfoRepository,
            BillRepository billRepository,
            BillItemRepository billItemRepository,
            InvoiceService invoiceService,
            IdSequenceService idSequenceService,
            VirtualTimeService virtualTimeService,
            SystemEventRepository eventRepository,
            UserContextService userContextService
    ) {
        this.accountRepository = accountRepository;
        this.serviceRepository = serviceRepository;
        this.serviceProductRepository = serviceProductRepository;
        this.billingRunRepository = billingRunRepository;
        this.billingChargeRepository = billingChargeRepository;
        this.billInfoRepository = billInfoRepository;
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.invoiceService = invoiceService;
        this.idSequenceService = idSequenceService;
        this.virtualTimeService = virtualTimeService;
        this.eventRepository = eventRepository;
        this.userContextService = userContextService;
    }

    /**
     * Ejecuta billing general para todas las cuentas activas.
     *
     * @return corrida finalizada como COMPLETED o FAILED.
     */
    BillingRun runBilling() {
        var virtualTime = virtualTimeService.getCurrentVirtualTime();
        var run = billingRunRepository.save(new BillingRun(BillingRunType.MANUAL, virtualTime));
        logEvent(EventType.BILLING_RUN_STARTED, "BILLING_RUN", run.id(), null,
                "Billing general iniciado con fecha virtual " + virtualTime + ".");

        try {
            var sourceAccounts = userContextService.isAdmin()
                    ? accountRepository.findAll()
                    : accountRepository.findByOwnerUsernameOrderByCreatedAtAsc(userContextService.currentUsername());
            var accounts = sourceAccounts.stream()
                    .filter(account -> account.status() == AccountStatus.ACTIVE)
                    .sorted(Comparator.comparing(Account::createdAt))
                    .toList();
            return processRun(run, accounts);
        } catch (Exception exception) {
            run.fail(exception.getMessage());
            var failed = billingRunRepository.save(run);
            logEvent(EventType.BILLING_RUN_FAILED, "BILLING_RUN", failed.id(), null,
                    "Billing general fallo: " + exception.getMessage());
            return failed;
        }
    }

    /**
     * Ejecuta billing para una cuenta especifica.
     *
     * @param accountId cuenta a facturar.
     * @return corrida finalizada como COMPLETED o FAILED.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     */
    BillingRun runBillingForAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        if (account.status() == AccountStatus.CLOSED) {
            throw new AccountClosedException("La cuenta esta cerrada y no puede facturarse.");
        }

        var virtualTime = virtualTimeService.getCurrentVirtualTime();
        var run = billingRunRepository.save(new BillingRun(BillingRunType.ACCOUNT, virtualTime));
        logEvent(EventType.BILLING_RUN_STARTED, "BILLING_RUN", run.id(), account.id(),
                "Billing de cuenta iniciado con fecha virtual " + virtualTime + ".");

        try {
            return processRun(run, List.of(account));
        } catch (Exception exception) {
            run.fail(exception.getMessage());
            var failed = billingRunRepository.save(run);
            logEvent(EventType.BILLING_RUN_FAILED, "BILLING_RUN", failed.id(), account.id(),
                    "Billing de cuenta fallo: " + exception.getMessage());
            return failed;
        }
    }

    /**
     * Lista corridas de billing de la mas reciente a la mas antigua.
     *
     * @return corridas registradas.
     */
    @Transactional(readOnly = true)
    List<BillingRun> getBillingRuns() {
        return billingRunRepository.findAllByOrderByStartedAtDesc();
    }

    /**
     * Lista todos los bills generados.
     *
     * @return bills ordenados por fecha de emision descendente.
     */
    @Transactional(readOnly = true)
    List<Bill> getBills() {
        return billRepository.findAll().stream()
                .filter(bill -> userContextService.canAccess(bill.account()))
                .sorted(Comparator.comparing(Bill::billDate).reversed())
                .toList();
    }

    /**
     * Consulta una corrida por id.
     *
     * @param runId identificador de corrida.
     * @return corrida encontrada.
     * @throws BillingRunNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    BillingRun getBillingRun(String runId) {
        return billingRunRepository.findById(runId)
                .orElseThrow(() -> new BillingRunNotFoundException("No existe un billing run con id " + runId + "."));
    }

    /**
     * Lista cargos creados por una corrida.
     *
     * @param runId corrida consultada.
     * @return cargos ordenados por fecha de creacion.
     * @throws BillingRunNotFoundException si la corrida no existe.
     */
    @Transactional(readOnly = true)
    List<BillingCharge> getChargesByRun(String runId) {
        getBillingRun(runId);
        return billingChargeRepository.findByBillingRunIdOrderByCreatedAtAsc(runId);
    }

    /**
     * Lista cargos de billing de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return cargos ordenados por fecha de cargo.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<BillingCharge> getChargesByAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        return billingChargeRepository.findByAccountIdOrderByChargeDateAsc(accountId);
    }

    /**
     * Consulta el billinfo de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return configuracion de facturacion.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    BillInfo getBillInfo(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        return billInfoRepository.findByAccountId(accountId)
                .orElseGet(() -> new BillInfo(account, virtualTimeService.getCurrentVirtualTime()));
    }

    /**
     * Lista bills de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return bills ordenados por fecha de emision descendente.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<Bill> getBillsByAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        return billRepository.findByAccountIdOrderByBillDateDesc(accountId);
    }

    /**
     * Consulta un bill por id.
     *
     * @param billId identificador del bill.
     * @return bill encontrado.
     * @throws BillingRunNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    Bill getBill(String billId) {
        var bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingRunNotFoundException("No existe un bill con id " + billId + "."));
        ensureAccountVisible(bill.account());
        return bill;
    }

    /**
     * Lista items de un bill.
     *
     * @param billId bill consultado.
     * @return items ordenados por fecha.
     */
    @Transactional(readOnly = true)
    List<BillItem> getItemsByBill(String billId) {
        getBill(billId);
        return billItemRepository.findByBillIdOrderByItemDateAsc(billId);
    }

    /**
     * Exporta un bill con sus items en formato CSV.
     *
     * @param billId bill exportado.
     * @return contenido CSV.
     */
    @Transactional(readOnly = true)
    String exportBillCsv(String billId) {
        var bill = getBill(billId);
        var items = getItemsByBill(billId);
        var csv = new StringBuilder();
        csv.append("bill_number,account_number,period_start,period_end,item_type,item_date,description,amount,currency\n");
        for (var item : items) {
            csv.append(csv(bill.billNo())).append(',')
                    .append(csv(bill.account().id())).append(',')
                    .append(bill.periodStart()).append(',')
                    .append(bill.periodEnd()).append(',')
                    .append(item.itemType()).append(',')
                    .append(item.itemDate()).append(',')
                    .append(csv(item.description())).append(',')
                    .append(item.amount()).append(',')
                    .append(item.currency()).append('\n');
        }
        return csv.toString();
    }

    /**
     * Lista items de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return items ordenados por fecha.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<BillItem> getItemsByAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        return billItemRepository.findByAccountIdOrderByItemDateAsc(accountId);
    }

    private void ensureAccountVisible(Account account) {
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + account.id() + ".");
        }
    }

    private BillingRun processRun(BillingRun run, List<Account> accounts) {
        var chargesCreated = 0;
        var totalAmount = BigDecimal.ZERO;
        var virtualTime = run.virtualTime();

        for (var account : accounts) {
            var billInfo = ensureBillInfo(account);
            Bill bill = null;
            var services = serviceRepository.findByAccountIdOrderByCreatedAtAsc(account.id()).stream()
                    .filter(service -> service.status() == ServiceStatus.ACTIVE)
                    .toList();
            for (var service : services) {
                var serviceProducts = serviceProductRepository.findByServiceIdAndStatusOrderByAssignedAtAsc(
                        service.id(),
                        ServiceProductStatus.ACTIVE
                );
                for (var serviceProduct : serviceProducts) {
                    if (shouldBill(serviceProduct, virtualTime)) {
                        if (bill == null) {
                            bill = createBill(account, billInfo, run);
                        }
                        var charge = createCharge(run, bill, account, service, serviceProduct);
                        chargesCreated++;
                        totalAmount = totalAmount.add(charge.amount());
                    }
                }
            }
            if (bill != null) {
                billInfo.markBilled(bill);
                billInfoRepository.save(billInfo);
                billRepository.save(bill);
            }
        }

        run.complete(accounts.size(), chargesCreated, totalAmount);
        var completed = billingRunRepository.save(run);
        if (chargesCreated > 0) {
            invoiceService.generateInvoicesForBillingRun(completed.id());
        }
        logEvent(EventType.BILLING_RUN_COMPLETED, "BILLING_RUN", completed.id(), null,
                "Billing completado. Cargos creados: " + chargesCreated + ".");
        return completed;
    }

    private boolean shouldBill(ServiceProduct serviceProduct, java.time.LocalDateTime virtualTime) {
        var product = serviceProduct.product();
        if (product.status() != ProductStatus.ACTIVE || !serviceProduct.isActive()) {
            return false;
        }

        if (product.productType() == ProductType.ONE_TIME) {
            return !billingChargeRepository.existsByServiceProductIdAndChargeType(serviceProduct.id(), ChargeType.ONE_TIME);
        }

        return product.productType() == ProductType.RECURRING
                && product.billingFrequency() == BillingFrequency.MONTHLY
                && (serviceProduct.nextBillAt() == null || !serviceProduct.nextBillAt().isAfter(virtualTime));
    }

    private BillingCharge createCharge(
            BillingRun run,
            Bill bill,
            Account account,
            BrmService service,
            ServiceProduct serviceProduct
    ) {
        var product = serviceProduct.product();
        var chargeType = product.productType() == ProductType.ONE_TIME ? ChargeType.ONE_TIME : ChargeType.RECURRING;
        var transactionDescription = "Billing charge - " + product.code() + " - " + product.name()
                + " - periodo " + bill.billingPeriodLabel();
        var transaction = account.applyBillingCharge(product.price(), transactionDescription, run.virtualTime());
        accountRepository.save(account);
        serviceProduct.markBilled(run.virtualTime(), bill.periodEnd().plusSeconds(1));
        serviceProductRepository.save(serviceProduct);
        var charge = billingChargeRepository.save(new BillingCharge(
                run,
                bill,
                account,
                service,
                serviceProduct,
                product,
                chargeType,
                run.virtualTime(),
                transaction.id()
        ));
        var item = billItemRepository.save(new BillItem(bill, charge));
        bill.addItemAmount(item.amount());
        logEvent(EventType.BILLING_CHARGE_CREATED, "BILLING_CHARGE", charge.id(), account.id(),
                charge.description() + " por " + charge.amount() + " COP. Periodo: " + bill.billingPeriodLabel() + ".");
        logEvent(EventType.ITEM_CREATED, "ITEM", item.id(), account.id(),
                "Item " + item.itemNo() + " creado para bill " + bill.billNo() + ".");
        logEvent(EventType.BILL_ITEM_CREATED, "BILL_ITEM", item.id(), account.id(),
                "Bill item " + item.itemNo() + " creado para bill " + bill.billNo() + ".");
        return charge;
    }

    private BillInfo ensureBillInfo(Account account) {
        return billInfoRepository.findByAccountId(account.id())
                .orElseGet(() -> {
                    var billInfo = billInfoRepository.save(new BillInfo(account, virtualTimeService.getCurrentVirtualTime()));
                    logEvent(EventType.BILLINFO_CREATED, "BILLINFO", billInfo.id(), account.id(),
                            "Billinfo creado automaticamente para billing.");
                    return billInfo;
                });
    }

    private Bill createBill(Account account, BillInfo billInfo, BillingRun run) {
        var bill = billRepository.save(new Bill(account, billInfo, run, idSequenceService.nextId("BILL")));
        logEvent(EventType.BILL_CREATED, "BILL", bill.id(), account.id(),
                "Bill " + bill.billNo() + " creado para corrida " + run.runCode()
                        + ". Ciclo " + billInfo.billingCycle()
                        + ", periodo " + bill.billingPeriodLabel()
                        + ", DOM " + billInfo.billingDom() + ".");
        return bill;
    }

    private void logEvent(EventType type, String entityType, String entityId, String accountId, String description) {
        eventRepository.save(new SystemEvent(
                type,
                entityType,
                entityId,
                accountId,
                description,
                virtualTimeService.getCurrentVirtualTime()
        ));
    }

    private String csv(Object value) {
        var text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
