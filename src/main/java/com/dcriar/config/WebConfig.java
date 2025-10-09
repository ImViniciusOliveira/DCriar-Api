package com.dcriar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS (Cross-Origin Resource Sharing) para a aplicação.
 * Permite que o frontend (rodando em http://localhost:4200) faça requisições
 * para o backend sem a necessidade de um proxy.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**") // Aplica a configuração a todos os endpoints sob /api/v1/
                .allowedOrigins("http://localhost:4200") // Permite requisições desta origem (Frontend Angular)
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS", "HEAD") // Métodos HTTP permitidos
                .allowCredentials(true); // Permite o envio de cookies e headers de autenticação
    }
}
