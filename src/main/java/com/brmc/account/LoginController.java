package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * Controlador web de la pagina de autenticacion.
 *
 * <p>Entrega el formulario HTML usado por Spring Security. El procesamiento de credenciales no se
 * implementa aqui; lo realiza la cadena de filtros configurada en {@link SecurityConfig}.</p>
 */
@RestController
class LoginController {

    private final UserAdminService userAdminService;

    /**
     * Constructor por defecto usado por Spring para registrar la pagina de login.
     */
    LoginController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    /**
     * Renderiza el formulario de login.
     *
     * @return documento HTML de autenticacion.
     */
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    String login(
            @RequestParam(required = false) String registered,
            @RequestParam(required = false) String registerError
    ) {
        var registerMessage = "";
        if (registered != null) {
            registerMessage = "<p class='message success'>Usuario creado correctamente. Ingresa con tus nuevas credenciales.</p>";
        } else if (registerError != null && !registerError.isBlank()) {
            registerMessage = "<p class='message error'>" + HtmlUtils.htmlEscape(registerError) + "</p>";
        }

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
                            max-width: 480px;
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
                        .panel {
                            display: none;
                        }
                        .panel.active {
                            display: grid;
                            gap: 14px;
                        }
                        .tabs {
                            display: grid;
                            gap: 8px;
                            grid-template-columns: 1fr 1fr;
                            margin: 18px 0;
                        }
                        .tab {
                            background: #eef2f7;
                            border: 1px solid #dbe3ea;
                            color: #334155;
                        }
                        .tab.active {
                            background: #0f766e;
                            border-color: #0f766e;
                            color: #ffffff;
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
                        .hint {
                            color: #64748b;
                            font-size: 12px;
                            margin: -6px 0 0;
                        }
                    </style>
                </head>
                <body>
                    <main>
                        <h1>BRMC Account Service</h1>
                        <p>Ingresa tus credenciales o crea un usuario de acceso.</p>
                        <section id="message"></section>
                        __REGISTER_MESSAGE__
                        <div class="tabs">
                            <button id="loginTab" class="tab active" type="button" onclick="showPanel('login')">Iniciar sesion</button>
                            <button id="registerTab" class="tab" type="button" onclick="showPanel('register')">Crear usuario</button>
                        </div>
                        <form id="loginPanel" class="panel active" method="post" action="/login">
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
                        <form id="registerPanel" class="panel" method="post" action="/register">
                            <label>
                                Usuario
                                <input name="username" type="text" autocomplete="username" maxlength="60" required>
                            </label>
                            <label>
                                Nombre completo
                                <input name="fullName" type="text" autocomplete="name" maxlength="120" required>
                            </label>
                            <label>
                                Correo
                                <input name="email" type="email" autocomplete="email" maxlength="160">
                            </label>
                            <label>
                                Contrasena
                                <input name="password" type="password" autocomplete="new-password" maxlength="120" required>
                            </label>
                            <p class="hint">El registro publico crea usuarios USER. Los permisos ADMIN se administran desde el gestor de usuarios.</p>
                            <button type="submit">Crear usuario</button>
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
                        if (params.has("registerError")) {
                            showPanel("register");
                        }
                        function showPanel(panel) {
                            const isLogin = panel === "login";
                            document.getElementById("loginPanel").classList.toggle("active", isLogin);
                            document.getElementById("registerPanel").classList.toggle("active", !isLogin);
                            document.getElementById("loginTab").classList.toggle("active", isLogin);
                            document.getElementById("registerTab").classList.toggle("active", !isLogin);
                        }
                    </script>
                </body>
                </html>
                """.replace("__REGISTER_MESSAGE__", registerMessage);
    }

    /**
     * Crea un usuario normal desde el formulario publico.
     *
     * @param request datos validados del formulario.
     * @return redireccion al login con mensaje de resultado.
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String register(@Valid RegisterRequest request) {
        try {
            userAdminService.registerPublicUser(
                    request.username(),
                    request.password(),
                    request.fullName(),
                    request.email()
            );
            return redirectToLogin("registered");
        } catch (RuntimeException exception) {
            return redirectToLogin("registerError=" + URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private String redirectToLogin(String query) {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                    <meta charset="utf-8">
                    <meta http-equiv="refresh" content="0;url=/login?%s">
                </head>
                <body></body>
                </html>
                """.formatted(query);
    }

    record RegisterRequest(
            @NotBlank(message = "username es obligatorio")
            @Size(max = 60, message = "username no puede superar 60 caracteres")
            String username,

            @NotBlank(message = "password es obligatorio")
            @Size(max = 120, message = "password no puede superar 120 caracteres")
            String password,

            @NotBlank(message = "fullName es obligatorio")
            @Size(max = 120, message = "fullName no puede superar 120 caracteres")
            String fullName,

            @Size(max = 160, message = "email no puede superar 160 caracteres")
            String email
    ) {
    }
}
