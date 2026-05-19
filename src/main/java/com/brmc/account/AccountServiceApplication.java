package com.brmc.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion Spring Boot BRMC Account Service.
 *
 * <p>Activa el auto-configurado de Spring Boot, el escaneo de componentes del paquete
 * {@code com.brmc.account} y el arranque del servidor embebido.</p>
 */
@SpringBootApplication
public class AccountServiceApplication {

    /**
     * Constructor publico requerido por las herramientas JavaDoc y por el modelo de clase Java.
     */
    public AccountServiceApplication() {
    }

    /**
     * Inicia la aplicacion.
     *
     * @param args argumentos de linea de comandos recibidos por Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
