package com.brmc.account;

/**
 * Estado funcional de una nota de credito asociada a una invoice.
 */
enum CreditNoteStatus {
    /**
     * Nota creada y aplicada al saldo pendiente de la invoice.
     */
    APPLIED,
    /**
     * Nota cancelada y reversada sobre la invoice.
     */
    CANCELLED
}
