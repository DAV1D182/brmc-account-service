package com.brmc.account;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para la API REST.
 *
 * <p>Convierte excepciones de negocio y validacion en respuestas {@link ProblemDetail} con codigo
 * HTTP consistente, titulo funcional y marca de tiempo. Esta capa evita exponer trazas internas al
 * cliente y centraliza el contrato de errores.</p>
 */
@RestControllerAdvice
class ApiExceptionHandler {

    /**
     * Constructor por defecto usado por Spring para registrar el advice global.
     */
    ApiExceptionHandler() {
    }

    /**
     * Responde cuando una cuenta no existe.
     *
     * @param exception excepcion de busqueda de cuenta.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleAccountNotFound(AccountNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Cuenta no encontrada");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando un pago requerido no existe.
     *
     * @param exception excepcion de busqueda de pago.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handlePaymentNotFound(PaymentNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Pago no encontrado");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando una disputa no existe.
     *
     * @param exception excepcion de busqueda de disputa.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(DisputeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleDisputeNotFound(DisputeNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Disputa no encontrada");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando un producto no existe.
     *
     * @param exception excepcion de busqueda de producto.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleProductNotFound(ProductNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Producto no encontrado");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando un servicio BRM no existe.
     *
     * @param exception excepcion de busqueda de servicio.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(BrmServiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleServiceNotFound(BrmServiceNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Servicio no encontrado");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando una asignacion producto-servicio no existe.
     *
     * @param exception excepcion de busqueda de asignacion.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(ServiceProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleServiceProductNotFound(ServiceProductNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Producto de servicio no encontrado");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando una corrida de billing no existe.
     *
     * @param exception excepcion de busqueda de billing run.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(BillingRunNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleBillingRunNotFound(BillingRunNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Billing run no encontrado");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando una invoice no existe.
     *
     * @param exception excepcion de busqueda de invoice.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(InvoiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleInvoiceNotFound(InvoiceNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Invoice no encontrada");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando una nota de credito no existe.
     *
     * @param exception excepcion de busqueda de nota de credito.
     * @return detalle HTTP 404.
     */
    @ExceptionHandler(CreditNoteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleCreditNoteNotFound(CreditNoteNotFoundException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Nota de credito no encontrada");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando una operacion requiere mas saldo disponible.
     *
     * @param exception excepcion de saldo insuficiente.
     * @return detalle HTTP 409.
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ProblemDetail handleInsufficientBalance(InsufficientBalanceException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("Saldo insuficiente");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando un reembolso excede o duplica lo permitido para un pago.
     *
     * @param exception excepcion de reembolso no permitido.
     * @return detalle HTTP 409.
     */
    @ExceptionHandler(RefundAmountExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ProblemDetail handleRefundAmountExceeded(RefundAmountExceededException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("Reembolso no permitido");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando se intenta operar sobre una cuenta cerrada.
     *
     * @param exception excepcion de cuenta cerrada.
     * @return detalle HTTP 409.
     */
    @ExceptionHandler(AccountClosedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ProblemDetail handleAccountClosed(AccountClosedException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("Cuenta cerrada");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde cuando se intenta resolver nuevamente una disputa cerrada.
     *
     * @param exception excepcion de disputa ya resuelta.
     * @return detalle HTTP 409.
     */
    @ExceptionHandler(DisputeAlreadyResolvedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ProblemDetail handleDisputeAlreadyResolved(DisputeAlreadyResolvedException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("Disputa ya resuelta");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde a reglas de negocio generales.
     *
     * @param exception excepcion funcional.
     * @return detalle HTTP 409.
     */
    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ProblemDetail handleBusinessRule(BusinessRuleException exception) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("Regla de negocio");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Responde a errores de validacion de solicitudes.
     *
     * @param exception excepcion emitida por Jakarta Validation en argumentos de controlador.
     * @return detalle HTTP 400 con lista de campos invalidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud contiene campos invalidos.");
        detail.setTitle("Error de validacion");
        detail.setProperty("timestamp", Instant.now());
        detail.setProperty("errors", List.copyOf(errors));
        return detail;
    }
}
