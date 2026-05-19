package com.brmc.account;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador web de la pagina de autenticacion.
 *
 * <p>Entrega el formulario HTML usado por Spring Security. El procesamiento de credenciales no se
 * implementa aqui; lo realiza la cadena de filtros configurada en {@link SecurityConfig}.</p>
 */
@RestController
class LoginController {

    /**
     * Constructor por defecto usado por Spring para registrar la pagina de login.
     */
    LoginController() {
    }

    /**
     * Renderiza el formulario de login.
     *
     * @return documento HTML de autenticacion.
     */
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    String login() {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Iniciar sesion - BRMC</title>
                    <style>
                        :root {
                            color-scheme: light;
                            font-family: Arial, sans-serif;
                            color: #1f2937;
                            background: #f4f7fb;
                        }
                        body {
                            align-items: center;
                            display: flex;
                            justify-content: center;
                            margin: 0;
                            min-height: 100vh;
                            padding: 24px;
                        }
                        main {
                            background: #ffffff;
                            border: 1px solid #dbe3ea;
                            border-radius: 8px;
                            box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
                            max-width: 420px;
                            padding: 28px;
                            width: 100%;
                        }
                        h1 {
                            color: #164e63;
                            font-size: 28px;
                            margin: 0 0 8px;
                        }
                        p {
                            color: #4b5563;
                            margin: 0 0 18px;
                        }
                        form {
                            display: grid;
                            gap: 14px;
                        }
                        label {
                            color: #334155;
                            display: grid;
                            font-size: 13px;
                            font-weight: 700;
                            gap: 6px;
                        }
                        input {
                            border: 1px solid #cbd5e1;
                            border-radius: 6px;
                            color: #111827;
                            font-size: 15px;
                            padding: 11px;
                        }
                        input:focus {
                            border-color: #0f766e;
                            outline: 2px solid #ccfbf1;
                        }
                        button {
                            background: #0f766e;
                            border: 0;
                            border-radius: 6px;
                            color: #ffffff;
                            cursor: pointer;
                            font-size: 15px;
                            font-weight: 700;
                            padding: 12px 16px;
                        }
                        button:hover {
                            background: #115e59;
                        }
                        .message {
                            border-radius: 6px;
                            margin-bottom: 14px;
                            padding: 10px 12px;
                        }
                        .error {
                            background: #fef2f2;
                            border: 1px solid #fecaca;
                            color: #991b1b;
                        }
                        .success {
                            background: #f0fdf4;
                            border: 1px solid #bbf7d0;
                            color: #166534;
                        }
                    </style>
                </head>
                <body>
                    <main>
                        <h1>BRMC Account Service</h1>
                        <p>Ingresa tus credenciales para continuar.</p>
                        <section id="message"></section>
                        <form method="post" action="/login">
                            <label>
                                Usuario
                                <input name="username" type="text" autocomplete="username" required autofocus>
                            </label>
                            <label>
                                Contrasena
                                <input name="password" type="password" autocomplete="current-password" required>
                            </label>
                            <button type="submit">Entrar</button>
                        </form>
                    </main>
                    <script>
                        const params = new URLSearchParams(window.location.search);
                        const message = document.getElementById("message");
                        if (params.has("error")) {
                            message.innerHTML = "<p class='message error'>Usuario o contrasena incorrectos.</p>";
                        }
                        if (params.has("logout")) {
                            message.innerHTML = "<p class='message success'>Sesion cerrada correctamente.</p>";
                        }
                    </script>
                </body>
                </html>
                """;
    }
}
