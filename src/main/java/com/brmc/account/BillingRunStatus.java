package com.brmc.account;

/**
 * Resultado de ejecucion de un ciclo de billing.
 *
 * <p>Permite distinguir corridas iniciadas, completadas correctamente y fallidas con mensaje de
 * error persistido en {@code billing_runs_t}.</p>
 */
enum BillingRunStatus {
    /**
     * Corrida creada e iniciada.
     */
    STARTED,
    /**
     * Corrida finalizada correctamente.
     */
    COMPLETED,
    /**
     * Corrida finalizada con error registrado en el mensaje.
     */
    FAILED
}
