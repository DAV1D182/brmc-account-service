package com.brmc.account;

import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de billing.
 *
 * <p>Permite ejecutar corridas generales o por cuenta y consultar sus resultados. Los cargos se
 * obtienen desde {@link BillingService} para incluir el detalle en las respuestas.</p>
 */
@RestController
@RequestMapping("/api")
class BillingRestController {

    private final BillingService billingService;

    /**
     * Crea el controlador de billing.
     *
     * @param billingService servicio de billing.
     */
    BillingRestController(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * Ejecuta billing general.
     *
     * @return corrida creada con sus cargos.
     */
    @PostMapping("/billing/run")
    @ResponseStatus(HttpStatus.CREATED)
    BillingRunResponse runBilling() {
        var run = billingService.runBilling();
        return BillingRunResponse.from(run, billingService.getChargesByRun(run.id()));
    }

    /**
     * Ejecuta billing para una cuenta especifica.
     *
     * @param accountId cuenta a facturar.
     * @return corrida creada con sus cargos.
     */
    @PostMapping("/billing/accounts/{accountId}/run")
    @ResponseStatus(HttpStatus.CREATED)
    BillingRunResponse runBillingForAccount(@PathVariable String accountId) {
        var run = billingService.runBillingForAccount(accountId);
        return BillingRunResponse.from(run, billingService.getChargesByRun(run.id()));
    }

    /**
     * Consulta corridas de billing.
     *
     * @return corridas con detalle de cargos.
     */
    @GetMapping("/billing/runs")
    List<BillingRunResponse> getRuns() {
        return billingService.getBillingRuns().stream()
                .map(run -> BillingRunResponse.from(run, billingService.getChargesByRun(run.id())))
                .toList();
    }

    /**
     * Consulta una corrida especifica.
     *
     * @param runId identificador de corrida.
     * @return corrida con detalle de cargos.
     */
    @GetMapping("/billing/runs/{runId}")
    BillingRunResponse getRun(@PathVariable String runId) {
        var run = billingService.getBillingRun(runId);
        return BillingRunResponse.from(run, billingService.getChargesByRun(run.id()));
    }

    /**
     * Consulta cargos de billing de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return cargos asociados.
     */
    @GetMapping("/accounts/{accountId}/billing-charges")
    List<BillingChargeResponse> getChargesByAccount(@PathVariable String accountId) {
        return billingService.getChargesByAccount(accountId).stream()
                .map(BillingChargeResponse::from)
                .toList();
    }

    /**
     * Consulta la configuracion de facturacion de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return billinfo de la cuenta.
     */
    @GetMapping("/accounts/{accountId}/billinfo")
    BillInfoResponse getBillInfo(@PathVariable String accountId) {
        return BillInfoResponse.from(billingService.getBillInfo(accountId));
    }

    /**
     * Consulta bills generados para una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return bills de la cuenta.
     */
    @GetMapping("/accounts/{accountId}/bills")
    List<BillResponse> getBillsByAccount(@PathVariable String accountId) {
        return billingService.getBillsByAccount(accountId).stream()
                .map(BillResponse::from)
                .toList();
    }

    /**
     * Consulta todos los bills generados.
     *
     * @return bills ordenados por fecha descendente.
     */
    @GetMapping("/bills")
    List<BillResponse> getBills() {
        return billingService.getBills().stream()
                .map(BillResponse::from)
                .toList();
    }

    /**
     * Consulta items generados para una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return items de la cuenta.
     */
    @GetMapping("/accounts/{accountId}/items")
    List<BillItemResponse> getItemsByAccount(@PathVariable String accountId) {
        return billingService.getItemsByAccount(accountId).stream()
                .map(BillItemResponse::from)
                .toList();
    }

    /**
     * Consulta un bill con sus items.
     *
     * @param billId identificador del bill.
     * @return bill con detalle de items.
     */
    @GetMapping("/bills/{billId}")
    BillResponse getBill(@PathVariable String billId) {
        var bill = billingService.getBill(billId);
        return BillResponse.from(bill, billingService.getItemsByBill(billId));
    }

    /**
     * Consulta items de un bill.
     *
     * @param billId identificador del bill.
     * @return items asociados.
     */
    @GetMapping("/bills/{billId}/items")
    List<BillItemResponse> getItemsByBill(@PathVariable String billId) {
        return billingService.getItemsByBill(billId).stream()
                .map(BillItemResponse::from)
                .toList();
    }

    /**
     * Exporta los items de un bill en CSV.
     *
     * @param billId bill exportado.
     * @return archivo CSV.
     */
    @GetMapping("/bills/{billId}/csv")
    ResponseEntity<String> exportBillCsv(@PathVariable String billId) {
        var bill = billingService.getBill(billId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(bill.billNo() + ".csv")
                        .build()
                        .toString())
                .body(billingService.exportBillCsv(billId));
    }
}
