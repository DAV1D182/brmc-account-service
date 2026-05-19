package com.brmc.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de seguridad HTTP de la aplicacion.
 *
 * <p>Protege todas las rutas excepto {@code /login}, usa formulario de login propio y delega la
 * autenticacion en usuarios persistidos en {@code app_users_t}. Las rutas de gestion de usuarios
 * quedan restringidas al rol ADMIN.</p>
 */
@Configuration
class SecurityConfig {

    /**
     * Constructor por defecto usado por Spring para cargar la configuracion de seguridad.
     */
    SecurityConfig() {
    }

    /**
     * Define la cadena de filtros de Spring Security.
     *
     * @param http constructor de seguridad HTTP.
     * @return cadena de filtros configurada.
     * @throws Exception si Spring Security no puede construir la configuracion.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers("/users", "/api/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .build();
    }

    /**
     * Codificador BCrypt usado para almacenar contrasenas de usuarios.
     *
     * @return codificador de contrasenas.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
