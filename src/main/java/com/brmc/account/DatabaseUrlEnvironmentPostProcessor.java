package com.brmc.account;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Normaliza variables de conexion PostgreSQL provistas por plataformas cloud.
 *
 * <p>Algunos proveedores exponen la base de datos como {@code DATABASE_URL} con esquema
 * {@code postgres://} o {@code postgresql://}. El driver JDBC de PostgreSQL requiere
 * {@code jdbc:postgresql://}, por lo que este post-procesador convierte el valor antes de que
 * Spring Boot construya el {@code DataSource}. Tambien extrae usuario y contrasena cuando vienen
 * embebidos en la URL.</p>
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "brmcCloudDatabaseUrl";

    /**
     * Agrega propiedades de datasource derivadas de variables de entorno cloud.
     *
     * @param environment entorno configurable de Spring.
     * @param application aplicacion Spring Boot en arranque.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var rawUrl = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("BRMC_DB_URL"),
                System.getenv("DATABASE_URL")
        );
        if (rawUrl == null) {
            return;
        }

        var properties = new LinkedHashMap<String, Object>();
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            properties.put("spring.datasource.url", rawUrl);
        } else if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
            properties.putAll(convertPostgresUrl(rawUrl));
        }

        putIfPresent(properties, "spring.datasource.username", firstNonBlank(
                System.getenv("SPRING_DATASOURCE_USERNAME"),
                System.getenv("BRMC_DB_USERNAME")
        ));
        putIfPresent(properties, "spring.datasource.password", firstNonBlank(
                System.getenv("SPRING_DATASOURCE_PASSWORD"),
                System.getenv("BRMC_DB_PASSWORD")
        ));

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private Map<String, Object> convertPostgresUrl(String rawUrl) {
        var uri = URI.create(rawUrl);
        var jdbcUrl = "jdbc:postgresql://" + uri.getHost();
        if (uri.getPort() > 0) {
            jdbcUrl += ":" + uri.getPort();
        }
        jdbcUrl += uri.getPath();
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl += "?" + uri.getQuery();
        }

        var properties = new LinkedHashMap<String, Object>();
        properties.put("spring.datasource.url", jdbcUrl);

        var userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            var split = userInfo.split(":", 2);
            putIfPresent(properties, "spring.datasource.username", decode(split[0]));
            if (split.length > 1) {
                putIfPresent(properties, "spring.datasource.password", decode(split[1]));
            }
        }
        return properties;
    }

    private static void putIfPresent(Map<String, Object> properties, String key, String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value);
        }
    }

    private static String firstNonBlank(String... values) {
        for (var value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
