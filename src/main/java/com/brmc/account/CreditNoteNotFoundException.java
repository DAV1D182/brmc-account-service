package com.brmc.account;

/**
 * Excepcion lanzada cuando una nota de credito no existe.
 */
class CreditNoteNotFoundException extends RuntimeException {

    CreditNoteNotFoundException(String message) {
        super(message);
    }
}
