package com.brmc.account;

/**
 * Error controlado cuando el Excel PODL_INPUT no puede convertirse en PODL.
 */
class PodlGenerationException extends RuntimeException {

    PodlGenerationException(String message) {
        super(message);
    }

    PodlGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
