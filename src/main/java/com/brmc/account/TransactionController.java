package com.brmc.account;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para historial global, reportes y exportacion de transacciones.
 *
 * <p>Permite filtrar por cuenta, tipo, rango de fechas y montos. La ruta de exportacion genera CSV
 * con codificacion UTF-8 y encabezado de descarga.</p>
 */
@RestController
@RequestMapping("/api/transactions")
class TransactionController {

    private final AccountService accountService;

    /**
     * Crea el controlador de transacciones.
     *
     * @param accountService servicio que ejecuta la busqueda de historial.
     */
    TransactionController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Busca transacciones con filtros opcionales.
     *
     * @param accountId cuenta opcional.
     * @param type tipo de transaccion opcional.
     * @param dateFrom fecha/hora inicial opcional.
     * @param dateTo fecha/hora final opcional.
     * @param minAmount monto minimo opcional.
     * @param maxAmount monto maximo opcional.
     * @return transacciones filtradas.
     */
    @GetMapping
    List<TransactionSearchResponse> searchTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        return accountService.searchTransactions(accountId, type, dateFrom, dateTo, minAmount, maxAmount);
    }

    /**
     * Exporta transacciones filtradas como archivo CSV.
     *
     * @param accountId cuenta opcional.
     * @param type tipo de transaccion opcional.
     * @param dateFrom fecha/hora inicial opcional.
     * @param dateTo fecha/hora final opcional.
     * @param minAmount monto minimo opcional.
     * @param maxAmount monto maximo opcional.
     * @return respuesta HTTP con contenido CSV descargable.
     */
    @GetMapping(value = "/export", produces = "text/csv")
    ResponseEntity<String> exportTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        var transactions = accountService.searchTransactions(accountId, type, dateFrom, dateTo, minAmount, maxAmount);
        var csv = toCsv(transactions);

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("brmc-transacciones.csv")
                        .build()
                        .toString())
                .body(csv);
    }

    private String toCsv(List<TransactionSearchResponse> transactions) {
        var csv = new StringBuilder("accountId,ownerName,transactionId,type,amount,currency,originalAmount,originalCurrency,exchangeRate,paymentMethod,description,createdAt,createdT,pinVirtualTimeT\n");
        transactions.forEach(transaction -> csv.append(csv(transaction.accountId())).append(",")
                .append(csv(transaction.ownerName())).append(",")
                .append(csv(transaction.id())).append(",")
                .append(csv(transaction.type().name())).append(",")
                .append(transaction.amount()).append(",")
                .append(csv(transaction.currency().name())).append(",")
                .append(transaction.originalAmount()).append(",")
                .append(csv(transaction.originalCurrency().name())).append(",")
                .append(transaction.exchangeRate()).append(",")
                .append(csv(transaction.paymentMethod() == null ? "" : transaction.paymentMethod().name())).append(",")
                .append(csv(transaction.description())).append(",")
                .append(csv(transaction.createdAt().toString())).append(",")
                .append(csv(transaction.createdT().toString())).append(",")
                .append(csv(transaction.pinVirtualTimeT() == null ? "" : transaction.pinVirtualTimeT().toString())).append("\n"));
        return csv.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
